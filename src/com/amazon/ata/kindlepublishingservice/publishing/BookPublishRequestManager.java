package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {
    private Queue<BookPublishRequest> collectionBookPublishRequest = new LinkedList<>();
    @Inject
    public BookPublishRequestManager (){
    }
    public void addBookPublishRequest (BookPublishRequest bookPublishRequest) {
        collectionBookPublishRequest.add(bookPublishRequest);
    }

    public BookPublishRequest getBookPublishRequestToProcess (){
        if (collectionBookPublishRequest.isEmpty()) {
            return null;
        }
        return collectionBookPublishRequest.remove();
    }
}
