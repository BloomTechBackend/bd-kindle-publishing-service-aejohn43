package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import javax.inject.Inject;

public class BookPublishTask implements Runnable{
    private BookPublishRequestManager bookPublishRequestManager;
    private CatalogDao catalogDao;
    private PublishingStatusDao publishingStatusDao;

    @Inject
    public BookPublishTask (BookPublishRequestManager bookPublishRequestManager, CatalogDao catalogDao,
                            PublishingStatusDao publishingStatusDao) {

        this.bookPublishRequestManager = bookPublishRequestManager;
        this.catalogDao = catalogDao;
        this.publishingStatusDao = publishingStatusDao;
    }

    /*public void run() {
        BookPublishRequest requestToProcess = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (requestToProcess == null){
            return;
        }
        while (requestToProcess!=null) {
            publishingStatusDao.setPublishingStatus(requestToProcess
                            .getPublishingRecordId(),
                    PublishingRecordStatus.IN_PROGRESS, requestToProcess.getBookId());

            KindleFormattedBook formatedBook = KindleFormatConverter.format(requestToProcess);
            CatalogItemVersion book;
            try {
                book = catalogDao.validateBookExists(requestToProcess.getBookId());
                if (book.isInactive()) {
                    //update the old item
                    catalogDao.createorupdate(book);

                    //new item in the table
                    newBookUpdate(formatedBook, book);

                } else {
                    //update the old item
                    book.setInactive(true);
                    catalogDao.createorupdate(book);

                    //new item in the table
                    newBookUpdate(formatedBook, book);

                }
            } catch (BookNotFoundException e) {
                //new item in the table
                book = new CatalogItemVersion();
                book.setBookId(KindlePublishingUtils.generateBookId());
                newBookUpdate(formatedBook, book);
                publishingStatusDao.setPublishingStatus(requestToProcess
                                .getPublishingRecordId(),
                        PublishingRecordStatus.SUCCESSFUL, requestToProcess.getBookId());
            }
            requestToProcess = bookPublishRequestManager.getBookPublishRequestToProcess();
        }

    }*/
    @Override
    public void run() {
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (request == null) return;

        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());

        try {
            CatalogItemVersion item = catalogDao.createOrUpdateBook(KindleFormatConverter.format(request));
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, item.getBookId());
        } catch (BookNotFoundException e) {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId());
        }
    }
    private void newBookUpdate(KindleFormattedBook formatedBook, CatalogItemVersion book) {
        book.setInactive(false);
        book.setVersion(book.getVersion() + 1);
        book.setText(formatedBook.getText());
        book.setAuthor(formatedBook.getAuthor());
        book.setGenre(formatedBook.getGenre());
        book.setTitle(formatedBook.getTitle());
        this.catalogDao.createorupdate(book);
    }
}
