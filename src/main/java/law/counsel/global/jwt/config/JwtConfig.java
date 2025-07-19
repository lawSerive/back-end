package law.counsel.global.jwt.config;


import law.counsel.global.jwt.authentication.JwtAuthenticationProvider;
import law.counsel.global.jwt.properties.JwtProperties;
import law.counsel.global.jwt.properties.KeyProperties;
import law.counsel.global.jwt.token.access.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({ KeyProperties.class, JwtProperties.class })
public class JwtConfig {
    @Bean
    public AccessTokenProvider accessTokenProvider(KeyProperties keyProperties, JwtProperties jwtProperties) {
        return new AccessTokenProvider(keyProperties, jwtProperties);
    }


    @Bean
    public AuthenticationManager authenticationManager(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(jwtAuthenticationProvider);
    }
}