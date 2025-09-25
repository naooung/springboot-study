package com.likelion.sbstudy.global.page.exception;

import com.likelion.sbstudy.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PageErrorCode implements BaseErrorCode {
  PAGE_NOT_FOUND("PAGE001", "페이지가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  PAGE_SIZE_ERROR("PAGE002", "페이지 크기 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
  PAGING_ERROR("PAGE003", "페이징 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}