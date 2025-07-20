package law.counsel.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentListResponse {
    private Long id;
    private String fileName;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
