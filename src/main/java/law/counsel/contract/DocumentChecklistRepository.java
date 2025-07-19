package law.counsel.contract;

import law.counsel.document.DocumentChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentChecklistRepository extends JpaRepository<DocumentChecklist, Long> {
    List<DocumentChecklist> findByDocument_DocumentId(Long documentId);
    List<DocumentChecklist> findByDocument_DocumentIdAndRequiredClause_Type_TypeId(Long documentId, Long typeId);
}
