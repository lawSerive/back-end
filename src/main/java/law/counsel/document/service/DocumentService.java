package law.counsel.document.service;

import law.counsel.document.domain.Document;
import law.counsel.document.dto.DocumentResponse;
import law.counsel.document.repository.DocumentRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;

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
                Document.ProcessingStatus.AI_PROCESSING
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