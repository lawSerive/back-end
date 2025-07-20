package law.counsel.document.service;

import law.counsel.document.domain.Document;
import law.counsel.document.dto.DocumentResponse;
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
}