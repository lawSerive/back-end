package law.counsel.member.service;


import law.counsel.global.exception.BusinessException;
import law.counsel.global.exception.ExceptionType;
import law.counsel.global.jwt.JwtClaims;
import law.counsel.global.jwt.token.access.AccessTokenData;
import law.counsel.global.jwt.token.access.AccessTokenProvider;
import law.counsel.global.jwt.token.dto.JwtPair;
import law.counsel.member.Member;
import law.counsel.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final AccessTokenProvider accessTokenProvider;


    private final PasswordEncoder passwordEncoder;

    public JwtPair signIn(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionType.MEMBER_INFO_INVALID));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ExceptionType.MEMBER_INFO_INVALID);
        }

        // 토큰 발급
        return generateJwtPair(member);
    }

    public JwtPair generateJwtPair(Member member) {
        JwtClaims claims = JwtClaims.create(member);
        AccessTokenData accessToken = accessTokenProvider.createToken(claims);
        return JwtPair.of(accessToken.token(), accessToken.expiredIn());
    }

}
