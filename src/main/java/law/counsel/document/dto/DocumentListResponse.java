package law.counsel.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DocumentListResponse {
    private Long documentId;
    private String fileName;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
