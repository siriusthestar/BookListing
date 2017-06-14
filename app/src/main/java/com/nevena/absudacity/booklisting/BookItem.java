package com.nevena.absudacity.booklisting;


import java.util.List;


// holds information about one book
public class BookItem {

    private String title;
    private List authors;

    public BookItem(String title, List authors) {
        this.title = title;
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getAuthors() {
        return authors;
    }
}
