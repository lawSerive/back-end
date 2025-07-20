package law.counsel.document.repository;



import law.counsel.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // memberId 로 본인이 올린 문서만 조회
    List<Document> findByMember_MemberId(Long memberId);
}
