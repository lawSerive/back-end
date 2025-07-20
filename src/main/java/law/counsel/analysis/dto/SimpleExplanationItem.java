package law.counsel.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleExplanationItem {
    private Long sentenceId;
    private String riskLevel;
    private String originalText;
    private String simpleExplanation;
    private String suggestedRevision;
}