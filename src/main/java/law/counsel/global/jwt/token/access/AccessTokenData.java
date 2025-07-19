package law.counsel.global.jwt.token.access;

public record AccessTokenData(
        String token,
        int expiredIn
){
}