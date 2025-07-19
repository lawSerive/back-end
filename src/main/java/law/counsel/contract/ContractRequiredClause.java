package law.counsel.contract;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contract_required_clauses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRequiredClause {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "required_clause_id")
    private Long requiredClauseId;

    @Column(name = "clause_code")
    private String clauseCode;

    @Column(name = "clause_name")
    private String clauseName;

    @Column(name = "detection_keywords", columnDefinition = "JSON")
    private String detectionKeywords;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private ContractType contractType;
}