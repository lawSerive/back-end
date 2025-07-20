package law.counsel.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import law.counsel.document.domain.Document;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "문서 응답")
public class DocumentResponse {
    @Schema(description = "문서 ID", example = "1")
    private Long id;
    
    @Schema(description = "파일명", example = "계약서.pdf")
    private String fileName;
    
    @Schema(description = "업로드 시간", example = "2023-12-01T10:30:00")
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
