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
public class RiskHighlightDto {
    private Long sentenceId;
    private String riskLevel;
    private String originalText;
    private String riskReason;
    private List<String> legalReferences;
    private int startIndex;
    private int endIndex;
}