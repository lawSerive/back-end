package law.counsel.document.service;

import law.counsel.document.domain.Document;
import law.counsel.document.dto.DocumentResponse;
import law.counsel.document.dto.DocumentUploadResponse;
import law.counsel.document.repository.DocumentRepository;
import law.counsel.analysis.dto.RiskAnalysisDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    /**
     * 주어진 memberId가 올린 문서 목록을 DTO로 변환해 반환
     */
    public List<DocumentResponse> listDocuments(Long memberId) {
        List<Document> docs = documentRepository.findByMember_MemberId(memberId);
        return docs.stream()
                .map(d -> DocumentResponse.builder()
                                .id(d.getId())
                                .fileName(d.getOriginalFilename())
                                .uploadedAt(d.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 문서 ID로 조회
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * 모든 문서 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<Document> getAllDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return documentRepository.findAll(pageable);
    }

    /**
     * 상태별 문서 조회
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByStatus(Document.ProcessingStatus status) {
        return documentRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 완료된 문서 조회
     */
    @Transactional(readOnly = true)
    public List<Document> getCompletedDocuments() {
        return getDocumentsByStatus(Document.ProcessingStatus.COMPLETED);
    }

    /**
     * 처리 중인 문서 조회
     */
    @Transactional(readOnly = true)
    public List<Document> getProcessingDocuments() {
        List<Document.ProcessingStatus> processingStatuses = List.of(
                Document.ProcessingStatus.UPLOADED,
                Document.ProcessingStatus.OCR_PROCESSING,
                Document.ProcessingStatus.OCR_COMPLETED,
                Document.ProcessingStatus.AI_PROCESSING,
                Document.ProcessingStatus.RISK_ANALYSIS_PROCESSING
        );

        return documentRepository.findAll()
                .stream()
                .filter(doc -> processingStatuses.contains(doc.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 실패한 문서 조회
     */
    @Transactional(readOnly = true)
    public List<Document> getFailedDocuments() {
        return getDocumentsByStatus(Document.ProcessingStatus.FAILED);
    }

    /**
     * 문서 삭제
     */
    @Transactional
    public boolean deleteDocument(Long id) {
        try {
            Optional<Document> documentOpt = documentRepository.findById(id);
            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();

                deleteFileFromSystem(document.getFilePath());
                documentRepository.delete(document);

                log.info("Document deleted successfully: {}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to delete document: {}", id, e);
            return false;
        }
    }

    /**
     * 문서 재처리 요청
     */
    @Transactional
    public boolean retryProcessing(Long id) {
        try {
            Optional<Document> documentOpt = documentRepository.findById(id);
            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();

                document.setExtractedText(null);
                document.setInterpretedText(null);
                document.setImprovedText(null);
                document.setRiskAnalysisJson(null);
                document.setErrorMessage(null);
                document.setStatus(Document.ProcessingStatus.UPLOADED);

                documentRepository.save(document);
                log.info("Document retry requested: {}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to retry document processing: {}", id, e);
            return false;
        }
    }

    /**
     * 분석 결과와 함께 문서 정보 조회 (업로드 응답용)
     */
    public DocumentUploadResponse getDocumentWithAnalysis(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Wait for processing completion if not completed yet
        if (document.getStatus() != Document.ProcessingStatus.COMPLETED && 
            document.getStatus() != Document.ProcessingStatus.FAILED) {
            
            waitForProcessingCompletion(documentId);
            document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        }

        return convertToUploadResponse(document);
    }

    private void waitForProcessingCompletion(Long documentId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            int maxAttempts = 60; // 60 seconds max wait
            int attempts = 0;
            
            while (attempts < maxAttempts) {
                try {
                    Thread.sleep(1000); // Wait 1 second
                    Document doc = documentRepository.findById(documentId).orElse(null);
                    
                    if (doc != null && (doc.getStatus() == Document.ProcessingStatus.COMPLETED || 
                                       doc.getStatus() == Document.ProcessingStatus.FAILED)) {
                        break;
                    }
                    
                    attempts++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        try {
            future.get(65, TimeUnit.SECONDS); // Wait up to 65 seconds
        } catch (Exception e) {
            log.warn("Timeout waiting for document processing completion: {}", documentId);
        }
    }

    private DocumentUploadResponse convertToUploadResponse(Document document) {
        DocumentUploadResponse.DocumentUploadResponseBuilder builder = DocumentUploadResponse.builder()
                .id(document.getId())
                .fileName(document.getOriginalFilename())
                .uploadedAt(document.getCreatedAt())
                .status(document.getStatus().name())
                .improvedText(document.getImprovedText());

        // Parse risk analysis JSON if available
        if (document.getRiskAnalysisJson() != null && !document.getRiskAnalysisJson().trim().isEmpty()) {
            try {
                List<RiskAnalysisDto> riskAnalyses = objectMapper.readValue(
                        document.getRiskAnalysisJson(),
                        new TypeReference<List<RiskAnalysisDto>>() {}
                );
                builder.riskAnalyses(riskAnalyses);
            } catch (Exception e) {
                log.error("Failed to parse risk analysis JSON for document: {}", document.getId(), e);
            }
        }

        return builder.build();
    }

    /**
     * 파일 시스템에서 파일 삭제
     */
    private void deleteFileFromSystem(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted from system: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Failed to delete file from system: {}", filePath, e);
        }
    }
}