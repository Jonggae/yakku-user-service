package com.jonggae.yakku.common.exceptions;

import com.jonggae.yakku.common.apiResponse.ApiResponseDto;
import com.jonggae.yakku.common.apiResponse.ApiResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        logger.error("Exception occurred: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
    }

    // Customer - 해당유저를 찾을 수 없을 때
    @ExceptionHandler(NotFoundMemberException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleNotFoundMemberException(NotFoundMemberException ex) {
        String errorMessage = "해당 사용자를 찾을 수 없습니다";
        return ApiResponseUtil.error(
                errorMessage,
                404,
                "NOT_FOUND_MEMBER",
                null
        );
    }

    // Product - 상품 등록 시 같은 이름의 상품을 등록하였을 때 - 데이터 무결성 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMessage = "같은 상품 이름으로 등록할 수 없습니다.";
        return ApiResponseUtil.error(
                errorMessage,
                400,
                "DATA_INTEGRITY_VIOLATION",
                null
        );
    }


}
