package com.nashss.se.booktrackerservice.activity;

import com.nashss.se.booktrackerservice.activity.requests.RemoveBooklistRequest;
import com.nashss.se.booktrackerservice.activity.results.RemoveBooklistResult;
import com.nashss.se.booktrackerservice.converters.ModelConverterCarbon;
import com.nashss.se.booktrackerservice.dynamodb.BooklistDao;
import com.nashss.se.booktrackerservice.dynamodb.models.Booklist;
import com.nashss.se.booktrackerservice.models.BooklistModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * Implementation of the RemoveBooklistActivity for the BookPlaylist API.
 *
 * This API allows the customer to remove one of their saved booklists.
 */
public class RemoveBooklistActivity {
    private final Logger log = LogManager.getLogger();
    private final BooklistDao booklistDao;

    /**
     * Instantiates a new RemoveBooklistActivity object.
     *
     * @param booklistDao BooklistDao to access the booklist table.
     */
    @Inject
    public RemoveBooklistActivity(BooklistDao booklistDao) {
        this.booklistDao = booklistDao;
    }

    /**
     * This method handles the incoming request by removing the booklist from the database.
     * <p>
     * If the booklist does not exist, this should throw a BooklistNotFoundException.
     *
     * @param removeBooklistRequest request object containing the booklist ID
     * @return removeBooklistResult result object containing the API defined {@link BooklistModel}
     */
    public RemoveBooklistResult handleRequest(final RemoveBooklistRequest removeBooklistRequest) {
        log.info("Received RemoveBooklistRequest {}", removeBooklistRequest);
        String requestedId = removeBooklistRequest.getId();
        Booklist booklist = booklistDao.removeBooklist(requestedId);
        BooklistModel booklistModel = null;

        if (booklist == null) {
            log.info("Booklist with id {} not found", requestedId);
        } else {
            booklistModel = new ModelConverterCarbon().toBooklistModel(booklist);
        }

        return  RemoveBooklistResult.builder()
                .withBooklist(booklistModel)
                .build();
    }
}
