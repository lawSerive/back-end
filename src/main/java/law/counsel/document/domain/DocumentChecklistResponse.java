package law.counsel.document.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/* 사용자가 문서에서 체크한 결과 */
@Entity
@Table(name = "document_checklist_response")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentChecklistResponse {

    @Id @GeneratedValue
    private Long responseId;

    @ManyToOne @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne @JoinColumn(name = "item_id")
    private ChecklistTemplateItem item;

    /** ✔︎ Boolean 으로 회귀 */
    private Boolean isChecked;              // true = 체크, false = 미체크

    private LocalDateTime checkedAt;
}