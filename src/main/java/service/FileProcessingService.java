package service;

import entity.Document;
import Repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    private final OCRService ocrService;
    private final OpenAIService openAIService;

    @Value("${file.upload.directory:./uploads}")
    private String uploadDirectory;

    @Value("${file.max-size:10485760}") // 10MB
    private long maxFileSize;

    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/tiff"
    );

    /**
     * 파일 업로드 및 비동기 처리 시작
     */
    @Transactional
    public Document uploadAndProcessFile(MultipartFile file) throws FileProcessingException {
        try {
            validateFile(file);

            // 파일 저장
            String savedFileName = saveFile(file);
            String filePath = Paths.get(uploadDirectory, savedFileName).toString();

            // 문서 엔티티 생성 및 저장
            Document document = Document.builder()
                    .fileName(savedFileName)
                    .originalFileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .status(Document.ProcessingStatus.UPLOADED)
                    .build();

            document = documentRepository.save(document);
            log.info("Document uploaded successfully: {}", document.getId());

            // 비동기 처리 시작
            processDocumentAsync(document.getId());

            return document;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new FileProcessingException("File upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * 문서 비동기 처리 (OCR + AI 해석)
     */
    @Transactional
    public CompletableFuture<Void> processDocumentAsync(Long documentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                processDocument(documentId);
            } catch (Exception e) {
                log.error("Async document processing failed for document ID: {}", documentId, e);
                updateDocumentStatus(documentId, Document.ProcessingStatus.FAILED, e.getMessage());
            }
        });
    }

    /**
     * 문서 처리 (OCR + AI 해석)
     */
    private void processDocument(Long documentId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new Exception("Document not found: " + documentId));

        try {
            // OCR 처리
            updateDocumentStatus(documentId, Document.ProcessingStatus.OCR_PROCESSING, null);
            String extractedText = performOCR(document.getFilePath());

            document.setExtractedText(extractedText);
            document.setStatus(Document.ProcessingStatus.OCR_COMPLETED);
            documentRepository.save(document);
            log.info("OCR completed for document: {}", documentId);

            // AI 해석 처리
            updateDocumentStatus(documentId, Document.ProcessingStatus.AI_PROCESSING, null);
            String interpretedText = performAIInterpretation(extractedText);

            document.setInterpretedText(interpretedText);
            document.setStatus(Document.ProcessingStatus.COMPLETED);
            documentRepository.save(document);
            log.info("AI interpretation completed for document: {}", documentId);

        } catch (Exception e) {
            log.error("Document processing failed for document: {}", documentId, e);
            updateDocumentStatus(documentId, Document.ProcessingStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    /**
     * OCR 수행
     */
    private String performOCR(String filePath) throws Exception {
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            throw new Exception("Image file not found: " + filePath);
        }

        try {
            return ocrService.extractTextFromFile(imageFile);
        } catch (OCRService.OCRException e) {
            throw new Exception("OCR processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * AI 해석 수행
     */
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

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) throws FileProcessingException {
        if (file.isEmpty()) {
            throw new FileProcessingException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileProcessingException("File size exceeds maximum allowed size");
        }

        if (!SUPPORTED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new FileProcessingException("Unsupported file type: " + file.getContentType());
        }
    }

    /**
     * 파일 저장
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유 파일명 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String savedFileName = timestamp + "_" + uuid + fileExtension;

        // 파일 저장
        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return savedFileName;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 문서 상태 업데이트
     */
    private void updateDocumentStatus(Long documentId, Document.ProcessingStatus status, String errorMessage) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document != null) {
                document.setStatus(status);
                document.setErrorMessage(errorMessage);
                documentRepository.save(document);
            }
        } catch (Exception e) {
            log.error("Failed to update document status for document: {}", documentId, e);
        }
    }

    /**
     * 파일 처리 전용 예외 클래스
     */
    public static class FileProcessingException extends Exception {
        public FileProcessingException(String message) {
            super(message);
        }

        public FileProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
