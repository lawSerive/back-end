package law.counsel.document.repository;

import law.counsel.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // memberId 로 본인이 올린 문서만 조회
    List<Document> findByMember_MemberId(Long memberId);
    
    // 상태별 문서 조회 (생성일 기준 내림차순)
    List<Document> findByStatusOrderByCreatedAtDesc(Document.ProcessingStatus status);
    
    // 특정 회원의 상태별 문서 조회
    List<Document> findByMember_MemberIdAndStatusOrderByCreatedAtDesc(Long memberId, Document.ProcessingStatus status);
}
