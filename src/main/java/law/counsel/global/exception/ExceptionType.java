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
    JWT_NOT_EXIST(UNAUTHORIZED, "S005", "인증 정보가 존재하지 않습니다."),

    // Member
    MEMBER_ALREADY_EXISTS(CONFLICT,"M001","이미 존재하는 유저입니다."),
    MEMBER_INFO_INVALID(CONFLICT,"M002","유저 정보가 일치하지 않습니다."),

    //Document
    DOCUMENT_NOT_FOUND(NOT_FOUND,"D001","문서를 찾을 수 없습니다."),
    DOCUMENT_NOT_PROCESSED(BAD_REQUEST,"D002","문서가 아직 처리되지 않았습니다."),
    ANALYSIS_DATA_NOT_AVAILABLE(BAD_REQUEST,"D003","분석 데이터를 사용할 수 없습니다. 파일을 다시 업로드해주세요."),
    FILE_UPLOAD_FAILED(BAD_REQUEST,"D004","파일 업로드에 실패했습니다."),
    UNSUPPORTED_FILE_TYPE(BAD_REQUEST,"D005","지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(BAD_REQUEST,"D006","파일 크기가 제한을 초과했습니다."),
    
    // Analysis
    ANALYSIS_FAILED(INTERNAL_SERVER_ERROR,"A001","문서 분석에 실패했습니다."),
    ANALYSIS_NOT_COMPLETED(BAD_REQUEST,"A002","문서 분석이 아직 완료되지 않았습니다."),
    RISK_DETECTION_FAILED(INTERNAL_SERVER_ERROR,"A003","위험조항 탐지에 실패했습니다."),
    SIMPLE_INTERPRETATION_FAILED(INTERNAL_SERVER_ERROR,"A004","쉬운말 해석에 실패했습니다."),
    TEXT_IMPROVEMENT_FAILED(INTERNAL_SERVER_ERROR,"A005","텍스트 가독성 향상에 실패했습니다."),

    // checklist
    TEMPLATE_NOT_FOUND(NOT_FOUND,"CH001","템플릿을 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}