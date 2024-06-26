package com.nashss.se.booktrackerservice.activity;

import com.nashss.se.booktrackerservice.activity.requests.AddBookToBooklistRequest;
import com.nashss.se.booktrackerservice.activity.results.AddBookToBooklistResult;
import com.nashss.se.booktrackerservice.converters.ModelConverterCarbon;
import com.nashss.se.booktrackerservice.dynamodb.BookDao;
import com.nashss.se.booktrackerservice.dynamodb.BooklistDao;
import com.nashss.se.booktrackerservice.dynamodb.models.Book;
import com.nashss.se.booktrackerservice.dynamodb.models.Booklist;
import com.nashss.se.booktrackerservice.exceptions.*;
import com.nashss.se.booktrackerservice.models.BookModel;
import com.nashss.se.booktrackerservice.test.helper.BookTestHelper;
import com.nashss.se.booktrackerservice.test.helper.BooklistTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class AddBookToBooklistActivityTest {
    @Mock
    private BooklistDao booklistDao;

    @Mock
    private BookDao bookDao;

    private AddBookToBooklistActivity addBookToBooklistActivity;
    private ModelConverterCarbon modelConverter;

    @BeforeEach
    void setup() {
        openMocks(this);
        this.addBookToBooklistActivity = new AddBookToBooklistActivity(booklistDao, bookDao);
        this.modelConverter = new ModelConverterCarbon();
    }

    @Test
    void handleRequest_validRequest_addsBookToEndOfBooklist() throws Exception {
        // GIVEN
        // a non-empty booklist
        Booklist originalBooklist = BooklistTestHelper.generateBooklist();
        String booklistId = originalBooklist.getId();
        String customerId = originalBooklist.getCustomerId();

        // The .generateBooklist() method generates a booklist with a single asin
        // Assigning this to a variable to be used in mockito when() statement (@Ln 79)
        String existingAsin = originalBooklist.getBooks().get(0).getAsin();

        // The new book to add to the booklist
        Book bookToAdd = BookTestHelper.generateBook(2);
        String addedAsin = bookToAdd.getAsin();

        // Since we use a List<String> for asins instead of List<Book>...
        // Need to create a Book object for the existing asin (@Ln 62) in the booklist.
        // If wanting to expand this test for a booklist containing more than one existing asin...
        // Need to create Book object for each additional asin
        Book existingBookInBooklist = BookTestHelper.generateBook(1);

        when(booklistDao.getBooklist(booklistId)).thenReturn(originalBooklist);
        when(booklistDao.saveBooklist(originalBooklist)).thenReturn(originalBooklist);
        when(bookDao.getBook(addedAsin)).thenReturn(bookToAdd);
        when(bookDao.getBook(existingAsin)).thenReturn(existingBookInBooklist);

        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withAsin(addedAsin)
                .withId(booklistId)
                .withCustomerId(customerId)
                .build();

        // WHEN
        AddBookToBooklistResult result = addBookToBooklistActivity.handleRequest(request);

        // THEN
        verify(booklistDao).saveBooklist(originalBooklist);

        assertEquals(2, result.getBookList().size());
        BookModel secondBook = result.getBookList().get(1);
        BookTestHelper.assertBookEqualsBookModel(bookToAdd, secondBook);
    }

    @Test
    public void handleRequest_noMatchingBooklistId_throwsBooklistNotFoundException() {
        // GIVEN
        String booklistId = "missing id";
        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin("asin")
                .withCustomerId("doesn't matter")
                .build();
        when(booklistDao.getBooklist(booklistId)).thenThrow(new BooklistNotFoundException());

        // WHEN + THEN
        assertThrows(BooklistNotFoundException.class, () -> addBookToBooklistActivity.handleRequest(request));
    }

    @Test
    public void handleRequest_noMatchingBook_throwsBookNotFoundException() {
        // GIVEN
        Booklist booklist = BooklistTestHelper.generateBooklist();

        String booklistId = booklist.getId();
        String customerId = booklist.getCustomerId();
        String asin = "nonexistent asin";
        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(asin)
                .withCustomerId(customerId)
                .build();

        // WHEN
        when(booklistDao.getBooklist(booklistId)).thenReturn(booklist);
        when(bookDao.getBook(asin)).thenThrow(new BookNotFoundException());

        // THEN
        assertThrows(BookNotFoundException.class, () -> addBookToBooklistActivity.handleRequest(request));
    }

    @Test
    public void handleRequest_similarSearches_returnSameBookFromAPI() {
        // GIVEN - A booklist and two requests with similar search terms to query to the Google Book API
        Booklist booklist = BooklistTestHelper.generateBooklist();
        String booklistId = booklist.getId();
        String customerId = booklist.getCustomerId();

        String titleSearch = "game of thrones book one";
        String otherSearch = "got book one";

        // Title
        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(titleSearch)
                .withCustomerId(customerId)
                .build();

        when(booklistDao.getBooklist(booklistId)).thenReturn(booklist);
        when(booklistDao.saveBooklist(booklist)).thenReturn(booklist);

        // WHEN - Calling handleRequest() with both requests
        AddBookToBooklistResult result = addBookToBooklistActivity.handleRequest(request);

        verify(booklistDao).saveBooklist(booklist);

        // Other
        AddBookToBooklistRequest request2 = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(otherSearch)
                .withCustomerId(customerId)
                .build();

        AddBookToBooklistResult result2 = addBookToBooklistActivity.handleRequest(request2);

        // THEN - The same book should be returned
        // Tests consistency of the API
        assertEquals(result.getBookList().get(1), result2.getBookList().get(1));
    }

    @Test
    public void handleRequest_bookExistsInDynamoDB_returnsExistingBook() {
        // GIVEN - A booklist and a request for an existing book in DynamoDB
        Booklist booklist = BooklistTestHelper.generateBooklist();
        String booklistId = booklist.getId();
        String customerId = booklist.getCustomerId();

        String isbn = "9780553897845";

        Book book = new Book();
        book.setAsin(isbn);
        book.setTitle("testBook");
        book.setAuthor("testBookAuthor");

        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(isbn)
                .withCustomerId(customerId)
                .build();

        when(booklistDao.getBooklist(booklistId)).thenReturn(booklist);
        when(booklistDao.saveBooklist(booklist)).thenReturn(booklist);
        when(bookDao.getBook(request.getAsin())).thenReturn(book);

        // WHEN - Calling handleRequest() with the request
        AddBookToBooklistResult result = addBookToBooklistActivity.handleRequest(request);


        verify(booklistDao).saveBooklist(booklist);
        verify(booklistDao).saveBooklist(booklist);
        verify(bookDao).getBook(request.getAsin());

        // THEN - The existing book in DynamoDB will be returned instead of pinging the Google Book API.
        // If a book already exists in DynamoDB from a previous request, return it instead of pinging the API again.
        assertEquals(modelConverter.toBookModel(book), result.getBookList().get(1));
    }

    @Test
    public void handleRequest_withSearchTerm_savesBookToBooklist() {
        // GIVEN - A booklist with a request for a book using a search term
        Booklist booklist = BooklistTestHelper.generateBooklist();
        String booklistId = booklist.getId();
        String customerId = booklist.getCustomerId();

        String titleSearch = "dune";

        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(titleSearch)
                .withCustomerId(customerId)
                .build();

        when(booklistDao.getBooklist(booklistId)).thenReturn(booklist);
        when(booklistDao.saveBooklist(booklist)).thenReturn(booklist);

        // WHEN - Calling handleRequest() with the request
        AddBookToBooklistResult result = addBookToBooklistActivity.handleRequest(request);

        // THEN - The book created from deserializing Google Book API response is saved
        verify(booklistDao).saveBooklist(booklist);
        assertEquals(2, booklist.getBookCount());
    }

    @Test
    public void handleRequest_invalidSearchTerm_throwsGoogleBookAPISearchException() {
        // GIVEN - A booklist and a request with an empty search term
        Booklist booklist = BooklistTestHelper.generateBooklist();
        String booklistId = booklist.getId();
        String customerId = booklist.getCustomerId();

        String titleSearch = "";

        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(titleSearch)
                .withCustomerId(customerId)
                .build();

        when(booklistDao.getBooklist(booklistId)).thenReturn(booklist);

        assertThrows(GoogleBookAPISearchException.class, () -> addBookToBooklistActivity.handleRequest(request));
    }

    @Test
    public void handleRequest_noResults_throwsGoogleBookAPISearchException() {
        Booklist booklist = BooklistTestHelper.generateBooklist();
        String booklistId = booklist.getId();
        String customerId = booklist.getCustomerId();

        // Can't find a search term that returns no results. Need to create this test in different package to mock the api
        String titleSearch = "";

        AddBookToBooklistRequest request = AddBookToBooklistRequest.builder()
                .withId(booklistId)
                .withAsin(titleSearch)
                .withCustomerId(customerId)
                .build();

        when(booklistDao.getBooklist(booklistId)).thenReturn(booklist);

        assertThrows(GoogleBookAPISearchException.class, () -> addBookToBooklistActivity.handleRequest(request));
    }
}
