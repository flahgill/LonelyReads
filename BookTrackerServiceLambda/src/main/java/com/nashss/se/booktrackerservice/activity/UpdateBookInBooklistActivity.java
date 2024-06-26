package com.nashss.se.booktrackerservice.activity;

import com.nashss.se.booktrackerservice.activity.requests.UpdateBookInBooklistRequest;
import com.nashss.se.booktrackerservice.activity.results.UpdateBookInBooklistResult;

import com.nashss.se.booktrackerservice.converters.ModelConverterCarbon;
import com.nashss.se.booktrackerservice.dynamodb.BookDao;
import com.nashss.se.booktrackerservice.dynamodb.BooklistDao;
import com.nashss.se.booktrackerservice.dynamodb.CommentDao;
import com.nashss.se.booktrackerservice.dynamodb.models.Book;
import com.nashss.se.booktrackerservice.dynamodb.models.Booklist;
import com.nashss.se.booktrackerservice.metrics.MetricsPublisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class UpdateBookInBooklistActivity {
    /**
     * Implementation of the UpdateBookInBooklistActivity for the BookTrackerService's UpdateBooklist API.
     *
     * This API allows the customer to update their saved book's information. Like rating and percent complete.
     */
    private final Logger log = LogManager.getLogger();
    private final BooklistDao booklistDao;
    private final BookDao bookDao;
    private final MetricsPublisher metricsPublisher;
    private final CommentDao commentDao;

    /**
     * Instantiates a new UpdateBooklist object.
     *
     * @param booklistDao BooklistDao to access the booklist table.
     * @param bookDao BookDao to access the book table.
     * @param commentDao Commentdao class to access comment table
     * @param metricsPublisher MetricsPublisher to publish metrics.
     */
    @Inject
    public UpdateBookInBooklistActivity(BooklistDao booklistDao, BookDao bookDao, MetricsPublisher metricsPublisher,
                                        CommentDao commentDao) {
        this.booklistDao = booklistDao;
        this.bookDao = bookDao;
        this.metricsPublisher = metricsPublisher;
        this.commentDao = commentDao;
    }

    /**
     * This method handles the incoming request by retrieving the book, updating it,
     * and persisting it across booklists.
     * <p>
     * It then returns the updated book.
     * <p>
     * If the booklist does not exist, this should throw a BookNotFoundException.
     * <p>
     * If the provided booklist name or customer ID has invalid characters, throws an
     * InvalidAttributeValueException
     * <p>
     * If the request tries to update the customer ID,
     * this should throw an InvalidAttributeChangeException
     *
     * @param updateBookInBooklistRequest request object containing the booklist ID, booklist name, and customer ID
     *                              associated with it
     * @return updateBookInBooklistResult result object containing the API defined {@link BookModel}
     */
    public UpdateBookInBooklistResult handleRequest(final UpdateBookInBooklistRequest updateBookInBooklistRequest) {
        log.info("Received UpdateBookInBooklistRequest {}", updateBookInBooklistRequest);
        Book originalBook = bookDao.getBook(updateBookInBooklistRequest.getAsin());
        Book book = bookDao.getBook(updateBookInBooklistRequest.getAsin());
        //tries to update currently reading, leaves alone if null
        try {
            book.setCurrentlyReading(updateBookInBooklistRequest.isCurrentlyReading());
        } catch (NullPointerException e) {
            System.out.println(e);
        }
        //tries to update percent complete, leaves alone if null
        try {
            book.setPercentComplete(updateBookInBooklistRequest.getPercentComplete());
        } catch (NullPointerException e) {
            System.out.println(e);
        }
        //tries to update rating, leaves alone if null
        try {
            book.setRating(updateBookInBooklistRequest.getRating());
        } catch (NullPointerException e) {
            System.out.println(e);
        }

        List<Booklist> results = booklistDao.getAllBooklistsForUser(updateBookInBooklistRequest.getCustomerId());
        for (Booklist booklist : results) {
            List<Book> currentBooklist = booklist.getBooks();
            if (currentBooklist.contains(originalBook)) {
                List<Book> newlist = new ArrayList<>(currentBooklist);
                newlist.remove(originalBook);
                newlist.add(book);
                booklist.setBooks(newlist);
                booklistDao.saveBooklist(booklist);
            }
        }

        return UpdateBookInBooklistResult.builder()
                .withBook(new ModelConverterCarbon().toBookModel(book))
                .build();
    }

}
