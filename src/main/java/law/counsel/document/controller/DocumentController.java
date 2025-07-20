package law.counsel.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import law.counsel.document.api.DocumentApi;
import law.counsel.document.domain.Document;
import law.counsel.document.dto.DocumentResponse;
import law.counsel.document.dto.DocumentUploadResponse;
import law.counsel.document.dto.DocumentStatusResponse;
import law.counsel.analysis.dto.RiskAnalysisDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import law.counsel.document.service.DocumentService;
import law.counsel.document.service.FileProcessingService;
import law.counsel.document.repository.DocumentRepository;
import law.counsel.global.jwt.annotation.CurrentMemberId;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import law.counsel.global.exception.BusinessException;
import law.counsel.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;
    private final FileProcessingService fileProcessingService;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<List<DocumentResponse>>> listMyDocuments(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(documentService.listDocuments(memberId)));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<Boolean>> retryProcessing(@PathVariable Long id) {
        boolean success = documentService.retryProcessing(id);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(true));
    }

    /**
     * 파일 업로드 및 처리 시작 (폴링 방식)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "이미지 파일을 업로드하고 OCR 및 AI 해석 처리를 시작합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResponseBody<DocumentUploadResponse>> uploadFile(@Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file, 
            @RequestParam(value = "waitForCompletion", defaultValue = "false") boolean waitForCompletion,
            @CurrentMemberId Long memberId) {

        try {
            log.info("File upload request received: {}", file.getOriginalFilename());

            Document document = fileProcessingService.uploadAndProcessFile(file, memberId);
            
            if (waitForCompletion) {
                // 기존 방식: 처리 완료까지 대기
                DocumentUploadResponse responseDto = documentService.getDocumentWithAnalysis(document.getId());
                return ResponseEntity.ok(ResponseUtil.createSuccessResponse(responseDto));
            } else {
                // 폴링 방식: 즉시 응답 (파일 ID와 상태만 반환)
                DocumentUploadResponse responseDto = DocumentUploadResponse.builder()
                        .id(document.getId())
                        .fileName(document.getOriginalFilename())
                        .uploadedAt(document.getCreatedAt())
                        .status(document.getStatus().name())
                        .build();
                return ResponseEntity.ok(ResponseUtil.createSuccessResponse(responseDto));
            }

        } catch (FileProcessingService.FileProcessingException e) {
            log.error("File upload failed", e);
            if (e.getMessage().contains("Unsupported file type")) {
                throw new BusinessException(ExceptionType.UNSUPPORTED_FILE_TYPE);
            } else if (e.getMessage().contains("File size exceeds")) {
                throw new BusinessException(ExceptionType.FILE_SIZE_EXCEEDED);
            } else {
                throw new BusinessException(ExceptionType.FILE_UPLOAD_FAILED);
            }
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            throw new BusinessException(ExceptionType.UNEXPECTED_SERVER_ERROR);
        }
    }

    /**
     * 문서 처리 상태 조회
     */
    @GetMapping("/{documentId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<DocumentStatusResponse>> getDocumentStatus(@PathVariable Long documentId, @CurrentMemberId Long memberId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.DOCUMENT_NOT_FOUND));
        
        // 소유자 확인
        if (!document.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ExceptionType.ACCESS_DENIED);
        }
        
        DocumentStatusResponse response = DocumentStatusResponse.builder()
                .documentId(document.getId())
                .fileName(document.getOriginalFilename())
                .status(document.getStatus().name())
                .isCompleted(document.getStatus() == Document.ProcessingStatus.COMPLETED)
                .isFailed(document.getStatus() == Document.ProcessingStatus.FAILED)
                .errorMessage(document.getErrorMessage())
                .build();
                
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(response));
    }

    /**
     * 문서 분석 결과 조회 (분석 페이지용 - 개선된 텍스트 + 위험 조항 데이터)
     */
    @GetMapping("/{documentId}/analysis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseBody<DocumentUploadResponse>> getDocumentAnalysis(@PathVariable Long documentId, @CurrentMemberId Long memberId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.DOCUMENT_NOT_FOUND));
        
        // 소유자 확인
        if (!document.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ExceptionType.ACCESS_DENIED);
        }
        
        // 분석이 완료되지 않은 경우
        if (document.getStatus() != Document.ProcessingStatus.COMPLETED) {
            throw new BusinessException(ExceptionType.ANALYSIS_NOT_COMPLETED);
        }
        
        DocumentUploadResponse response = documentService.getDocumentWithAnalysis(documentId);
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(response));
    }

}
