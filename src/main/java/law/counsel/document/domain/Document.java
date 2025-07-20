package law.counsel.document.domain;

import jakarta.persistence.*;
import law.counsel.global.response.AuditEntity;
import law.counsel.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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

    @Column(name = "file_name")
    private String fileName; // 저장된 파일명

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private ContractType contractType; // 계약 유형

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String interpretedText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String improvedText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String riskAnalysisJson;

    private String errorMessage;


    public enum ProcessingStatus {
        UPLOADED,
        OCR_PROCESSING,
        OCR_COMPLETED,
        AI_PROCESSING,
        RISK_ANALYSIS_PROCESSING,
        COMPLETED,
        FAILED
    }
}