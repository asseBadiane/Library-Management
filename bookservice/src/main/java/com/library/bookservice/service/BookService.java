package com.library.bookservice.service;


import com.library.bookservice.dto.BookDto;
import com.library.bookservice.entity.Book;
import com.library.bookservice.entity.Book.BookStatus;
import com.library.bookservice.exception.BookNotFoundException;
import com.library.bookservice.mapper.BookMapper;
import com.library.bookservice.repository.BookRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookEventService bookEventService;
    private final BookSearchService bookSearchService;
    
    @Autowired
    public BookService(BookRepository bookRepository, BookMapper bookMapper, 
                      BookEventService bookEventService, BookSearchService bookSearchService) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.bookEventService = bookEventService;
        this.bookSearchService = bookSearchService;
    }
    
    public BookDto createBook(BookDto bookDto) {
        logger.info("Creating new book with ISBN: {}", bookDto.getIsbn());
        
        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }
        
        Book book = bookMapper.toEntity(bookDto);
        book.setAvailableCopies(book.getTotalCopies());
        
        Book savedBook = bookRepository.save(book);
        logger.info("Book created successfully with ID: {}", savedBook.getId());
        
        // Index in Elasticsearch
        bookSearchService.indexBook(savedBook);
        
        // Publish event
        String correlationId = UUID.randomUUID().toString();
        bookEventService.publishBookEvent("BOOK_ADDED", savedBook, correlationId);
        
        return bookMapper.toDto(savedBook);
    }
    
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        logger.debug("Fetching book with ID: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        return bookMapper.toDto(book);
    }
    
    @Transactional(readOnly = true)
    public BookDto getBookByIsbn(String isbn) {
        logger.debug("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
        return bookMapper.toDto(book);
    }
    
    @Transactional(readOnly = true)
    public Page<BookDto> getAllBooks(Pageable pageable) {
        logger.debug("Fetching all books with pagination");
        return bookRepository.findAll(pageable).map(bookMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public List<BookDto> getBooksByAuthor(String author) {
        logger.debug("Fetching books by author: {}", author);
        return bookRepository.findByAuthorIgnoreCase(author)
            .stream()
            .map(bookMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<BookDto> getBooksByGenre(String genre) {
        logger.debug("Fetching books by genre: {}", genre);
        return bookRepository.findByGenreIgnoreCase(genre)
            .stream()
            .map(bookMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<BookDto> getAvailableBooks() {
        logger.debug("Fetching available books");
        return bookRepository.findAvailableBooks()
            .stream()
            .map(bookMapper::toDto)
            .toList();
    }
    
    public BookDto updateBook(Long id, BookDto bookDto) {
        logger.info("Updating book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        // Update fields
        existingBook.setTitle(bookDto.getTitle());
        existingBook.setAuthor(bookDto.getAuthor());
        existingBook.setDescription(bookDto.getDescription());
        existingBook.setGenre(bookDto.getGenre());
        existingBook.setPublicationYear(bookDto.getPublicationYear());
        
        // Handle inventory changes
        if (!existingBook.getTotalCopies().equals(bookDto.getTotalCopies())) {
            int difference = bookDto.getTotalCopies() - existingBook.getTotalCopies();
            existingBook.setTotalCopies(bookDto.getTotalCopies());
            existingBook.setAvailableCopies(existingBook.getAvailableCopies() + difference);
        }
        
        Book updatedBook = bookRepository.save(existingBook);
        logger.info("Book updated successfully with ID: {}", updatedBook.getId());
        
        // Update in Elasticsearch
        bookSearchService.indexBook(updatedBook);
        
        // Publish event
        String correlationId = UUID.randomUUID().toString();
        bookEventService.publishBookEvent("BOOK_UPDATED", updatedBook, correlationId);
        
        return bookMapper.toDto(updatedBook);
    }
    
    public void deleteBook(Long id) {
        logger.info("Deleting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        bookRepository.delete(book);
        logger.info("Book deleted successfully with ID: {}", id);
        
        // Remove from Elasticsearch
        bookSearchService.deleteBook(id);
        
        // Publish event
        String correlationId = UUID.randomUUID().toString();
        bookEventService.publishBookEvent("BOOK_DELETED", book, correlationId);
    }
    
    public BookDto borrowBook(Long id) {
        logger.info("Processing borrow request for book ID: {}", id);
        
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        book.borrowCopy();
        Book updatedBook = bookRepository.save(book);
        logger.info("Book borrowed successfully. Available copies: {}", updatedBook.getAvailableCopies());
        
        // Update in Elasticsearch
        bookSearchService.indexBook(updatedBook);
        
        // Publish event
        String correlationId = UUID.randomUUID().toString();
        bookEventService.publishBookEvent("BOOK_STATUS_CHANGED", updatedBook, correlationId);
        
        return bookMapper.toDto(updatedBook);
    }
    
    public BookDto returnBook(Long id) {
        logger.info("Processing return request for book ID: {}", id);
        
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        book.returnCopy();
        Book updatedBook = bookRepository.save(book);
        logger.info("Book returned successfully. Available copies: {}", updatedBook.getAvailableCopies());
        
        // Update in Elasticsearch
        bookSearchService.indexBook(updatedBook);
        
        // Publish event
        String correlationId = UUID.randomUUID().toString();
        bookEventService.publishBookEvent("BOOK_STATUS_CHANGED", updatedBook, correlationId);
        
        return bookMapper.toDto(updatedBook);
    }
    
    @Transactional(readOnly = true)
    public Page<BookDto> searchBooks(String query, Pageable pageable) {
        logger.debug("Searching books with query: {}", query);
        return bookRepository.searchBooks(query, pageable).map(bookMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public long getAvailableBooksCount() {
        return bookRepository.countAvailableBooks();
    }
    
    @Transactional(readOnly = true)
    public List<Object[]> getBookStatsByGenre() {
        return bookRepository.countBooksByGenre();
    }

    public void updateBookStatus(Long id, BookStatus status) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        
        book.setStatus(status);
        bookRepository.save(book);
    }
}