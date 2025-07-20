import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@OpenAPIDefinition(
        info = @Info(
                title = "Law Document Processor API",
                version = "1.0",
                description = "법률 문서 OCR 및 AI 해석 시스템"
        )
)
public class LawDocumentProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LawDocumentProcessorApplication.class, args);
    }

}