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
        try {
            Document document = fileProcessingService.uploadAndProcessFile(file);
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(document));
        } catch (FileProcessingService.FileProcessingException e) {
            return ResponseEntity.badRequest().body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<Document>> getDocument(@PathVariable Long id) {
        Optional<Document> document = documentService.getDocumentById(id);
        if (document.isPresent()) {
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(document.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<Document>>> getDocumentsByStatus(@PathVariable Document.ProcessingStatus status) {
        List<Document> documents = documentService.getDocumentsByStatus(status);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documents));
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

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<Boolean>> deleteDocument(@PathVariable Long id) {
        boolean success = documentService.deleteDocument(id);
        if (success) {
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(true));
        } else {
            return ResponseEntity.badRequest().body(ResponseUtil.createErrorResponse("Failed to delete document"));
        }
    }

    @GetMapping("/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<Document>>> getCompletedDocuments() {
        List<Document> documents = documentService.getCompletedDocuments();
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documents));
    }

    @GetMapping("/processing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<Document>>> getProcessingDocuments() {
        List<Document> documents = documentService.getProcessingDocuments();
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documents));
    }

    @GetMapping("/failed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<Document>>> getFailedDocuments() {
        List<Document> documents = documentService.getFailedDocuments();
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documents));
    }
}
