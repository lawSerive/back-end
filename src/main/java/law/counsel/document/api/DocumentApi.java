/*───────────────────────────────────────────────────────
  src/main/java/law/counsel/document/api/DocumentApi.java
───────────────────────────────────────────────────────*/
package law.counsel.document.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import law.counsel.document.dto.DocumentResponse;
import law.counsel.global.config.swagger.SwaggerApiFailedResponse;
import law.counsel.global.config.swagger.SwaggerApiResponses;
import law.counsel.global.config.swagger.SwaggerApiSuccessResponse;
import law.counsel.global.exception.ExceptionType;
import law.counsel.global.jwt.annotation.CurrentMemberId;
import law.counsel.global.response.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(
        name        = "문서 API",
        description = "로그인 사용자가 업로드한 문서 리스트"
)
@RequestMapping("/api/documents")
public interface DocumentApi {

    /*────────────────────────────────────────────
      GET /api/documents   —  내 문서 목록
    ────────────────────────────────────────────*/
    @Operation(
            summary     = "내 문서 목록",
            description = """
            로그인된 사용자가 업로드한 모든 문서를 반환합니다.
            """
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    description = "조회 성공",
                    response    = DocumentResponse.class
            ),
            errors = {
                    @SwaggerApiFailedResponse(value = ExceptionType.NEED_AUTHORIZED, description = "인증 정보 없음"),
                    @SwaggerApiFailedResponse(value = ExceptionType.JWT_EXPIRED    , description = "토큰 만료"),
                    @SwaggerApiFailedResponse(value = ExceptionType.JWT_INVALID    , description = "잘못된 토큰"),
                    @SwaggerApiFailedResponse(value = ExceptionType.JWT_NOT_EXIST  , description = "토큰이 비어 있음")
            }
    )
    @GetMapping
    ResponseEntity<ResponseBody<List<DocumentResponse>>> listMyDocuments(
            @Parameter(hidden = true)          // Swagger UI 에서는 감춤
            @CurrentMemberId Long memberId     // 커스텀 리졸버가 주입
    );
}
