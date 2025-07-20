package law.counsel.analysis;

import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import law.counsel.analysis.dto.SimpleExplanationDto;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clauses")
public class SentenceAnalysisController {
    private final SentenceAnalysisService analysisService;


    @GetMapping("/{fileId}")
    public ResponseEntity<ResponseBody<List<SentenceAnalysis>>> getClauseAnalyses(@PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(analysisService.getAnalysesByDocumentId(fileId)));
    }

    /*
    쉬운 파일 보기
     */
    @GetMapping("/{fileId}/explanations")
    public ResponseEntity<ResponseBody<List<SimpleExplanationDto>>> getSimpleExplanations(@PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(analysisService.getExplanationsByDocumentId(fileId)));
    }

}

