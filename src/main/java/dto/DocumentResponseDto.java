package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDto {

    private Long id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String mimeType;
    private Document.ProcessingStatus status;
    private String extractedText;
    private String interpretedText;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 변환
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 처리 상태를 한국어로 변환
     */
    public String getStatusInKorean() {
        if (status == null) return "알 수 없음";

        return switch (status) {
            case UPLOADED -> "업로드 완료";
            case OCR_PROCESSING -> "텍스트 추출 중";
            case OCR_COMPLETED -> "텍스트 추출 완료";
            case AI_PROCESSING -> "AI 해석 중";
            case COMPLETED -> "처리 완료";
            case FAILED -> "처리 실패";
        };
    }

    /**
     * 처리 진행률 계산 (0-100)
     */
    public int getProgressPercentage() {
        if (status == null) return 0;

        return switch (status) {
            case UPLOADED -> 10;
            case OCR_PROCESSING -> 30;
            case OCR_COMPLETED -> 60;
            case AI_PROCESSING -> 80;
            case COMPLETED -> 100;
            case FAILED -> 0;
        };
    }

    /**
     * 처리 완료 여부
     */
    public boolean isCompleted() {
        return status == Document.ProcessingStatus.COMPLETED;
    }

    /**
     * 처리 실패 여부
     */
    public boolean isFailed() {
        return status == Document.ProcessingStatus.FAILED;
    }

    /**
     * 처리 중인지 여부
     */
    public boolean isProcessing() {
        return status != null && (
                status == Document.ProcessingStatus.OCR_PROCESSING ||
                        status == Document.ProcessingStatus.AI_PROCESSING
        );
    }
}