package law.counsel.analysis;

// law/counsel/analysis/SentenceAnalysisController.java

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import law.counsel.analysis.dto.SimpleExplanationDto;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clauses")
public class SentenceAnalysisController {
    private final SentenceAnalysisService analysisService;

    @GetMapping("/{fileId}")
    public List<SentenceAnalysis> getClauseAnalyses(@PathVariable("fileId") Long fileId) {
        return analysisService.getAnalysesByDocumentId(fileId);
    }

    // law/counsel/analysis/SentenceAnalysisController.java


    @GetMapping("/{fileId}/explanations")
    public List<SimpleExplanationDto> getSimpleExplanations(@PathVariable("fileId") Long fileId) {
        return analysisService.getExplanationsByDocumentId(fileId);
    }

}

