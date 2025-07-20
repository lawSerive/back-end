package law.counsel.document.controller;

import law.counsel.document.dto.DocumentResponse;
import law.counsel.document.service.DocumentService;
import law.counsel.global.jwt.annotation.CurrentMemberId;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;


    /*
    문서 전체조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<DocumentResponse>>> listMyDocuments(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documentService.listDocuments(memberId)));
    }
}
