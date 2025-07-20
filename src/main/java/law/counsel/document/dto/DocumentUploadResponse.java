package law.counsel.document.dto;

import law.counsel.analysis.dto.RiskAnalysisDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {
    private Long id;
    private String fileName;
    private LocalDateTime uploadedAt;
    private String status;
    private String improvedText;
    private List<RiskAnalysisDto> riskAnalyses;
}