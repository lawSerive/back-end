package law.counsel.global.jwt.authentication;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import law.counsel.global.exception.BusinessException;
import law.counsel.global.exception.ExceptionType;
import law.counsel.global.jwt.JwtClaims;
import law.counsel.global.jwt.token.access.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;



@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final AccessTokenProvider accessTokenProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtUnauthenticatedToken unauthenticatedToken = (JwtUnauthenticatedToken) authentication;

        String accessToken = unauthenticatedToken.token();
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }

        try {
            JwtClaims claims = accessTokenProvider.parseToken(accessToken);
            return new JwtAuthentication(claims);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ExceptionType.JWT_EXPIRED);
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            throw new BusinessException(ExceptionType.JWT_INVALID);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtUnauthenticatedToken.class.isAssignableFrom(authentication);
    }
}