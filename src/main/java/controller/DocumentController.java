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


}