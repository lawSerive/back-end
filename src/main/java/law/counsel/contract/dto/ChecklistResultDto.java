package law.counsel.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChecklistResultDto {
    private String clauseName;
    private String isIncluded;      // 포함여부
    private String matchConfidence; // 확신도
    private Long matchedSentenceId; // 포함된 문장 ID (없으면 null)
}
