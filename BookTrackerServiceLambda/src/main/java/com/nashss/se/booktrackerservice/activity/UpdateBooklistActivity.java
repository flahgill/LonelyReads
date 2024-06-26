package com.nashss.se.booktrackerservice.activity;

import com.nashss.se.booktrackerservice.activity.requests.UpdateBooklistRequest;
import com.nashss.se.booktrackerservice.activity.results.UpdateBooklistResult;

import com.nashss.se.booktrackerservice.converters.ModelConverterCarbon;
import com.nashss.se.booktrackerservice.dynamodb.BooklistDao;
import com.nashss.se.booktrackerservice.dynamodb.models.Booklist;
import com.nashss.se.booktrackerservice.exceptions.InvalidAttributeValueException;
import com.nashss.se.booktrackerservice.metrics.MetricsConstants;
import com.nashss.se.booktrackerservice.metrics.MetricsPublisher;
import com.nashss.se.booktrackerservice.models.BooklistModel;

import com.nashss.se.projectresources.music.playlist.servic.util.MusicPlaylistServiceUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * Implementation of the UpdateBooklistActivity for the BookTrackerService's UpdateBooklist API.
 *
 * This API allows the customer to update their saved booklist's information.
 */
public class UpdateBooklistActivity {

    private final Logger log = LogManager.getLogger();
    private final BooklistDao booklistDao;
    private final MetricsPublisher metricsPublisher;

    /**
     * Instantiates a new UpdateBooklist object.
     *
     * @param booklistDao BooklistDao to access the booklist table.
     * @param metricsPublisher MetricsPublisher to publish metrics.
     */

    @Inject
    public UpdateBooklistActivity(BooklistDao booklistDao, MetricsPublisher metricsPublisher) {
        this.booklistDao = booklistDao;
        this.metricsPublisher = metricsPublisher;
    }

    /**
     * This method handles the incoming request by retrieving the booklist, updating it,
     * and persisting the booklist.
     * <p>
     * It then returns the updated booklist.
     * <p>
     * If the booklist does not exist, this should throw a BooklistNotFoundException.
     * <p>
     * If the provided booklist name or customer ID has invalid characters, throws an
     * InvalidAttributeValueException
     * <p>
     * If the request tries to update the customer ID,
     * this should throw an InvalidAttributeChangeException
     *
     * @param updateBooklistRequest request object containing the playlist ID, playlist name, and customer ID
     *                              associated with it
     * @return updateBooklistResult result object containing the API defined {@link BooklistModel}
     */

    public UpdateBooklistResult handleRequest(final UpdateBooklistRequest updateBooklistRequest) {
        log.info("Recieved UpdateBooklistRequest {}", updateBooklistRequest);

        if (!MusicPlaylistServiceUtils.isValidString(updateBooklistRequest.getName())) {
            publishExceptionMetrics(true, false);
            throw new InvalidAttributeValueException("Booklist name [" + updateBooklistRequest.getName() +
                    "] contains illegal characters");
        }

        Booklist booklist = booklistDao.getBooklist(updateBooklistRequest.getId());

        if (!booklist.getCustomerId().equals(updateBooklistRequest.getCustomerId())) {
            publishExceptionMetrics(false, true);
            throw new SecurityException("You must own a booklist to update it");
        }

        booklist.setName(updateBooklistRequest.getName());
        booklist = booklistDao.saveBooklist(booklist);

        publishExceptionMetrics(false, false);
        return UpdateBooklistResult.builder()
                .withBooklist(new ModelConverterCarbon().toBooklistModel(booklist))
                .build();
    }

    /**
     * Helper method to publish exception metrics.
     * @param isInvalidAttributeValue indicates whether InvalidAttributeValueException is thrown
     * @param isInvalidAttributeChange indicates whether InvalidAttributeChangeException is thrown
     */
    private void publishExceptionMetrics(final boolean isInvalidAttributeValue,
                                         final boolean isInvalidAttributeChange) {
        metricsPublisher.addCount(MetricsConstants.UPDATEBOOKLIST_INVALIDATTRIBUTEVALUE_COUNT,
                isInvalidAttributeValue ? 1 : 0);
        metricsPublisher.addCount(MetricsConstants.UPDATEBOOKLIST_INVALIDATTRIBUTECHANGE_COUNT,
                isInvalidAttributeChange ? 1 : 0);
    }
}
