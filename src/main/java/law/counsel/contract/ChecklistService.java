package law.counsel.contract;

import law.counsel.document.DocumentSentence;
import law.counsel.document.DocumentSentenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ContractRequiredClauseRepository clauseRepository;
    private final DocumentSentenceRepository sentenceRepository;
    private final DocumentChecklistRepository checklistRepository;

    // 체크리스트 생성/업데이트
    @Transactional
    public void generateChecklist(Long documentId, Long typeId) {
        // 1. 필수조항 불러오기
        List<ContractRequiredClause> requiredClauses = clauseRepository.findByType_TypeId(typeId);
        // 2. 문서 내 문장 불러오기
        List<DocumentSentence> sentences = sentenceRepository.findByDocumentId(documentId);

        // 3. 조항별 포함여부 판단
        for (ContractRequiredClause clause : requiredClauses) {
            boolean found = false;
            DocumentSentence matchedSentence = null;

            for (DocumentSentence sentence : sentences) {
                // 단순 키워드 매칭 예시 (추후 GPT 등 활용 가능)
                String[] keywords = clause.getDetectionKeywords().split(",");
                for (String keyword : keywords) {
                    if (sentence.getOriginalContent() != null && sentence.getOriginalContent().contains(keyword.trim())) {
                        found = true;
                        matchedSentence = sentence;
                        break;
                    }
                }
                if (found) break;
            }

            // 4. 결과 저장 (포함여부, 일치문장)
            DocumentChecklist checklist = new DocumentChecklist();
            checklist.setDocument(document); // 미리 Document 객체 받아오거나 documentId로 조회해서 세팅
            checklist.setRequiredClause(clause);
            checklist.setDocumentSentence(matchedSentence);
            checklist.setIsIncluded(found ? "Y" : "N");
            checklist.setMatchConfidence(found ? "HIGH" : "LOW");
            checklistRepository.save(checklist);
        }
    }
}
