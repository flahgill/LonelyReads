package com.nashss.se.booktrackerservice.dynamodb;

import com.nashss.se.booktrackerservice.dynamodb.models.Book;
import com.nashss.se.booktrackerservice.dynamodb.models.Booklist;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Accesses data for a book using {@link Book} to represent the model in DynamoDB.
 */

@Singleton
public class BookDao {

    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Instantiates an BookDao object.
     *
     * @param dynamoDbMapper the {@link DynamoDBMapper} used to interact with the book_table table
     */

    @Inject
    public BookDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDBMapper = dynamoDbMapper;
    }

    /**
     * Perform a search (via a "scan") of the book table for book with currentlyReading set to true.
     *
     * CurrentlyReading attribute is searched.
     *
     * @param isCurrentlyReading boolean for isCurrentlyReading
     * @return a List of Booklist objects that match the search criteria
     */
    public Booklist getCurrentlyReading(boolean isCurrentlyReading) {
        Map<String, AttributeValue> valueMap = new HashMap<>();
        valueMap.put(":currentlyReading", new AttributeValue().withBOOL(isCurrentlyReading));
        DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression()
                .withFilterExpression("currentlyReading = :currentlyReading")
                .withExpressionAttributeValues(valueMap);
        ScanResultPage<Book> bookResultPage = dynamoDBMapper.scanPage(Book.class, dynamoDBScanExpression);
        List<Book> results = bookResultPage.getResults();

        Booklist returnList = new Booklist();
        returnList.setBooks(results);
        returnList.setBookCount(results.size());
        returnList.setName("Currently Reading");
        return returnList;
    }
    /**
     * Returns the {@link Book} corresponding to the specified asin.
     *
     * @param asin the Book asin
     * @return the stored Book, or null if none was found.
     */
    public Book getBook(String asin) {
        return this.dynamoDBMapper.load(Book.class, asin);
    }

    /**
     * Saves a book to the Book DynamoDB table.
     * @param book the Book object to be saved
     */
    public void saveBook(Book book) {
        this.dynamoDBMapper.save(book);
    }

    /**
     * Perform a search (via a "scan") of the book table for books matching the given criteria.
     *
     * Both "bookName" and "tags" attributes are searched.
     * The criteria are an array of Strings. Each element of the array is search individually.
     * ALL elements of the criteria array must appear in the bookName or the asin (or both).
     * Searches are CASE SENSITIVE.
     *
     * @param criteria an array of String containing search criteria.
     * @return a List of Book objects that match the search criteria.
     */
    public List<Book> searchBooks(String[] criteria) {
        DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression();

        if (criteria.length > 0) {
            Map<String, AttributeValue> valueMap = new HashMap<>();
            String valueMapNamePrefix = ":c";

            StringBuilder titleFilterExpression = new StringBuilder();
            StringBuilder asinFilterExpression = new StringBuilder();

            for (int i = 0; i < criteria.length; i++) {
                valueMap.put(valueMapNamePrefix + i,
                        new AttributeValue().withS(criteria[i]));
                titleFilterExpression.append(
                        filterExpressionPart("title", valueMapNamePrefix, i));
                asinFilterExpression.append(
                        filterExpressionPart("asin", valueMapNamePrefix, i));
            }

            dynamoDBScanExpression.setExpressionAttributeValues(valueMap);
            dynamoDBScanExpression.setFilterExpression(
                    "(" + titleFilterExpression + ") or (" + asinFilterExpression + ")");
        }

        return this.dynamoDBMapper.scan(Book.class, dynamoDBScanExpression);
    }

    /**
     * Helper method for searchBooks method.
     */
    private StringBuilder filterExpressionPart(String target, String valueMapNamePrefix, int position) {
        String possiblyAnd = position == 0 ? "" : "and ";
        return new StringBuilder()
                .append(possiblyAnd)
                .append("contains(")
                .append(target)
                .append(", ")
                .append(valueMapNamePrefix).append(position)
                .append(") ");
    }
}
