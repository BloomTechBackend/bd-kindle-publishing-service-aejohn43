package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.activity.RemoveBookFromCatalogActivity;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public void removeBook (String bookId) {

            CatalogItemVersion book = getLatestVersionOfBook(bookId);
            if (book == null || book.isInactive()){
                throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
            }
            book.setBookId(bookId);
            book.setInactive(true);
            dynamoDbMapper.save(book);
    }
    public CatalogItemVersion validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
        return book;
    }
    public void createorupdate (CatalogItemVersion book){
        dynamoDbMapper.save(book);
    }
    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook book) throws BookNotFoundException {
        if (book.getBookId() == null) {
            String bookId = KindlePublishingUtils.generateBookId();
            CatalogItemVersion newBook = new CatalogItemVersion();
            newBook.setBookId(bookId);
            newBook.setVersion(1);
            newBook.setTitle(book.getTitle());
            newBook.setGenre(book.getGenre());
            newBook.setAuthor(book.getAuthor());
            newBook.setText(book.getText());
            dynamoDbMapper.save(newBook);
            return getLatestVersionOfBook(newBook.getBookId());
        }

        validateBookExists(book.getBookId());
        CatalogItemVersion latest = getLatestVersionOfBook(book.getBookId());
        latest.setVersion(latest.getVersion() + 1);
        removeBook(latest.getBookId());
        dynamoDbMapper.save(latest);
        return getLatestVersionOfBook(latest.getBookId());
    }

}
