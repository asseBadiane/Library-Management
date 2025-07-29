package com.library.bookservice.mapper;

import com.library.bookservice.dto.BookDto;
import com.library.bookservice.dto.BookSearchDto;
import com.library.bookservice.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {
    
    BookDto toDto(Book book);
    
    Book toEntity(BookDto bookDto);
    
    BookSearchDto toSearchDto(Book book);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Book book, BookDto bookDto);
}