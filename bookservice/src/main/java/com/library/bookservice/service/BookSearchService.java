package com.library.bookservice.service;

import com.library.bookservice.dto.BookSearchDto;
import com.library.bookservice.entity.Book;
import com.library.bookservice.mapper.BookMapper;
import com.library.bookservice.repository.BookSearchRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookSearchService.class);
    
    private final BookSearchRepository bookSearchRepository;
    // private final ElasticsearchOperations elasticsearchOperations;
    private final BookMapper bookMapper;
    
    @Autowired
    public BookSearchService(BookSearchRepository bookSearchRepository, 
                           ElasticsearchOperations elasticsearchOperations, 
                           BookMapper bookMapper) {
        this.bookSearchRepository = bookSearchRepository;
        // this.elasticsearchOperations = elasticsearchOperations;
        this.bookMapper = bookMapper;
    }
    
    public void indexBook(Book book) {
        try {
            logger.debug("Indexing book with ID: {} in Elasticsearch", book.getId());
            bookSearchRepository.save(book);
            logger.debug("Book indexed successfully");
        } catch (Exception e) {
            logger.error("Failed to index book with ID: {}", book.getId(), e);
        }
    }
    
    public void deleteBook(Long bookId) {
        try {
            logger.debug("Deleting book with ID: {} from Elasticsearch", bookId);
            bookSearchRepository.deleteById(bookId);
            logger.debug("Book deleted from index successfully");
        } catch (Exception e) {
            logger.error("Failed to delete book with ID: {} from index", bookId, e);
        }
    }
    
    // public List<BookSearchDto> searchBooks(Function<Builder, ObjectBuilder<MatchQuery>> query) {
    //     logger.debug("Searching books with query: {}", query);
    //     NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
    //         .withQuery(QueryBuilders.match(query)
    //             .field("title", 2.0f)
    //             .field("author", 1.5f)
    //             .field("description", 1.0f)
    //             .field("genre", 1.0f)
    //             .type("best_fields"))
    //         .build();
        
    //     SearchHits<Book> searchHits = elasticsearchOperations.search(searchQuery, Book.class);
        
    //     return searchHits.stream()
    //         .map(hit -> {
    //             BookSearchDto dto = bookMapper.toSearchDto(hit.getContent());
    //             dto.setScore(Double.valueOf(hit.getScore()));
    //             return dto;
    //         })
    //         .collect(Collectors.toList());
    // }
    
    public Page<BookSearchDto> searchBooksWithPagination(String query, Pageable pageable) {
        logger.debug("Searching books with pagination. Query: {}", query);
        
        Page<Book> books = bookSearchRepository.findByMultiMatch(query, pageable);
        return books.map(bookMapper::toSearchDto);
    }
    
    public List<BookSearchDto> searchByAuthor(String author) {
        logger.debug("Searching books by author: {}", author);
        return bookSearchRepository.findByAuthor(author)
            .stream()
            .map(bookMapper::toSearchDto)
            .collect(Collectors.toList());
    }
    
    public List<BookSearchDto> searchByGenre(String genre) {
        logger.debug("Searching books by genre: {}", genre);
        return bookSearchRepository.findByGenre(genre)
            .stream()
            .map(bookMapper::toSearchDto)
            .collect(Collectors.toList());
    }
    
    public List<BookSearchDto> searchByYearRange(Integer startYear, Integer endYear) {
        logger.debug("Searching books by year range: {} - {}", startYear, endYear);
        return bookSearchRepository.findByPublicationYearBetween(startYear, endYear)
            .stream()
            .map(bookMapper::toSearchDto)
            .collect(Collectors.toList());
    }
    
    public List<BookSearchDto> searchAvailableBooks() {
        logger.debug("Searching available books");
        return bookSearchRepository.findAvailableBooks()
            .stream()
            .map(bookMapper::toSearchDto)
            .collect(Collectors.toList());
    }
    
    // public List<BookSearchDto> advancedSearch(String title, String author, String genre, 
    //                                         Integer yearFrom, Integer yearTo, Boolean availableOnly) {
    //     logger.debug("Advanced search with filters");
        
    //     NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        
    //     if (title != null && !title.trim().isEmpty()) {
    //         queryBuilder.withQuery(QueryBuilders.match().field("title").query(title));
    //     }
    //     if (author != null && !author.trim().isEmpty()) {
    //         queryBuilder.withQuery(QueryBuilders.match("author", author));
    //     }
    //     }
        
    //     if (genre != null && !genre.trim().isEmpty()) {
    //         queryBuilder.withQuery(termQuery("genre", genre));
    //     }
        
    //     if (yearFrom != null && yearTo != null) {
    //         queryBuilder.withQuery(rangeQuery("publicationYear").gte(yearFrom).lte(yearTo));
    //     }
        
    //     if (availableOnly != null && availableOnly) {
    //         org.springframework.data.elasticsearch.client.elc.QueryBuilders.withQuery(boolQuery()
    //             .must(termQuery("status", "AVAILABLE"))
    //             .must(rangeQuery("availableCopies").gt(0)));
    //     }
        
    //     SearchHits<Book> searchHits = elasticsearchOperations.search(org.springframework.data.elasticsearch.client.elc.QueryBuilders.build(), Book.class);
        
    //     return searchHits.stream()
    //         .map(hit -> {
    //             BookSearchDto dto = bookMapper.toSearchDto(hit.getContent());
    //             dto.setScore(hit.getScore());
    //             return dto;
    //         })
    //         .collect(Collectors.toList());
    // }
}