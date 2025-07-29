package com.library.bookservice.repository;


import com.library.bookservice.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSearchRepository extends ElasticsearchRepository<Book, Long> {
    
    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"title\": {\"query\": \"?0\", \"boost\": 2}}}," +
           "{\"match\": {\"author\": {\"query\": \"?0\", \"boost\": 1.5}}}," +
           "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1}}}," +
           "{\"match\": {\"genre\": {\"query\": \"?0\", \"boost\": 1}}}" +
           "]}}")
    Page<Book> findByMultiMatch(String query, Pageable pageable);
    
    List<Book> findByAuthor(String author);
    
    List<Book> findByGenre(String genre);
    
    @Query("{\"bool\": {\"must\": [" +
           "{\"range\": {\"publicationYear\": {\"gte\": ?0, \"lte\": ?1}}}" +
           "]}}")
    List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear);
    
    @Query("{\"bool\": {\"must\": [" +
           "{\"term\": {\"status\": \"AVAILABLE\"}}," +
           "{\"range\": {\"availableCopies\": {\"gt\": 0}}}" +
           "]}}")
    List<Book> findAvailableBooks();
}