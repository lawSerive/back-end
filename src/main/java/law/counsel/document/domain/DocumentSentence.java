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
@Table(name = "document_sentences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentSentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sentence_id")
    private Long sentenceId;

    @Column(name = "sentence_order")
    private Integer sentenceOrder;  // 문장의 해당 페이지 내 순서

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;  // 원본 문장 텍스트

    @Column(name = "position_coordinates", columnDefinition = "JSON")
    private String positionCoordinates; // 사각형 좌표 json {x,y,w,h}

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private DocumentPage page;
}
