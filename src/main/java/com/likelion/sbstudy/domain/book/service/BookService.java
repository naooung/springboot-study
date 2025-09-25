package com.likelion.sbstudy.domain.book.service;

import com.likelion.sbstudy.domain.book.dto.request.bookRequest;
import com.likelion.sbstudy.domain.book.dto.response.BookResponse;
import com.likelion.sbstudy.domain.book.entity.Category;
import com.likelion.sbstudy.global.page.response.InfiniteResponse;
import com.likelion.sbstudy.global.page.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 책 도메인의 비즈니스 로직 인터페이스.
 * 컨트롤러는 구현체가 아닌 이 인터페이스에만 의존합니다.
 */
public interface BookService {

  /**
   * 새 책을 생성합니다. (이미지는 선택 사항)
   *
   * @param request  책 생성에 필요한 요청 DTO (제목, 저자, 출판사, 가격, 설명, 출간일, 카테고리 목록)
   * @param images   책 이미지 파일 목록 (nullable, 비어있을 수 있음)
   * @return 생성된 책 정보를 담은 응답 DTO
   * @throws com.likelion.sbstudy.global.exception.CustomException
   *         BOOK_ALREADY_EXISTS: 동일한 (제목, 저자) 조합이 이미 존재할 경우
   */
  BookResponse createBook(bookRequest request, List<MultipartFile> images);

  /**
   * 모든 책을 조회합니다.
   *
   * @return 책 응답 DTO 리스트
   */
  List<BookResponse> getBooks();

  /**
   * 책 하나를 ID로 조회합니다.
   *
   * @param id  조회할 책의 식별자
   * @return 책 응답 DTO
   * @throws com.likelion.sbstudy.global.exception.CustomException
   *         BOOK_NOT_FOUND: 해당 ID의 책이 없을 경우
   */
  BookResponse getBook(Long id);

  /**
   * 책 정보를 수정합니다. (이미지가 전달되면 기존 이미지를 교체)
   *
   * @param id       수정할 책의 식별자
   * @param request  수정 값이 들어있는 요청 DTO
   * @param images   새로 교체할 이미지 목록 (nullable: null이면 이미지 유지, 빈 리스트면 이미지를 모두 제거하고 아무 것도 추가하지 않음)
   * @return 수정된 책 응답 DTO
   * @throws com.likelion.sbstudy.global.exception.CustomException
   *         BOOK_NOT_FOUND: 해당 ID의 책이 없을 경우
   */
  BookResponse updateBook(Long id, bookRequest request, List<MultipartFile> images);

  /**
   * 책을 삭제합니다. (연관된 이미지도 함께 제거)
   *
   * @param id  삭제할 책의 식별자
   * @throws com.likelion.sbstudy.global.exception.CustomException
   *         BOOK_NOT_FOUND: 해당 ID의 책이 없을 경우
   */
  void deleteBook(Long id);

  /**
   * 기존 책에 이미지를 추가합니다. (추가만, 교체 아님)
   *
   * @param id      이미지 추가 대상 책의 식별자
   * @param images  추가할 이미지 파일 목록 (nullable/empty 허용, empty면 변화 없음)
   * @return 이미지가 추가된 책 응답 DTO
   * @throws com.likelion.sbstudy.global.exception.CustomException
   *         BOOK_NOT_FOUND: 해당 ID의 책이 없을 경우
   */
  BookResponse addBookImages(Long id, List<MultipartFile> images);

  /**
   * 특정 카테고리에 속한 도서를 페이지 단위로 조회합니다.
   *
   * @param category 조회할 도서 카테고리
   * @param pageable 페이징 및 정렬 정보
   * @return 카테고리별 도서 목록 페이지
   */
  PageResponse<BookResponse> getBookPageByCategory(Category category, Pageable pageable);

  /**
   * 특정 카테고리에 속한 도서를 인피니티 스크롤 방식으로 조회합니다.
   *
   * @param category 조회할 도서 카테고리
   * @param lastBookId 이전 조회에서 마지막으로 가져온 도서 ID (처음 조회 시 null)
   * @param size 한 번에 가져올 도서 개수
   * @return 조회된 도서 목록
   */
  InfiniteResponse<BookResponse> getBooksByCategoryInfinite(
      Category category, Long lastBookId, Integer size);
}
