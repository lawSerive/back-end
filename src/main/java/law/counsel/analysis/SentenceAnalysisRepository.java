package law.counsel.analysis;

// law/counsel/analysis/SentenceAnalysisRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SentenceAnalysisRepository extends JpaRepository<SentenceAnalysis, Long> {
    List<SentenceAnalysis> findBySentence_Id(Long documentId);
}
