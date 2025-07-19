package law.counsel.global.exception;


import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RequiredArgsConstructor
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseBody<Void>> handleBusinessException(BusinessException e) {
        ExceptionType exceptionType = e.getExceptionType();
        return ResponseEntity.status(exceptionType.getStatus())
                .body(ResponseUtil.createFailureResponse(exceptionType));

    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBody<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String customMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(ExceptionType.BINDING_ERROR.getStatus())
                .body(ResponseUtil.createFailureResponse(ExceptionType.BINDING_ERROR, customMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBody<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException : {}", e);
        return ResponseEntity
                .status(ExceptionType.BINDING_ERROR.getStatus())
                .body(ResponseUtil.createFailureResponse(ExceptionType.BINDING_ERROR));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseBody<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(ExceptionType.INVALID_HTTP_METHOD.getStatus())
                .body(ResponseUtil.createFailureResponse(ExceptionType.INVALID_HTTP_METHOD));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseBody<Void>> handleNotFound(NoResourceFoundException e) {
        return ResponseEntity
                .status(ExceptionType.INVALID_ENDPOINT.getStatus())
                .body(ResponseUtil.createFailureResponse(ExceptionType.INVALID_ENDPOINT));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseBody<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException : {}", e);
        return ResponseEntity
                .status(ExceptionType.ACCESS_DENIED.getStatus())
                .body(ResponseUtil.createFailureResponse(ExceptionType.ACCESS_DENIED));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ResponseBody<Void>> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e) {
        return ResponseEntity
                .status(ExceptionType.NEED_AUTHORIZED.getStatus())
                .body(ResponseUtil.createFailureResponse(ExceptionType.NEED_AUTHORIZED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBody<Void>> handleException(Exception e){
        log.error("unhandle error : {}", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtil.createFailureResponse(ExceptionType.UNEXPECTED_SERVER_ERROR));
    }
}
