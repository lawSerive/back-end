package law.counsel.member.controller;

import jakarta.validation.Valid;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import law.counsel.member.api.MemberApi;
import law.counsel.member.dto.SignUpRequest;
import law.counsel.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {
    private final MemberService memberService;
    /*
    회원 가입
     */
    @PostMapping("/sign-up")
    public ResponseEntity<ResponseBody<String>> signUp(@RequestBody @Valid SignUpRequest signUpRequest){
        memberService.signUp(signUpRequest);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse("회원가입 성공"));
    }
}
