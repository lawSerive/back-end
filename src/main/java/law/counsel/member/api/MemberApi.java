package law.counsel.member.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import law.counsel.global.config.swagger.SwaggerApiFailedResponse;
import law.counsel.global.config.swagger.SwaggerApiResponses;
import law.counsel.global.config.swagger.SwaggerApiSuccessResponse;
import law.counsel.global.exception.ExceptionType;
import law.counsel.global.response.ResponseBody;
import law.counsel.member.dto.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface MemberApi {
    @Tag(name = "회원 API", description = "회원 관련 API")
    @Operation(
            summary = "회원 가입",
            description = "새로운 사용자 계정을 생성합니다."
    )
    @SwaggerApiResponses(
            success = @SwaggerApiSuccessResponse(
                    response = String.class,
                    description = "회원가입이 성공적으로 완료되었습니다."
            ),
            errors = {
                    @SwaggerApiFailedResponse(
                            value = ExceptionType.MEMBER_ALREADY_EXISTS,
                            description = "이미 등록된 이메일 주소로 회원가입을 시도할 때 발생합니다."
                    )
            }
    )
    ResponseEntity<ResponseBody<String>> signUp(@Valid @RequestBody SignUpRequest signUpRequest);
}