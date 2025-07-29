package com.library.notificationservice.dto;


import lombok.Data;



@Data
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String status;
}