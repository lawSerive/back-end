package law.counsel.document.domain;

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
@Table(name = "document_pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private Long pageId;

    @Column(name = "page_number")
    private Integer pageNumber;  // 페이지 번호

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;  // 페이지 전체 텍스트

    @Column(name = "ocr_confidence", columnDefinition = "JSON")
    private String ocrConfidence;  // OCR 신뢰도 정보

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
}