package com.nashss.se.booktrackerservice.activity.results;

import com.nashss.se.booktrackerservice.models.BooklistModel;

public class GetCurrentlyReadingResult {
    private final BooklistModel booklist;

    private GetCurrentlyReadingResult(BooklistModel booklist) {
        this.booklist = booklist;
    }

    public BooklistModel getBooklist() {
        return booklist;
    }

    @Override
    public String toString() {
        return "GetCurrentlyReadingResult{" +
                "booklist=" + booklist +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static GetCurrentlyReadingResult.Builder builder() {
        return new GetCurrentlyReadingResult.Builder();
    }

    public static class Builder {
        private BooklistModel booklist;

        public GetCurrentlyReadingResult.Builder withBooklist(BooklistModel booklist) {
            this.booklist = booklist;
            return this;
        }

        public GetCurrentlyReadingResult build() {
            return new GetCurrentlyReadingResult(booklist);
        }
    }
}
