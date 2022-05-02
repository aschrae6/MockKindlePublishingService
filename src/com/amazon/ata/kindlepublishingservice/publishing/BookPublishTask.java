package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private BookPublishRequestManager publishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;

    /**
     * Constructs a BookPublishTask object.
     */
    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                           PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao) {
        this.publishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    @Override
    public void run() {
//        Retrieve the next request from the RequestManager.
        BookPublishRequest publishRequest = publishRequestManager.getBookPublishRequestToProcess();
//        If there is no request waiting, immediately return without any other action
        if (publishRequest == null) {
            return;
        }
        String publishingId = publishRequest.getPublishingRecordId();

//        Adds an entry to the Publishing Status table with state IN_PROGRESS
//              Call to make a publishingstatus entry through the dao
//              Use the state IN_PROGRESS
        publishingStatusDao.setPublishingStatus(publishingId,
                PublishingRecordStatus.IN_PROGRESS, publishRequest.getBookId());

//        Performs formatting and conversion of the book
        KindleFormattedBook kindleFormattedBook = KindleFormatConverter.format(publishRequest);

//        Adds the new book to the CatalogItemVersion table
        CatalogItemVersion publishedBook;
        try {
            publishedBook = catalogDao.createOrUpdateBook(kindleFormattedBook);
        } catch (BookNotFoundException exception) {
//      If an exception is caught while processing, adds an item into the Publishing Status table with state FAILED and includes the exception message.
            PublishingStatusItem failedPublish = publishingStatusDao.setPublishingStatus(publishingId,
                    PublishingRecordStatus.FAILED,
                    publishRequest.getBookId(),
                    exception.getMessage());
            return;
        }

//        Adds an item to the Publishing Status table with state SUCCESSFUL if all the processing steps succeed.
        PublishingStatusItem successfulPublish = publishingStatusDao.setPublishingStatus(publishingId,
                PublishingRecordStatus.SUCCESSFUL, publishedBook.getBookId());
    }
}
