package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class BookPublishRequestManager {

    private final Queue<BookPublishRequest> publishingQueue;

    @Inject
    BookPublishRequestManager() {
        publishingQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds a {@link BookPublishRequest} into the queue to be processed and published.
     *
     * @param publishRequest the BookPublishRequest to be added and processed
     */
    public void addBookPublishRequest(BookPublishRequest publishRequest) {
        publishingQueue.add(publishRequest);
    }

    /**
     * Provides the next {@link BookPublishRequest} to be processed for publication.
     *
     * @return the next Book to be published, or null if there are no requests to process
     */
    public BookPublishRequest getBookPublishRequestToProcess() {
        return publishingQueue.poll();
    }
}
