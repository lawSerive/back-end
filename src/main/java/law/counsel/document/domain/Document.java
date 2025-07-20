package law.counsel.document.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import law.counsel.contract.ContractType;
import law.counsel.global.response.AuditEntity;
import law.counsel.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;


@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
문서 정보
 */
public class Document extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long id;

    @Column(name = "original_filename")
    private String originalFilename; // 업로드된 원본 파일명

    @Column(name = "file_path")
    private String filePath; // S3 PresignedURL

    private String status;

    @Column(name = "analysis_progress")
    private BigDecimal analysisProgress;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private ContractType contractType; // 계약 유형
}