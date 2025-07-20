package law.counsel.document.repository;

import law.counsel.document.domain.ChecklistTemplate;
import law.counsel.document.domain.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {
    /** 가장 최신(버전이 큰) 활성 템플릿 한 건 */
    Optional<ChecklistTemplate> findByContractType(ContractType contractType);
}