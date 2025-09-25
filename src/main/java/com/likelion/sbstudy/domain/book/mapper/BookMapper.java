package com.likelion.sbstudy.domain.book.mapper;

import com.likelion.sbstudy.domain.book.dto.request.bookRequest;
import com.likelion.sbstudy.domain.book.dto.response.BookResponse;
import com.likelion.sbstudy.domain.book.entity.Book;
import com.likelion.sbstudy.domain.book.entity.BookImage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

  public Book toEntity(bookRequest req) {
    return Book.builder()
        .title(req.getTitle())
        .author(req.getAuthor())
        .publisher(req.getPublisher())
        .price(req.getPrice())
        .description(req.getDescription())
        .releaseDate(req.getReleaseDate())
        .categoryList(req.getCategoryList())
        .build();
  }

  public void updateEntity(Book book, bookRequest req) {
    book.update(
        req.getTitle(),
        req.getAuthor(),
        req.getPublisher(),
        req.getPrice(),
        req.getDescription(),
        req.getReleaseDate(),
        req.getCategoryList()
    );
  }

  public BookResponse toBookResponse(Book book) {
    return BookResponse.builder()
        .id(book.getId())
        .title(book.getTitle())
        .author(book.getAuthor())
        .publisher(book.getPublisher())
        .price(book.getPrice())
        .description(book.getDescription())
        .releaseDate(book.getReleaseDate())
        .categoryList(book.getCategoryList())
        .bookImagesUrl(
            book.getBookImageList() == null ? List.of()
                : book.getBookImageList().stream()
                    .map(BookImage::getImageUrl)
                    .toList()
        )
        .build();
  }
}

