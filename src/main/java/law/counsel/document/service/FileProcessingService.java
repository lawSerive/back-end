package law.counsel.document.service;

import law.counsel.document.domain.Document;
import law.counsel.document.repository.DocumentRepository;
import law.counsel.member.Member;
import law.counsel.member.repository.MemberRepository;
import law.counsel.ai.service.OpenAIService;
import law.counsel.analysis.dto.DocumentAnalysisResponse;
import law.counsel.document.service.ocr.OCRService;
import law.counsel.document.service.ocr.OCRException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final DocumentRepository documentRepository;
    private final MemberRepository memberRepository;
    private final OCRService ocrService;
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;

    @Value("${file.upload.directory:./uploads}")
    private String uploadDirectory;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/tiff",
            "application/pdf"
    );

    @Transactional
    public Document uploadAndProcessFile(MultipartFile file, Long memberId) throws FileProcessingException {
        try {
            validateFile(file);

            String savedFileName = saveFile(file);
            String filePath = Paths.get(uploadDirectory, savedFileName).toString();

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new FileProcessingException("Member not found: " + memberId));

            Document document = Document.builder()
                    .originalFilename(file.getOriginalFilename())
                    .fileName(savedFileName)
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .status(Document.ProcessingStatus.UPLOADED)
                    .member(member)
                    .build();

            document = documentRepository.save(document);
            log.info("Document saved with ID: {}", document.getId());
            
            // 트랜잭션 커밋 후 비동기 처리 시작
            Long documentId = document.getId();
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100); // 트랜잭션 커밋 대기
                    processDocumentAsync(documentId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            return document;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new FileProcessingException("File upload failed: " + e.getMessage(), e);
        }
    }

    public void processDocumentAsync(Long documentId) {
        try {
            log.info("Starting async processing for document ID: {}", documentId);
            processDocument(documentId);
        } catch (Exception e) {
            log.error("Async document processing failed for document ID: {}", documentId, e);
            updateDocumentStatus(documentId, Document.ProcessingStatus.FAILED, e.getMessage());
        }
    }

    @Transactional
    private void processDocument(Long documentId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found: " + documentId));

        try {
            updateDocumentStatus(documentId, Document.ProcessingStatus.OCR_PROCESSING, null);
            String extractedText = performOCR(document.getFilePath());

            document.setExtractedText(extractedText);
            document.setStatus(Document.ProcessingStatus.OCR_COMPLETED);
            documentRepository.save(document);
            log.info("OCR completed for document: {}", documentId);

            updateDocumentStatus(documentId, Document.ProcessingStatus.AI_PROCESSING, null);
            String interpretedText = performAIInterpretation(extractedText);

            document.setInterpretedText(interpretedText);
            documentRepository.save(document);
            log.info("AI interpretation completed for document: {}", documentId);

            updateDocumentStatus(documentId, Document.ProcessingStatus.RISK_ANALYSIS_PROCESSING, null);
            DocumentAnalysisResponse analysisResponse = performRiskAnalysis(extractedText);

            document.setImprovedText(analysisResponse.getImprovedText());
            document.setRiskAnalysisJson(objectMapper.writeValueAsString(analysisResponse.getRiskAnalyses()));
            document.setStatus(Document.ProcessingStatus.COMPLETED);
            documentRepository.save(document);
            log.info("Risk analysis completed for document: {}", documentId);

        } catch (Exception e) {
            log.error("Document processing failed for document: {}", documentId, e);
            updateDocumentStatus(documentId, Document.ProcessingStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    private String performOCR(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("File not found: " + filePath);
        }

        try {
            // PDF 파일인경우 텍스트 직접 추출 시도
            if (filePath.toLowerCase().endsWith(".pdf")) {
                return extractTextFromPDF(file);
            } else {
                // 이미지 파일인 경우 OCR 처리
                return ocrService.extractTextFromFile(file);
            }
        } catch (Exception e) {
            throw new Exception("Text extraction failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromPDF(File pdfFile) throws Exception {
        // 간단한 구현: PDF에서 텍스트 추출이 실패하면 OCR로 대체
        // 실제로는 Apache PDFBox 등을 사용하여 구현해야 합니다
        try {
            // TODO: PDF 텍스트 추출 라이브러리 구현
            // 현재는 OCR로 처리
            log.warn("PDF text extraction not implemented, falling back to OCR");
            return ocrService.extractTextFromFile(pdfFile);
        } catch (Exception e) {
            log.error("PDF processing failed, trying OCR fallback", e);
            return ocrService.extractTextFromFile(pdfFile);
        }
    }

    private String performAIInterpretation(String extractedText) throws Exception {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new Exception("No text available for AI interpretation");
        }

        try {
            return openAIService.interpretLegalDocument(extractedText);
        } catch (OpenAIService.AIInterpretationException e) {
            throw new Exception("AI interpretation failed: " + e.getMessage(), e);
        }
    }

    private DocumentAnalysisResponse performRiskAnalysis(String extractedText) throws Exception {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new Exception("No text available for risk analysis");
        }

        try {
            return openAIService.analyzeDocumentRisks(extractedText);
        } catch (OpenAIService.AIInterpretationException e) {
            throw new Exception("Risk analysis failed: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) throws FileProcessingException {
        if (file.isEmpty()) {
            throw new FileProcessingException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileProcessingException("File size exceeds maximum allowed size");
        }

        if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
            throw new FileProcessingException("Unsupported file type: " + file.getContentType());
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String savedFileName = timestamp + "_" + uuid + fileExtension;

        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return savedFileName;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    @Transactional
    private void updateDocumentStatus(Long documentId, Document.ProcessingStatus status, String errorMessage) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document != null) {
                document.setStatus(status);
                document.setErrorMessage(errorMessage);
                documentRepository.save(document);
                log.info("Document status updated to {} for document ID: {}", status, documentId);
            } else {
                log.error("Document not found when updating status: {}", documentId);
            }
        } catch (Exception e) {
            log.error("Failed to update document status for document: {}", documentId, e);
        }
    }

    public static class FileProcessingException extends Exception {
        public FileProcessingException(String message) {
            super(message);
        }

        public FileProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}