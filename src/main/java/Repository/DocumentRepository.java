package Repository;


import entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatusOrderByCreatedAtDesc(Document.ProcessingStatus status);

    List<Document> findAllByOrderByCreatedAtDesc();

    @Query("SELECT d FROM Document d WHERE d.status = :status AND d.createdAt >= CURRENT_DATE")
    List<Document> findTodayByStatus(Document.ProcessingStatus status);

    Optional<Document> findByFileName(String fileName);
}