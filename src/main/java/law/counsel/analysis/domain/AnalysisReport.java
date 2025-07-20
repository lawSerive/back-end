package law.counsel.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import law.counsel.document.domain.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "analysis_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_report_id")
    private Long analysisReportId;

    @Column(name = "report_title")
    private String reportTitle;

    @Column(name = "total_sentences")
    private Integer totalSentences;

    @Column(name = "high_risk_count")
    private Integer highRiskCount;

    @Column(name = "overall_risk_score")
    private BigDecimal overallRiskScore;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
}