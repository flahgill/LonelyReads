package com.nashss.se.booktrackerservice.activity;

import com.nashss.se.booktrackerservice.activity.requests.SearchBooklistsRequest;
import com.nashss.se.booktrackerservice.activity.results.SearchBooklistsResult;
import com.nashss.se.booktrackerservice.converters.ModelConverterCarbon;
import com.nashss.se.booktrackerservice.dynamodb.BooklistDao;

import com.nashss.se.booktrackerservice.dynamodb.models.Booklist;
import com.nashss.se.booktrackerservice.models.BooklistModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import javax.inject.Inject;

import static com.nashss.se.booktrackerservice.utils.NullUtils.ifNull;

/**
 * Implementation of the SearchBooklistActivity for the BookTrackerService's SearchBooklists API.
 * <p>
 * This API allows the customer to search for booklist by name or tag.
 */
public class SearchBooklistsActivity {

    private final Logger log = LogManager.getLogger();
    private final BooklistDao booklistDao;

    /**
     * Instantiates a new SearchBooklistActivity object.
     *
     * @param booklistDao BooklistDao to access the booklist table.
     */
    @Inject
    public SearchBooklistsActivity(BooklistDao booklistDao) {
        this.booklistDao = booklistDao;
    }

    /**
     * This method handles the incoming request by searching for booklist from the database.
     * <p>
     * It then returns the matching booklists, or an empty result list if none are found.
     *
     * @param searchBooklistsRequest request object containing the search criteria
     * @return searchBooklistsResult result object containing the booklists that match the
     * search criteria.
     */
    public SearchBooklistsResult handleRequest(final SearchBooklistsRequest searchBooklistsRequest) {
        log.info("Recieved SearchBooklistsRequest {}", searchBooklistsRequest);

        String criteria = ifNull(searchBooklistsRequest.getCriteria(), "");
        String[] criteriaArray = criteria.isBlank() ? new String[0] : criteria.split("\\s");

        List<Booklist> results = booklistDao.searchBooklists(criteriaArray);
        List<BooklistModel> booklistModels = new ModelConverterCarbon().toBookListModelList(results);

        return SearchBooklistsResult.builder()
                .withBooklists(booklistModels)
                .build();
    }
}
