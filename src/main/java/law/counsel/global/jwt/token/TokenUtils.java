package law.counsel.global.jwt.token;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * AccessToken을 AUTHORIZATION 헤더에 넣습니다.
     */
    public void setAccessToken(HttpServletResponse response, String accessToken) {
        response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);
    }
}