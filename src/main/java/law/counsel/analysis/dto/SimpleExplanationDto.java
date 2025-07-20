package law.counsel.analysis.dto;

// law/counsel/analysis/dto/SimpleExplanationDto.java

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SimpleExplanationDto {
    private Long sentenceId;
    private String simpleExplanation;
    private Integer riskLevel;
    private String suggestedRevision;
}
