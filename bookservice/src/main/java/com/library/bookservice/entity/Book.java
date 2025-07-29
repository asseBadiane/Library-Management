package com.library.bookservice.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
// import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
// @Document(indexName = "books")
public class Book {
    
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @NotBlank(message = "Author is required")
    @Column(nullable = false)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Column(nullable = false, unique = true)
    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Text)
    private String description;

    @NotBlank(message = "Genre is required")
    @Column(nullable = false)
    @Field(type = FieldType.Keyword)
    private String genre;

    @NotNull(message = "Publication year is required")
    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private Integer publicationYear;

    @NotNull(message = "Total copies is required")
    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private Integer totalCopies;

    @NotNull(message = "Available copies is required")
    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private Integer availableCopies;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Field(type = FieldType.Keyword)
    private BookStatus status = BookStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Book() {}

    public Book(String title, String author, String isbn, String description, 
                String genre, Integer publicationYear, Integer totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { 
        this.availableCopies = availableCopies;
        updateStatus();
    }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business logic methods
    public void borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            updateStatus();
        } else {
            throw new IllegalStateException("No copies available for borrowing");
        }
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
            updateStatus();
        } else {
            throw new IllegalStateException("All copies are already returned");
        }
    }

    private void updateStatus() {
        this.status = availableCopies > 0 ? BookStatus.AVAILABLE : BookStatus.UNAVAILABLE;
    }

    // public enum BookStatus {
    //     AVAILABLE, UNAVAILABLE, MAINTENANCE
    // }
    public enum BookStatus {
    AVAILABLE, UNAVAILABLE, BORROWED, RESERVED
}
}