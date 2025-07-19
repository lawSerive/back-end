package law.counsel.member.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import law.counsel.global.config.swagger.SwaggerApiFailedResponse;
import law.counsel.global.config.swagger.SwaggerApiResponses;
import law.counsel.global.config.swagger.SwaggerApiSuccessResponse;
import law.counsel.global.exception.ExceptionType;
import law.counsel.global.jwt.token.dto.SignInRequest;
import law.counsel.global.response.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthApi {
    @Tag(name = "인증 API", description = "인증 관련 API")
    @Operation(
            summary = "로그인",
            description = "사용자 로그인을 진행해 액세스 토큰을 획득할 수 있습니다."
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    description = "로그인이 성공적으로 완료되어 액세스 토큰 및 리프레시 토큰이 발급되었습니다.",
                    response = String.class
            ),
            errors = {
                    @SwaggerApiFailedResponse(
                            value = ExceptionType.MEMBER_INFO_INVALID,
                            description = "이메일 또는 비밀번호가 일치하지 않을 때 발생합니다."
                    )
            }
    )
    ResponseEntity<ResponseBody<Void>> signIn(@Valid @RequestBody SignInRequest signInRequest,
                                              HttpServletResponse response);
}