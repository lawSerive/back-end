package law.counsel.document.controller;

import law.counsel.document.api.DocumentApi;
import law.counsel.document.domain.Document;
import law.counsel.document.dto.DocumentResponse;
import law.counsel.document.service.DocumentService;
import law.counsel.document.service.FileProcessingService;
import law.counsel.global.jwt.annotation.CurrentMemberId;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;
    private final FileProcessingService fileProcessingService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<DocumentResponse>>> listMyDocuments(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documentService.listDocuments(memberId)));
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<Document>> uploadDocument(@RequestParam("file") MultipartFile file) {
        Document document = fileProcessingService.uploadAndProcessFile(file);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(document));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<Boolean>> retryProcessing(@PathVariable Long id) {
        boolean success = documentService.retryProcessing(id);
        if (success) {
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(true));
        } else {
            return ResponseEntity.badRequest().body(ResponseUtil.createErrorResponse("Failed to retry processing"));
        }
    }

}
