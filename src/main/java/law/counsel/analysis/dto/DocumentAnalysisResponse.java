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
public class DocumentAnalysisResponse {
    private String improvedText;
    private List<RiskAnalysisDto> riskAnalyses;
}