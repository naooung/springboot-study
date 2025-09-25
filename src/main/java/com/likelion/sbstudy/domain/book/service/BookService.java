package com.likelion.sbstudy.domain.book.service;

import com.likelion.sbstudy.domain.book.dto.request.bookRequest;
import com.likelion.sbstudy.domain.book.dto.response.BookResponse;
import com.likelion.sbstudy.domain.book.entity.Book;
import com.likelion.sbstudy.domain.book.entity.BookImage;
import com.likelion.sbstudy.domain.book.exception.BookErrorCode;
import com.likelion.sbstudy.domain.book.mapper.BookMapper;
import com.likelion.sbstudy.domain.book.repository.BookRepository;
import com.likelion.sbstudy.global.exception.CustomException;
import com.likelion.sbstudy.global.s3.dto.S3Response;
import com.likelion.sbstudy.global.s3.entity.PathName;
import com.likelion.sbstudy.global.s3.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

  private final BookRepository bookRepository;
  private final S3Service s3Service;
  private final BookMapper bookMapper;

  @Transactional
  public BookResponse createBook(bookRequest request, List<MultipartFile> images) {
    bookRepository.findByTitleAndAuthor(request.getTitle(), request.getAuthor())
        .ifPresent(b -> { throw new CustomException(BookErrorCode.BOOK_ALREADY_EXISTS); });

    Book book = bookMapper.toEntity(request);

    if (images != null && !images.isEmpty()) {
      List<BookImage> bookImages = images.stream()
          .filter(img -> img != null && !img.isEmpty())
          .map(img -> {
            // ✅ S3Service.uploadImage 사용
            S3Response res = s3Service.uploadImage(PathName.FOLDER1, img);
            return BookImage.builder()
                .imageUrl(res.getImageUrl())
                .book(book)
                .build();
          })
          .toList();
      book.addBookImages(bookImages);
    }

    bookRepository.save(book);
    return bookMapper.toBookResponse(book);
  }

  @Transactional(readOnly = true)
  public List<BookResponse> getBooks() {
    return bookRepository.findAll().stream()
        .map(bookMapper::toBookResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public BookResponse getBook(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));
    return bookMapper.toBookResponse(book);
  }

  @Transactional
  public BookResponse updateBook(Long id, bookRequest request, List<MultipartFile> images) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));

    bookMapper.updateEntity(book, request);

    // 이미지가 "전달되었을 때만" 교체(전달 안 되면 유지)
    if (images != null) {
      // 기존 이미지 S3 삭제 + 컬렉션 정리
      if (book.getBookImageList() != null) {
        for (BookImage img : book.getBookImageList()) {
          safeDeleteFromS3(img.getImageUrl());
        }
        book.getBookImageList().clear();
      }

      // 새 이미지 업로드
      List<BookImage> newImages = images.stream()
          .filter(img -> img != null && !img.isEmpty())
          .map(img -> {
            // ✅ S3Service.uploadImage 사용
            S3Response res = s3Service.uploadImage(PathName.FOLDER1, img);
            return BookImage.builder()
                .imageUrl(res.getImageUrl())
                .book(book)
                .build();
          })
          .toList();
      book.addBookImages(newImages);
    }

    return bookMapper.toBookResponse(book);
  }

  @Transactional
  public void deleteBook(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));

    // 이미지 S3 삭제
    if (book.getBookImageList() != null) {
      for (BookImage img : book.getBookImageList()) {
        safeDeleteFromS3(img.getImageUrl());
      }
    }
    bookRepository.delete(book);
  }

  private void safeDeleteFromS3(String url) {
    try {
      if (url != null && !url.isBlank()) s3Service.deleteFile(url);
    } catch (Exception e) {
      log.warn("S3 파일 삭제 실패 url={}", url, e);
    }
  }

  @Transactional
  public BookResponse addBookImages(Long id, List<MultipartFile> images) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));

    if (images != null && !images.isEmpty()) {
      List<BookImage> newImages = images.stream()
          .filter(img -> img != null && !img.isEmpty())
          .map(img -> {
            S3Response res = s3Service.uploadImage(PathName.FOLDER1, img);
            return BookImage.builder()
                .imageUrl(res.getImageUrl())
                .book(book)
                .build();
          })
          .toList();

      book.getBookImageList().addAll(newImages);
    }

    return bookMapper.toBookResponse(book);
  }
}