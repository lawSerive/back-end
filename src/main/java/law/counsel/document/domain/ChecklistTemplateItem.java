package law.counsel.document.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="checklist_template_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

/*
각 유형별 질문이 담기는 테이블
 */
public class ChecklistTemplateItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="template_id")
    private ChecklistTemplate template;

    private Integer itemIndex;
    @Column(columnDefinition = "TEXT")
    private String question;
    private Boolean isMandatory;
}