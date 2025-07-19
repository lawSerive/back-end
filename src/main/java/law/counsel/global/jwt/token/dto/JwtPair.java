package law.counsel.global.jwt.token.dto;

public record JwtPair (
        String accessToken,
        int accessTokenExpiredIn
) {
    public static JwtPair of(String accessToken, int accessTokenExpiredIn) {
        return new JwtPair(accessToken, accessTokenExpiredIn);
    }
}
