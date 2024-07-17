package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
final public class BookPublishRequestManager {
    final private ConcurrentLinkedQueue<BookPublishRequest> collectionBookPublishRequest = new ConcurrentLinkedQueue<>();
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
        return collectionBookPublishRequest.poll();
    }
}
