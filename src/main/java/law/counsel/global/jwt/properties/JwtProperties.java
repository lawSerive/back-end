package law.counsel.global.jwt.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuer;
    private String audience;
    private int accessTokenExpiredIn;
}
