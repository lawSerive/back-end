package service;

import dto.DocumentResponseDto;
import entity.Document;
import Repository.DocumentRepository;
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
     * 문서 ID로 조회
     */
    @Transactional(readOnly = true)
    public Optional<DocumentResponseDto> getDocumentById(Long id) {
        return documentRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 모든 문서 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<DocumentResponseDto> getAllDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return documentRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 상태별 문서 조회
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getDocumentsByStatus(Document.ProcessingStatus status) {
        return documentRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 완료된 문서 조회
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getCompletedDocuments() {
        return getDocumentsByStatus(Document.ProcessingStatus.COMPLETED);
    }

    /**
     * 처리 중인 문서 조회
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getProcessingDocuments() {
        List<Document.ProcessingStatus> processingStatuses = List.of(
                Document.ProcessingStatus.UPLOADED,
                Document.ProcessingStatus.OCR_PROCESSING,
                Document.ProcessingStatus.OCR_COMPLETED,
                Document.ProcessingStatus.AI_PROCESSING
        );

        return documentRepository.findAll()
                .stream()
                .filter(doc -> processingStatuses.contains(doc.getStatus()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 실패한 문서 조회
     */
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getFailedDocuments() {
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

                // 파일 시스템에서 파일 삭제
                deleteFileFromSystem(document.getFilePath());

                // 데이터베이스에서 삭제
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

                // 이전 결과 초기화
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

    /**
     * Entity를 DTO로 변환
     */
    private DocumentResponseDto convertToDto(Document document) {
        return DocumentResponseDto.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .fileSize(document.getFileSize())
                .mimeType(document.getMimeType())
                .status(document.getStatus())
                .extractedText(document.getExtractedText())
                .interpretedText(document.getInterpretedText())
                .errorMessage(document.getErrorMessage())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }