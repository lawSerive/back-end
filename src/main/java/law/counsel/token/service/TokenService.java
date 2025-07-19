package law.counsel.token.service;


import law.counsel.global.jwt.JwtClaims;
import law.counsel.global.jwt.token.access.AccessTokenData;
import law.counsel.global.jwt.token.access.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final AccessTokenProvider accessTokenProvider;

    public String createAccessToken(JwtClaims claims) {
        AccessTokenData accessToken = accessTokenProvider.createToken(claims);
        return accessToken.token();
    }
}