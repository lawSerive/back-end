package law.counsel.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentStatusResponse {
    private Long documentId;
    private String fileName;
    private String status;
    private boolean isCompleted;
    private boolean isFailed;
    private String errorMessage;
}