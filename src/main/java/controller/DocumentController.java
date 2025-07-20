package controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import dto.ApiResponse;
import dto.DocumentResponseDto;
import entity.Document;
import service.DocumentService;
import service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document", description = "문서 OCR 및 AI 해석 API")
public class DocumentController {

    private final FileProcessingService fileProcessingService;
    private final DocumentService documentService;

    /**
     * 파일 업로드 및 처리 시작
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "이미지 파일을 업로드하고 OCR 및 AI 해석 처리를 시작합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadFile(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("File upload request received: {}", file.getOriginalFilename());

            Document document = fileProcessingService.uploadAndProcessFile(file);
            DocumentResponseDto responseDto = documentService.getDocumentById(document.getId())
                    .orElseThrow(() -> new RuntimeException("Document not found after upload"));

            return ResponseEntity.ok(ApiResponse.success("파일이 성공적으로 업로드되었습니다. 처리가 시작됩니다.", responseDto));

        } catch (FileProcessingService.FileProcessingException e) {
            log.error("File upload failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일 업로드 실패", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
    }

    /**
     * 문서 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "문서 조회", description = "ID로 특정 문서 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<DocumentResponseDto>> getDocument(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long id) {

        return documentService.getDocumentById(id)
                .map(doc -> ResponseEntity.ok(ApiResponse.success(doc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 모든 문서 조회 (페이징)
     */
    @GetMapping
    @Operation(summary = "문서 목록 조회", description = "모든 문서를 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<DocumentResponseDto>>> getAllDocuments(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {

        Page<DocumentResponseDto> documents = documentService.getAllDocuments(page, size);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * 완료된 문서 조회
     */
    @GetMapping("/completed")
    @Operation(summary = "완료된 문서 조회", description = "처리가 완료된 문서들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getCompletedDocuments() {
        List<DocumentResponseDto> documents = documentService.getCompletedDocuments();
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * 처리 중인 문서 조회
     */
    @GetMapping("/processing")
    @Operation(summary = "처리 중인 문서 조회", description = "현재 처리 중인 문서들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getProcessingDocuments() {
        List<DocumentResponseDto> documents = documentService.getProcessingDocuments();
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * 실패한 문서 조회
     */
    @GetMapping("/failed")
    @Operation(summary = "실패한 문서 조회", description = "처리에 실패한 문서들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getFailedDocuments() {
        List<DocumentResponseDto> documents = documentService.getFailedDocuments();
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * 문서 재처리
     */
    @PostMapping("/{id}/retry")
    @Operation(summary = "문서 재처리", description = "실패한 문서를 다시 처리합니다.")
    public ResponseEntity<ApiResponse<String>> retryDocument(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long id) {

        boolean success = documentService.retryProcessing(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("문서 재처리가 시작되었습니다."));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("문서 재처리에 실패했습니다."));
        }
    }

    /**
     * 문서 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "문서 삭제", description = "문서와 관련 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @Parameter(description = "문서 ID", required = true)
            @PathVariable Long id) {

        boolean success = documentService.deleteDocument(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("문서가 삭제되었습니다."));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("문서 삭제에 실패했습니다."));
        }
    }

    /**
     * 상태별 문서 조회
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "상태별 문서 조회", description = "특정 상태의 문서들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getDocumentsByStatus(
            @Parameter(description = "문서 상태", required = true)
            @PathVariable Document.ProcessingStatus status) {

        List<DocumentResponseDto> documents = documentService.getDocumentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }
}