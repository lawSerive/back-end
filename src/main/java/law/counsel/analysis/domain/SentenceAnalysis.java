package law.counsel.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import law.counsel.document.domain.DocumentSentence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sentence_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
GPT 위험도 분석 결과
 */
public class SentenceAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "risk_level")
    private Integer riskLevel; // 위험도

    @Column(name = "risk_reason", columnDefinition = "TEXT")
    private String riskReason; // 위험 판단 사유

    @Column(name = "simple_explanation", columnDefinition = "TEXT")
    private String simpleExplanation; // 쉬운 말 해석

    @Column(name = "suggested_revision", columnDefinition = "TEXT")
    private String suggestedRevision;  // 추천 수정안

    @Column(name = "legal_references", columnDefinition = "JSON")
    private String legalReferences; // 관련 법령,조항

    @ManyToOne
    @JoinColumn(name = "sentence_id", nullable = false)
    private DocumentSentence sentence;
}