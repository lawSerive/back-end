package law.counsel.analysis;

// law/counsel/analysis/SentenceAnalysisService.java

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import law.counsel.analysis.dto.SimpleExplanationDto;

@Service
@RequiredArgsConstructor
public class SentenceAnalysisService {
    private final SentenceAnalysisRepository analysisRepository;

    public List<SentenceAnalysis> getAnalysesBySentenceId(Long sentenceId) {
        return analysisRepository.findBySentence_SentenceId(sentenceId);
    }

    public List<SimpleExplanationDto> getExplanationsBySentenceId(Long sentenceId) {
        List<SentenceAnalysis> analyses = analysisRepository.findBySentence_SentenceId(sentenceId);
        return analyses.stream()
                .map(a -> new SimpleExplanationDto(
                        a.getSentence().getSentenceId(),
                        a.getSimpleExplanation(),
                        a.getRiskLevel(),
                        a.getSuggestedRevision()
                ))
                .toList();
    }

    public List<SentenceAnalysis> getAnalysesByDocumentId(Long documentId) {
        return analysisRepository.findBySentence_Document_Id(documentId);
    }

    public List<SimpleExplanationDto> getExplanationsByDocumentId(Long documentId) {
        List<SentenceAnalysis> analyses = analysisRepository.findBySentence_Document_Id(documentId);
        return analyses.stream()
                .map(a -> new SimpleExplanationDto(
                        a.getSentence().getSentenceId(),
                        a.getSimpleExplanation(),
                        a.getRiskLevel(),
                        a.getSuggestedRevision()
                ))
                .toList();
    }
}