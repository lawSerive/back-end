package law.counsel.global.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {
    // Common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR,"C001","예상치 못한 서버 오류가 발생했습니다."),
    BINDING_ERROR(BAD_REQUEST,"C002","요청 데이터 변환 과정에서 오류가 발생했습니다."),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT , "C003","필수 필드를 누락했습니다."),
    INVALID_ENDPOINT(NOT_FOUND, "C004", "잘못된 API URI로 요청했습니다."),
    INVALID_HTTP_METHOD(METHOD_NOT_ALLOWED, "C005","잘못된 HTTP 메서드로 요청했습니다."),


    // Security
    NEED_AUTHORIZED(UNAUTHORIZED, "S001", "인증이 필요합니다."),
    ACCESS_DENIED(FORBIDDEN, "S002", "접근 권한이 없습니다."),
    JWT_EXPIRED(UNAUTHORIZED, "S003", "인증 정보가 만료되었습니다."),
    JWT_INVALID(UNAUTHORIZED, "S004", "인증 정보가 잘못되었습니다."),
    JWT_NOT_EXIST(UNAUTHORIZED, "S005", "인증 정보가 존재하지 않습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}