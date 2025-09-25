package com.likelion.sbstudy.domain.book.controller;

import com.likelion.sbstudy.domain.book.dto.request.bookRequest;
import com.likelion.sbstudy.domain.book.dto.response.BookResponse;
import com.likelion.sbstudy.domain.book.entity.Category;
import com.likelion.sbstudy.domain.book.service.BookService;
import com.likelion.sbstudy.global.exception.CustomException;
import com.likelion.sbstudy.global.page.exception.PageErrorCode;
import com.likelion.sbstudy.global.page.response.InfiniteResponse;
import com.likelion.sbstudy.global.page.response.PageResponse;
import com.likelion.sbstudy.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "Book", description = "Book 관련 API")
public class BookController {

  private final BookService bookService;

  @Operation(summary = "새 책 등록", description = "새로운 책을 등록하고, 등록된 책 정보를 반환합니다. (201 Created)")
  @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<BookResponse>> createBook(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @RequestPart("book") @Valid bookRequest request,
      @Parameter(description = "책 이미지들", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {

    BookResponse response = bookService.createBook(request, images);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("책 생성에 성공하였습니다.", response));
  }

  @Operation(summary = "책 전체 조회", description = "등록된 모든 책을 조회합니다.")
  @GetMapping("")
  public ResponseEntity<BaseResponse<List<BookResponse>>> getBooks() {
    List<BookResponse> responses = bookService.getBooks();
    return ResponseEntity.ok(BaseResponse.success("책 목록 조회에 성공하였습니다.", responses));
  }

  @Operation(summary = "책 단일 조회", description = "ID로 책 하나를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<BaseResponse<BookResponse>> getBook(@PathVariable Long id) {
    BookResponse response = bookService.getBook(id);
    return ResponseEntity.ok(BaseResponse.success("책 조회에 성공하였습니다.", response));
  }

  @Operation(summary = "책 수정", description = "책 정보를 수정합니다. 이미지가 전달되면 기존 이미지를 교체합니다.")
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<BookResponse>> updateBook(
      @PathVariable Long id,
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @RequestPart("book") @Valid bookRequest request,
      @Parameter(description = "책 이미지들(전달되면 교체)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {

    BookResponse response = bookService.updateBook(id, request, images);
    return ResponseEntity.ok(BaseResponse.success("책 수정에 성공하였습니다.", response));
  }

  @Operation(summary = "책 삭제", description = "책과 연결된 이미지까지 모두 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseResponse<Void>> deleteBook(@PathVariable Long id) {
    bookService.deleteBook(id);
    return ResponseEntity.ok(BaseResponse.success("책 삭제에 성공하였습니다.", null));
  }

  @Operation(summary = "책 이미지 추가", description = "기존 책에 이미지를 추가합니다.")
  @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<BookResponse>> addBookImages(
      @PathVariable Long id,
      @RequestPart(value = "images") List<MultipartFile> images) {

    BookResponse response = bookService.addBookImages(id, images);
    return ResponseEntity.ok(BaseResponse.success("책 이미지 추가에 성공하였습니다.", response));
  }

  @Operation(summary = "책 페이지 조회", description = "정렬 기준에 맞춰 책 페이지 정보를 조회합니다.")
  @GetMapping("/page")
  public ResponseEntity<BaseResponse<PageResponse<BookResponse>>> getBookPageByCategory(
      @RequestParam Category category,
      @RequestParam(defaultValue = "1") Integer pageNum,
      @RequestParam(defaultValue = "4") Integer pageSize) {

    Pageable pageable = validatePageable(pageNum, pageSize);
    PageResponse<BookResponse> pageResponse = bookService.getBookPageByCategory(category, pageable);

    return ResponseEntity.ok(BaseResponse.success("페이지 조회에 성공했습니다.", pageResponse));
  }

  @Operation(summary = "책 인피니티 스크롤 조회", description = "마지막으로 조회한 책 식별자 이후의 책 목록을 조회합니다.")
  @GetMapping("/infinite")
  public ResponseEntity<BaseResponse<InfiniteResponse<BookResponse>>> getBooksByCategoryInfinite(
      @RequestParam Category category,
      @RequestParam(required = false) Long lastBookId,
      @RequestParam(defaultValue = "3") Integer size) {

    InfiniteResponse<BookResponse> response =
        bookService.getBooksByCategoryInfinite(category, lastBookId, size);

    return ResponseEntity.ok(BaseResponse.success("인피니티 스크롤 조회에 성공했습니다.", response));
  }

  // 검증 로직은 controller에서 실행
  private Pageable validatePageable(Integer pageNum, Integer pageSize) {
    if (pageNum < 1) {
      throw new CustomException(PageErrorCode.PAGE_NOT_FOUND);
    }
    if (pageSize < 1) {
      throw new CustomException(PageErrorCode.PAGE_SIZE_ERROR);
    }

    return PageRequest.of(pageNum - 1, pageSize);
  }
}