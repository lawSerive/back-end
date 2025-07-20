package law.counsel.document.repository;

import law.counsel.document.domain.ChecklistTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistTemplateItemRepository extends JpaRepository<ChecklistTemplateItem, Long> {
    List<ChecklistTemplateItem>
    findByTemplate_TemplateIdOrderByItemIndex(Long templateId);
}
