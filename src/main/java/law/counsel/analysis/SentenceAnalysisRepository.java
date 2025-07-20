package law.counsel.analysis;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SentenceAnalysisRepository extends JpaRepository<SentenceAnalysis, Long> {

    List<SentenceAnalysis> findBySentence_Document_Id(Long documentId);
}