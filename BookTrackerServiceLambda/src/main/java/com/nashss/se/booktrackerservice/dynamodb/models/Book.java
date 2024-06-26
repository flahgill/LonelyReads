package com.nashss.se.booktrackerservice.dynamodb.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Objects;

/**
 * Represents a Book in the Books table.
 */

@DynamoDBTable(tableName = "Books")
public class Book {

    private String asin;
    private String title;
    private String author;
    private String genre;
    private String thumbnail;
    private Integer rating;
    private Boolean currentlyReading;
    private Integer percentComplete;
    private Integer pageCount;

    @DynamoDBHashKey(attributeName = "asin")
    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBAttribute(attributeName = "author")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @DynamoDBAttribute(attributeName = "genre")
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @DynamoDBAttribute(attributeName = "thumbnail")
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @DynamoDBAttribute(attributeName = "rating")
    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    @DynamoDBAttribute(attributeName = "currentlyReading")
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    public Boolean isCurrentlyReading() {
        return currentlyReading;
    }

    public void setCurrentlyReading(Boolean currentlyReading) {
        this.currentlyReading = currentlyReading;
    }

    @DynamoDBAttribute(attributeName = "percentComplete")
    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }

    @DynamoDBAttribute(attributeName = "pageCount")
    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asin, title, author);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book that = (Book) o;
        return asin.equals(that.asin) &&
                title.equals(that.title) &&
                author.equals(that.author);
    }

    @Override
    public String toString() {
        return "Book{" +
                "asin='" + asin + '\'' +
                ", title=" + title +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", rating='" + rating + '\'' +
                ", currentlyReading='" + currentlyReading + '\'' +
                ", percentageComplete='" + percentComplete + '\'' +
                ", pageCount='" + pageCount + '\'' +
                '}';
    }

}
