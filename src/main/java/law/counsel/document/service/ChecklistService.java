package law.counsel.document.service;

import jakarta.transaction.Transactional;
import law.counsel.document.domain.*;
import law.counsel.document.dto.ChecklistBatchSaveDto;
import law.counsel.document.dto.ChecklistItemDto;
import law.counsel.document.repository.ChecklistTemplateItemRepository;
import law.counsel.document.repository.ChecklistTemplateRepository;
import law.counsel.document.repository.DocumentChecklistResponseRepository;
import law.counsel.document.repository.DocumentRepository;
import law.counsel.global.exception.BusinessException;
import law.counsel.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistTemplateRepository tplRepo;
    private final ChecklistTemplateItemRepository itemRepo;
    private final DocumentChecklistResponseRepository respRepo;
    private final DocumentRepository docRepo;

    /* ① 템플릿 + 사용자 응답 결합 */
    public List<ChecklistItemDto> loadForDocument(Long docId, ContractType type) {

        // 템플릿 있는지 확인하기
        ChecklistTemplate tpl = tplRepo.findByContractType(type)
                .orElseThrow(() -> new BusinessException(ExceptionType.TEMPLATE_NOT_FOUND));

        // 템플릿 아이디를 통해 상세 질문 가져오기
        List<ChecklistTemplateItem> items =
                itemRepo.findByTemplate_TemplateIdOrderByItemIndex(tpl.getTemplateId());


        Map<Long, Boolean> respMap = respRepo.findByDocument_Id(docId).stream()
                .collect(Collectors.toMap(r -> r.getItem().getItemId(), DocumentChecklistResponse::getIsChecked));

        return items.stream()
                .map(it -> new ChecklistItemDto(
                        it.getItemId(),
                        it.getItemIndex(),
                        it.getQuestion(),
                        respMap.getOrDefault(it.getItemId(), Boolean.FALSE)
                ))
                .toList();
    }

    @Transactional
    public void saveResponses(Long docId, ChecklistBatchSaveDto dto) {

        Document doc = docRepo.getReferenceById(docId);

        // 해당 문서의 기존 응답을 Map<itemId, resp 엔티티>
        Map<Long, DocumentChecklistResponse> existing =
                respRepo.findByDocument_Id(docId).stream()
                        .collect(Collectors.toMap(
                                r -> r.getItem().getItemId(), r -> r));

        for (ChecklistBatchSaveDto.Item i : dto.items()) {

            // 문항 프록시
            ChecklistTemplateItem item = itemRepo.getReferenceById(i.itemId());

            // 있으면 업데이트, 없으면 새로 생성
            DocumentChecklistResponse resp = existing.getOrDefault(
                    i.itemId(),
                    DocumentChecklistResponse.builder()
                            .document(doc)
                            .item(item)
                            .build());

            resp.setIsChecked(i.isChecked());
            resp.setCheckedAt(LocalDateTime.now());
            respRepo.save(resp);
        }
    }
}
