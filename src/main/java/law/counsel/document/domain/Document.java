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

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private ContractType contractType; // 계약 유형
}