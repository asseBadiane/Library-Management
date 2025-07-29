package com.library.bookservice.dto;


import com.library.bookservice.entity.Book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSearchDto {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String genre;
    private Integer publicationYear;
    private Integer availableCopies;
    private Book.BookStatus status;
    private Double score; // Elasticsearch relevance score

}