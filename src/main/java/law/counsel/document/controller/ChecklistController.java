package law.counsel.document.controller;

import law.counsel.document.api.ChecklistApi;
import law.counsel.document.domain.ContractType;
import law.counsel.document.dto.ChecklistBatchSaveDto;
import law.counsel.document.dto.ChecklistItemDto;
import law.counsel.document.service.ChecklistService;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  문서별 체크리스트 조회·저장 컨트롤러
 */
@RestController
@RequestMapping("/api/documents/{documentId}/checklist")
@RequiredArgsConstructor
public class ChecklistController implements ChecklistApi {

    private final ChecklistService checklistService;

    /*  체크리스트 + 사용자의 체크 상태 조회 */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<ChecklistItemDto>>> getChecklist(@PathVariable Long documentId, @RequestParam ContractType contractType) {
        List<ChecklistItemDto> result = checklistService.loadForDocument(documentId, contractType);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(result));
    }

    /*  체크 / 언체크 저장 */
    @PostMapping("/bulk")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<Void>> saveBulk(@PathVariable Long documentId, @RequestBody ChecklistBatchSaveDto dto) {
        checklistService.saveResponses(documentId, dto);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse());
    }
}
