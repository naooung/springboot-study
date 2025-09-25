package com.likelion.sbstudy.domain.book.service;

import com.likelion.sbstudy.domain.book.dto.request.bookRequest;
import com.likelion.sbstudy.domain.book.dto.response.BookResponse;
import com.likelion.sbstudy.domain.book.entity.Book;
import com.likelion.sbstudy.domain.book.entity.BookImage;
import com.likelion.sbstudy.domain.book.entity.Category;
import com.likelion.sbstudy.domain.book.exception.BookErrorCode;
import com.likelion.sbstudy.domain.book.mapper.BookMapper;
import com.likelion.sbstudy.domain.book.repository.BookRepository;
import com.likelion.sbstudy.global.exception.CustomException;
import com.likelion.sbstudy.global.page.mapper.InfiniteMapper;
import com.likelion.sbstudy.global.page.mapper.PageMapper;
import com.likelion.sbstudy.global.page.response.InfiniteResponse;
import com.likelion.sbstudy.global.page.response.PageResponse;
import com.likelion.sbstudy.global.s3.dto.S3Response;
import com.likelion.sbstudy.global.s3.entity.PathName;
import com.likelion.sbstudy.global.s3.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link BookService} 구현체.
 * S3 업로드와 JPA 저장/조회, 매핑을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final S3Service s3Service;
  private final BookMapper bookMapper;
  private final PageMapper pageMapper;
  private final InfiniteMapper infiniteMapper;

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public BookResponse createBook(bookRequest request, List<MultipartFile> images) {
    bookRepository.findByTitleAndAuthor(request.getTitle(), request.getAuthor())
        .ifPresent(b -> { throw new CustomException(BookErrorCode.BOOK_ALREADY_EXISTS); });

    Book book = bookMapper.toEntity(request);

    if (images != null && !images.isEmpty()) {
      List<BookImage> bookImages = images.stream()
          .filter(img -> img != null && !img.isEmpty())
          .map(img -> {
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

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public List<BookResponse> getBooks() {
    return bookRepository.findAll().stream()
        .map(bookMapper::toBookResponse)
        .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public BookResponse getBook(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));
    return bookMapper.toBookResponse(book);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public BookResponse updateBook(Long id, bookRequest request, List<MultipartFile> images) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));

    // 값 갱신 (전체 교체 or 부분 업데이트는 mapper 정책에 따름)
    bookMapper.updateEntity(book, request);

    // 이미지가 전달되었을 때만 교체 (null이면 유지)
    if (images != null) {
      if (book.getBookImageList() != null) {
        for (BookImage img : book.getBookImageList()) {
          safeDeleteFromS3(img.getImageUrl());
        }
        book.getBookImageList().clear();
      }

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

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public void deleteBook(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new CustomException(BookErrorCode.BOOK_NOT_FOUND));

    if (book.getBookImageList() != null) {
      for (BookImage img : book.getBookImageList()) {
        safeDeleteFromS3(img.getImageUrl());
      }
    }
    bookRepository.delete(book);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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

      book.getBookImageList().addAll(newImages); // 추가(append)
    }

    return bookMapper.toBookResponse(book);
  }

  // 내부 유틸
  private void safeDeleteFromS3(String url) {
    try {
      if (url != null && !url.isBlank()) s3Service.deleteFile(url);
    } catch (Exception e) {
      log.warn("S3 파일 삭제 실패 url={}", url, e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<BookResponse> getBookPageByCategory(Category category, Pageable pageable) {

    Page<BookResponse> bookPage =
        bookRepository
            .findAllByCategoryListContaining(category, pageable)
            .map(bookMapper::toBookResponse);

    log.info(
        "책 페이지 조회 성공: category={}, pageNumber={}, totalElements={}",
        category,
        pageable.getPageNumber(),
        bookPage.getTotalElements());
    return pageMapper.toPageResponse(bookPage);
  }

  @Override
  @Transactional(readOnly = true)
  public InfiniteResponse<BookResponse> getBooksByCategoryInfinite(
      Category category, Long lastBookId, Integer size) {

    Pageable pageable = PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id"));
    List<Book> books;

    if (lastBookId == null) {
      books = bookRepository.findAllByCategoryListContaining(category, pageable).getContent();
    } else {
      books =
          bookRepository
              .findAllByCategoryListContainingAndIdLessThan(category, lastBookId, pageable)
              .getContent();
    }

    boolean hasNext = books.size() > size;
    if (hasNext) {
      books = books.subList(0, size);
    }

    List<BookResponse> bookResponseList = books.stream().map(bookMapper::toBookResponse).toList();

    Long newLastCursor = books.isEmpty() ? null : books.getLast().getId();

    log.info("책 인피니티 스크롤 조회 성공: category={}, lastBookId={}, size={}", category, lastBookId, size);
    return infiniteMapper.toInfiniteResponse(bookResponseList, newLastCursor, hasNext, size);
  }
}