package law.counsel.analysis;

import law.counsel.ai.service.OpenAIService;
import law.counsel.analysis.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import law.counsel.document.domain.Document;
import law.counsel.document.repository.DocumentRepository;
import law.counsel.global.response.ResponseBody;
import law.counsel.global.response.ResponseUtil;
import law.counsel.global.exception.BusinessException;
import law.counsel.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clauses")
public class SentenceAnalysisController {
    private final SentenceAnalysisService analysisService;
    private final OpenAIService openAIService;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;


    @GetMapping("/{documentId}")
    public ResponseEntity<ResponseBody<List<SentenceAnalysis>>> getClauseAnalyses(@PathVariable("documentId") Long documentId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(analysisService.getAnalysesByDocumentId(documentId)));
    }


    /*
    쉬운말해석 (쉬운설명 + 권장문장)
     */
    @GetMapping("/{documentId}/simple-interpretation")
    public ResponseEntity<ResponseBody<SimpleInterpretationResponse>> getSimpleInterpretation(@PathVariable("documentId") Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.DOCUMENT_NOT_FOUND));
        
        // 기존 분석 결과 사용 (업로드 시 이미 처리됨)
        String riskAnalysisJson = document.getRiskAnalysisJson();
        
        if (riskAnalysisJson == null) {
            throw new BusinessException(ExceptionType.ANALYSIS_DATA_NOT_AVAILABLE);
        }
        
        try {
            // JSON에서 위험조항 데이터 파싱
            List<RiskAnalysisDto> riskAnalyses = objectMapper.readValue(
                    riskAnalysisJson, new TypeReference<List<RiskAnalysisDto>>() {}
            );
            
            // 쉬운말해석 데이터로 변환
            List<SimpleExplanationItem> explanations = riskAnalyses.stream()
                    .map(risk -> new SimpleExplanationItem(
                            risk.getSentenceId(),
                            risk.getRiskLevel(),
                            risk.getOriginalText(),
                            risk.getSimpleExplanation(),
                            risk.getSuggestedRevision()
                    ))
                    .toList();
            
            SimpleInterpretationResponse response = new SimpleInterpretationResponse(explanations);
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(response));
            
        } catch (Exception e) {
            throw new BusinessException(ExceptionType.SIMPLE_INTERPRETATION_FAILED);
        }
    }
}

