package law.counsel.member.service;
import law.counsel.global.exception.BusinessException;
import law.counsel.global.exception.ExceptionType;
import law.counsel.member.Member;
import law.counsel.member.MemberType;
import law.counsel.member.dto.SignUpRequest;
import law.counsel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    public void signUp(SignUpRequest signUpRequest){
        // 존재하는 유저인지 확인
        if(memberRepository.findByEmail(signUpRequest.getEmail()).isPresent()){
            throw new BusinessException(ExceptionType.MEMBER_ALREADY_EXISTS);
        }
        Member member = Member.builder()
                .name(signUpRequest.getName())
                .memberType(MemberType.USER)
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build();
        memberRepository.save(member);

    }
}
