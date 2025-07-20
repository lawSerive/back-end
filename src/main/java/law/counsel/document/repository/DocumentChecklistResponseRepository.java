package law.counsel.document.repository;

import law.counsel.document.domain.DocumentChecklistResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentChecklistResponseRepository extends JpaRepository<DocumentChecklistResponse, Long> {
    List<DocumentChecklistResponse> findByDocument_Id(Long docId);
}