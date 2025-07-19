package law.counsel.global.config.swagger;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.HandlerMethod;

@Configuration
@Profile("!test && !prod")
public class SwaggerConfig {
    @Value("${counsel.swagger.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${counsel.swagger.description:법률 자문 시스템 API 서버}")
    private String serverDescription;

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");

        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description(serverDescription))
                .addSecurityItem(securityRequirement)
                .components(securitySchemes());
    }

    @Bean
    public OperationCustomizer customize(
            SwaggerApiSuccessResponseHandler apiSuccessResponseHandler,
            SwaggerApiFailedResponseHandler apiFailedResponseHandler
    ) {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            apiSuccessResponseHandler.handle(operation, handlerMethod);
            apiFailedResponseHandler.handle(operation, handlerMethod);

            return operation;
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("Law Counsel API 명세서")
                .description("<p>이 문서는 Law Counsel 백엔드 API의 사용 방법과 예시를 제공합니다.</p>" +
                        "<p>API 사용 중 문제가 발생하거나 문의가 필요한 경우, 담당자에게 문의해 주세요.</p>" +
                        "<p>입력 유효성 오류는 명시돼 있지 않으며 <code>4XX</code> 상태 코드를 반환합니다." +
                        "<code>5XX</code> 상태 코드의 경우 정의되지 않은 서버 오류이므로 담당자에게 문의해 주세요.</p>")
                .version("1.0.0");
    }

    private Components securitySchemes() {
        final SecurityScheme accessTokenSecurityScheme = new SecurityScheme()
                .name("JWT")
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new Components()
                .addSecuritySchemes("JWT", accessTokenSecurityScheme);
    }
}
