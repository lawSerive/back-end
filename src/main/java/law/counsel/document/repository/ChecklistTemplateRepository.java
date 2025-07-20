package law.counsel.document.repository;

import law.counsel.document.domain.ChecklistTemplate;
import law.counsel.document.domain.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

    Optional<ChecklistTemplate> findByContractType(ContractType contractType);
}