package com.library.bookservice.controller;

import com.google.common.base.Optional;
import com.library.bookservice.dto.BookDto;
import com.library.bookservice.dto.BookSearchDto;
import com.library.bookservice.entity.Book;
import com.library.bookservice.entity.Book.BookStatus;
import com.library.bookservice.repository.BookRepository;
import com.library.bookservice.service.BookService;
import com.library.bookservice.service.BookSearchService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
// @Tag(name = "Book Management", description = "APIs for manakging books in the library")
public class BookController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    
    private final BookService bookService;
    private final BookSearchService bookSearchService;
    private final BookRepository bookRepository;
    
    @Autowired
    public BookController(BookService bookService, BookSearchService bookSearchService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookSearchService = bookSearchService;
        this.bookRepository = bookRepository;
    }
    
    @PostMapping
    // @Operation(summary = "Create a new book", description = "Add a new book to the library catalog")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "201", description = "Book created successfully"),
    //     @ApiResponse(responseCode = "400", description = "Invalid input data"),
    //     @ApiResponse(responseCode = "409", description = "Book with ISBN already exists")
    // })
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        logger.info("Creating book with title: {}", bookDto.getTitle());
        BookDto createdBook = bookService.createBook(bookDto);
        logger.info("Book created successfully with ID: {}", createdBook.getId());
        
        MDC.clear();
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    // @Operation(summary = "Get book by ID", description = "Retrieve a book by its unique identifier")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Book found"),
    //     @ApiResponse(responseCode = "404", description = "Book not found")
    // })
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        logger.debug("Fetching book with ID: {}", id);
        BookDto book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }
    
    @GetMapping("/isbn/{isbn}")
    // @Operation(summary = "Get book by ISBN", description = "Retrieve a book by its ISBN")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Book found"),
    //     @ApiResponse(responseCode = "404", description = "Book not found")
    // })
    public ResponseEntity<BookDto> getBookByIsbn(@PathVariable String isbn) {
        logger.debug("Fetching book with ISBN: {}", isbn);
        BookDto book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }
    
    @GetMapping
    // @Operation(summary = "Get all books", description = "Retrieve all books with pagination")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Books retrieved successfully")
    // })
    public ResponseEntity<Page<BookDto>> getAllBooks(
        @PageableDefault(size = 20) Pageable pageable) {
        logger.debug("Fetching all books with pagination");
        Page<BookDto> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/author/{author}")
    // @Operation(summary = "Get books by author", description = "Retrieve books by a specific author")
    public ResponseEntity<List<BookDto>> getBooksByAuthor(@PathVariable String author) {
        logger.debug("Fetching books by author: {}", author);
        List<BookDto> books = bookService.getBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/genre/{genre}")
    // @Operation(summary = "Get books by genre", description = "Retrieve books by a specific genre")
    public ResponseEntity<List<BookDto>> getBooksByGenre(@PathVariable String genre) {
        logger.debug("Fetching books by genre: {}", genre);
        List<BookDto> books = bookService.getBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/available")
    // @Operation(summary = "Get available books", description = "Retrieve all books that are currently available for borrowing")
    public ResponseEntity<List<BookDto>> getAvailableBooks() {
        logger.debug("Fetching available books");
        List<BookDto> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }
    
    @PutMapping("/{id}")
    // @Operation(summary = "Update book", description = "Update an existing book")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Book updated successfully"),
    //     @ApiResponse(responseCode = "404", description = "Book not found"),
    //     @ApiResponse(responseCode = "400", description = "Invalid input data")
    // })
    public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        logger.info("Updating book with ID: {}", id);
        BookDto updatedBook = bookService.updateBook(id, bookDto);
        logger.info("Book updated successfully with ID: {}", id);
        
        MDC.clear();
        return ResponseEntity.ok(updatedBook);
    }

    // @PutMapping("/api/books/{id}/status")
    // public ResponseEntity<?> updateBookStatus(
    //         @PathVariable Long id,
    //         @RequestParam BookStatus status) {
    //     bookService.updateBookStatus(id, status);
    //     return ResponseEntity.ok().build();
    // }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") BookStatus status) {
        java.util.Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
        Book book = bookOpt.get();
        // No need to validate status, Spring will reject invalid values before this point
        book.setStatus(status);
        bookRepository.save(book);
        return ResponseEntity.ok().build();
    }
    
    
    @DeleteMapping("/{id}")
    // @Operation(summary = "Delete book", description = "Delete a book from the catalog")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
    //     @ApiResponse(responseCode = "404", description = "Book not found")
    // })
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        logger.info("Deleting book with ID: {}", id);
        bookService.deleteBook(id);
        logger.info("Book deleted successfully with ID: {}", id);
        
        MDC.clear();
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/borrow")
    // @Operation(summary = "Borrow a book", description = "Mark a book as borrowed (reduce available copies)")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Book borrowed successfully"),
    //     @ApiResponse(responseCode = "404", description = "Book not found"),
    //     @ApiResponse(responseCode = "400", description = "No copies available")
    // })
    public ResponseEntity<BookDto> borrowBook(@PathVariable Long id) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        logger.info("Processing borrow request for book ID: {}", id);
        BookDto book = bookService.borrowBook(id);
        logger.info("Book borrowed successfully. Book ID: {}", id);
        
        MDC.clear();
        return ResponseEntity.ok(book);
    }
    
    @PostMapping("/{id}/return")
    // @Operation(summary = "Return a book", description = "Mark a book as returned (increase available copies)")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Book returned successfully"),
    //     @ApiResponse(responseCode = "404", description = "Book not found"),
    //     @ApiResponse(responseCode = "400", description = "All copies are already returned")
    // })
    public ResponseEntity<BookDto> returnBook(@PathVariable Long id) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        logger.info("Processing return request for book ID: {}", id);
        BookDto book = bookService.returnBook(id);
        logger.info("Book returned successfully. Book ID: {}", id);
        
        MDC.clear();
        return ResponseEntity.ok(book);
    }
    
    // @GetMapping("/search")
    // @Operation(summary = "Search books", description = "Search books using database query")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Search completed successfully")
    // })
    // public ResponseEntity<Page<BookDto>> searchBooks(
    //     @RequestParam @Parameter(description = "Search query") String query,
    //     @PageableDefault(size = 20) Pageable pageable) {
    //     logger.debug("Searching books with query: {}", query);
    //     Page<BookDto> books = bookService.searchBooks(query, pageable);
    //     return ResponseEntity.ok(books);
    // }
    
    // @GetMapping("/search/elasticsearch")
    // @Operation(summary = "Search books with Elasticsearch", description = "Advanced search using Elasticsearch")
    // public ResponseEntity<List<BookSearchDto>> searchBooksElasticsearch(
    //     @RequestParam @Parameter(description = "Search query") String query) {
    //     logger.debug("Elasticsearch search with query: {}", query);
    //     List<BookSearchDto> books = bookSearchService.searchBooks(query);
    //     return ResponseEntity.ok(books);
    // }
    
    // @GetMapping("/search/elasticsearch/paginated")
    // @Operation(summary = "Search books with Elasticsearch and pagination")
    // public ResponseEntity<Page<BookSearchDto>> searchBooksElasticsearchPaginated(
    //     @RequestParam @Parameter(description = "Search query") String query,
    //     @PageableDefault(size = 20) Pageable pageable) {
    //     logger.debug("Elasticsearch paginated search with query: {}", query);
    //     Page<BookSearchDto> books = bookSearchService.searchBooksWithPagination(query, pageable);
    //     return ResponseEntity.ok(books);
    // }
    
    // @GetMapping("/search/advanced")
    // @Operation(summary = "Advanced search", description = "Advanced search with multiple filters")
    // public ResponseEntity<List<BookSearchDto>> advancedSearch(
    //     @RequestParam(required = false) String title,
    //     @RequestParam(required = false) String author,
    //     @RequestParam(required = false) String genre,
    //     @RequestParam(required = false) Integer yearFrom,
    //     @RequestParam(required = false) Integer yearTo,
    //     @RequestParam(required = false) Boolean availableOnly) {
    //     logger.debug("Advanced search with filters");
    //     List<BookSearchDto> books = bookSearchService.advancedSearch(
    //         title, author, genre, yearFrom, yearTo, availableOnly);
    //     return ResponseEntity.ok(books);
    // }
    
    // @GetMapping("/stats/available-count")
    // @Operation(summary = "Get available books count", description = "Get the count of available books")
    // public ResponseEntity<Long> getAvailableBooksCount() {
    //     long count = bookService.getAvailableBooksCount();
    //     return ResponseEntity.ok(count);
    // }
    
    // @GetMapping("/stats/by-genre")
    // @Operation(summary = "Get books statistics by genre", description = "Get book count grouped by genre")
    // public ResponseEntity<List<Object[]>> getBookStatsByGenre() {
    //     List<Object[]> stats = bookService.getBookStatsByGenre();
    //     return ResponseEntity.ok(stats);
    // }
}