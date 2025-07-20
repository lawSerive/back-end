package law.counsel.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskAnalysisDto {
    private Long sentenceId;
    private String riskLevel;
    private String originalText;
    private String riskReason;
    private List<String> legalReferences;
    private String simpleExplanation;
    private String suggestedRevision;
}