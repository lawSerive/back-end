package law.counsel.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import law.counsel.global.jwt.token.TokenUtils;
import law.counsel.global.jwt.token.dto.JwtPair;
import law.counsel.global.jwt.token.dto.SignInRequest;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import law.counsel.member.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenUtils tokenUtils;
    /*
    로그인
     */
    @PostMapping("/sign-in")
    public ResponseEntity<ResponseBody<Void>> signIn(@RequestBody SignInRequest signInRequest,
                                                     HttpServletResponse response) {
        JwtPair tokens = authService.signIn(signInRequest.email(), signInRequest.password());
        tokenUtils.setAccessToken(response, tokens);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse());
    }
}
