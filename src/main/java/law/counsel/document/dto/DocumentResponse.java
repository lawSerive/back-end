package law.counsel.document.dto;

import law.counsel.document.domain.Document;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentResponse {
    private Long id;
    private String fileName;
    private LocalDateTime uploadedAt;

    public DocumentResponse(Long id, String fileName, LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.uploadedAt = uploadedAt;
    }

    public static DocumentResponse from(Document document){
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getOriginalFilename())
                .uploadedAt(document.getCreatedAt())
                .build();
    }
}
