package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
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
     * @param bookId ID associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }


    /**
     * Adds the provided KindleFormattedBook to the catalog.
     * Will be added as a new book or the newest version if the book was previously published.
     * Throws {@link BookNotFoundException} if trying to update a book that does not exist in the catalog.
     * @param formattedBook The properly formatted book to be published to the catalog
     * @return The newly published version of the book
     */
    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook formattedBook) {
        String bookId = formattedBook.getBookId();
        int versionNumber;

        if (bookId == null) {
            bookId = KindlePublishingUtils.generateBookId();
            versionNumber = 1;
        } else {
            validateBookExists(formattedBook.getBookId());
            CatalogItemVersion currentVersion = getLatestVersionOfBook(bookId);
            versionNumber = currentVersion.getVersion() + 1;
            removeBookFromCatalog(bookId);
        }

        CatalogItemVersion newVersion = new CatalogItemVersion();
        newVersion.setBookId(bookId);
        newVersion.setVersion(versionNumber);
        newVersion.setTitle(formattedBook.getTitle());
        newVersion.setAuthor(formattedBook.getAuthor());
        newVersion.setText(formattedBook.getText());
        newVersion.setGenre(formattedBook.getGenre());
        newVersion.setInactive(false);
        saveBookInCatalog(newVersion);

        return newVersion;
    }

    private void saveBookInCatalog(CatalogItemVersion bookToSave) {
        dynamoDbMapper.save(bookToSave);
    }

    /**
     * Sets the catalog item corresponding to the provided bookId as inactive, thus removing it from the catalog.
     * Throws a BookNotFoundException if there is no active version for the bookId provided
     * @param bookId the ID of the book that is to be removed
     * @return The CatalogItem marked as inactive
     */
    public CatalogItemVersion removeBookFromCatalog(String bookId) {
        CatalogItemVersion bookToRemove;

        bookToRemove = getBookFromCatalog(bookId);

        bookToRemove.setInactive(true);
        dynamoDbMapper.save(bookToRemove);

        return bookToRemove;
    }

    /**
     * Checks to ensure a Book corresponding to the provided ID exists in the catalog.
     * Does not check whether book is active or inactive.
     * Throws a BookNotFoundException if no book was found with the provided ID.
     * @param bookId ID to search for in the catalog
     */
    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression<CatalogItemVersion>()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
