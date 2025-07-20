package law.counsel.document.service;

import law.counsel.document.domain.Document;
import law.counsel.document.dto.DocumentListResponse;
import law.counsel.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    /**
     * 주어진 memberId가 올린 문서 목록을 DTO로 변환해 반환
     */
    public List<DocumentListResponse> listDocuments(Long memberId) {
        List<Document> docs = documentRepository.findByMember_MemberId(memberId);
        return docs.stream()
                .map(d -> new DocumentListResponse(
                        d.getId(),
                        d.getOriginalFilename(),
                        d.getCreatedAt(),
                        d.getFilePath()      // presigned URL 혹은 저장된 URL
                ))
                .collect(Collectors.toList());
    }
}