package law.counsel.document.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="checklist_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

/*
계약 유형별 체크리스트
 */
public class ChecklistTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Enumerated(EnumType.STRING)
    private ContractType contractType; // 계약 유형


    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistTemplateItem> items = new ArrayList<>();
}
