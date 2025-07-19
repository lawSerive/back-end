package law.counsel.global.jwt.token.access;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import law.counsel.global.jwt.JwtClaims;
import law.counsel.global.jwt.properties.JwtProperties;
import law.counsel.global.jwt.properties.KeyProperties;
import law.counsel.member.MemberType;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AccessTokenProvider {

    private static final String MEMBER_ID = "MEMBER_ID";
    private static final String MEMBER_TYPE = "MEMBER_TYPE";
    private static final long MILLI_SECOND = 1000L;

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public AccessTokenProvider(KeyProperties keyProperties, JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(keyProperties.getSalt().getBytes(StandardCharsets.UTF_8));
    }

    public AccessTokenData createToken(JwtClaims jwtClaims) {
        Date now = new Date(System.currentTimeMillis());
        Date expired = new Date(now.getTime() + (long) jwtProperties.getAccessTokenExpiredIn() * MILLI_SECOND);

        return new AccessTokenData(Jwts.builder()
                .claims(generateClaims(jwtClaims))
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(now)
                .expiration(expired)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact(), jwtProperties.getAccessTokenExpiredIn());
    }

    private Map<String, Object> generateClaims(JwtClaims jwtClaims) {
        return Map.of(
                MEMBER_ID, jwtClaims.memberId(),
                MEMBER_TYPE, jwtClaims.memberType());
    }

    public JwtClaims parseToken(String accessToken) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        return convertJwtClaims(claims);
    }

    public Optional<JwtClaims> getClaims(String accessToken) {
        try {
            return Optional.of(parseToken(accessToken));
        } catch (ExpiredJwtException e) {
            return Optional.of(convertJwtClaims(e.getClaims()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private JwtClaims convertJwtClaims(Claims claims) {
        return new JwtClaims(claims.get(MEMBER_ID, Long.class),
                MemberType.valueOf(claims.get(MEMBER_TYPE, String.class)));
    }

}