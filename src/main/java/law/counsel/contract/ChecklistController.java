package law.counsel.contract;

import law.counsel.document.DocumentChecklist;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checklist")
public class ChecklistController {
    private final ChecklistService checklistService;
    private final DocumentChecklistRepository checklistRepository;

    // 체크리스트 생성
    @PostMapping("/{fileId}")
    public String generateChecklist(@PathVariable("fileId") Long fileId, @RequestParam Long typeId) {
        checklistService.generateChecklist(fileId, typeId);
        return "체크리스트 생성 완료";
    }

    // 체크리스트 결과 조회
    @GetMapping("/{fileId}")
    public List<DocumentChecklist> getChecklist(@PathVariable("fileId") Long fileId, @RequestParam Long typeId) {
        return checklistRepository.findByDocument_DocumentIdAndRequiredClause_Type_TypeId(fileId, typeId);
    }
}
