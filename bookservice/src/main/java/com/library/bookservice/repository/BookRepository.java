package com.library.bookservice.repository;


import com.library.bookservice.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByAuthorIgnoreCase(String author);
    
    List<Book> findByGenreIgnoreCase(String genre);
    
    List<Book> findByStatus(Book.BookStatus status);
    
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();
    
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0")
    long countAvailableBooks();
    
    @Query("SELECT b.genre, COUNT(b) FROM Book b GROUP BY b.genre")
    List<Object[]> countBooksByGenre();
    
    boolean existsByIsbn(String isbn);
}