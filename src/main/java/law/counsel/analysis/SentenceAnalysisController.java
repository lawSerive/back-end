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
    쉬운 파일 보기
     */
    @GetMapping("/{documentId}/explanations")
    public ResponseEntity<ResponseBody<List<SimpleExplanationDto>>> getSimpleExplanations(@PathVariable("documentId") Long documentId) {
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(analysisService.getExplanationsByDocumentId(documentId)));
    }

    /*
    위험조항탐지 및 쉬운말해석 분석
     */
    @PostMapping("/{documentId}/risk-analysis")
    public ResponseEntity<ResponseBody<DocumentAnalysisResponse>> analyzeDocumentRisks(@PathVariable("documentId") Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.DOCUMENT_NOT_FOUND));
        
        String documentText = document.getExtractedText();
        if (documentText == null || documentText.trim().isEmpty()) {
            throw new BusinessException(ExceptionType.DOCUMENT_NOT_PROCESSED);
        }
        
        try {
            DocumentAnalysisResponse analysis = openAIService.analyzeDocumentRisks(documentText);
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(analysis));
        } catch (OpenAIService.AIInterpretationException e) {
            throw new BusinessException(ExceptionType.ANALYSIS_FAILED);
        }
    }

    /*
    위험조항탐지 (하이라이팅된 OCR 수정본 + 위험조항 정보)
     */
    @GetMapping("/{documentId}/risk-detection")
    public ResponseEntity<ResponseBody<RiskDetectionResponse>> getRiskDetection(@PathVariable("documentId") Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.DOCUMENT_NOT_FOUND));
        
        // 기존 분석 결과 사용 (업로드 시 이미 처리됨)
        String improvedText = document.getImprovedText();
        String riskAnalysisJson = document.getRiskAnalysisJson();
        
        if (improvedText == null || riskAnalysisJson == null) {
            throw new BusinessException(ExceptionType.ANALYSIS_DATA_NOT_AVAILABLE);
        }
        
        try {
            // JSON에서 위험조항 데이터 파싱
            List<RiskAnalysisDto> riskAnalyses = objectMapper.readValue(
                    riskAnalysisJson, new TypeReference<List<RiskAnalysisDto>>() {}
            );
            
            // 하이라이트 정보 생성 (원본 텍스트에서 위치 찾기)
            List<RiskHighlightDto> riskHighlights = createRiskHighlights(improvedText, riskAnalyses);
            
            RiskDetectionResponse response = new RiskDetectionResponse(improvedText, riskHighlights);
            return ResponseEntity.ok(ResponseUtil.createSuccessResponse(response));
            
        } catch (Exception e) {
            throw new BusinessException(ExceptionType.RISK_DETECTION_FAILED);
        }
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

    /*
    OCR 가독성 향상된 텍스트 조회
     */
    @GetMapping("/{documentId}/improved-text")
    public ResponseEntity<ResponseBody<String>> getImprovedText(@PathVariable("documentId") Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ExceptionType.DOCUMENT_NOT_FOUND));
        
        // 기존 분석 결과 사용 (업로드 시 이미 처리됨)
        String improvedText = document.getImprovedText();
        
        if (improvedText == null) {
            throw new BusinessException(ExceptionType.ANALYSIS_DATA_NOT_AVAILABLE);
        }
        
        return ResponseEntity.ok(ResponseUtil.createSuccessResponse(improvedText));
    }

    /*
    하이라이트 정보 생성 헬퍼 메서드
     */
    private List<RiskHighlightDto> createRiskHighlights(String improvedText, List<RiskAnalysisDto> riskAnalyses) {
        List<RiskHighlightDto> highlights = new ArrayList<>();
        
        for (RiskAnalysisDto risk : riskAnalyses) {
            String originalText = risk.getOriginalText();
            if (originalText != null && !originalText.trim().isEmpty()) {
                int startIndex = improvedText.indexOf(originalText);
                if (startIndex != -1) {
                    int endIndex = startIndex + originalText.length();
                    
                    RiskHighlightDto highlight = new RiskHighlightDto(
                            risk.getSentenceId(),
                            risk.getRiskLevel(),
                            risk.getOriginalText(),
                            risk.getRiskReason(),
                            risk.getLegalReferences(),
                            startIndex,
                            endIndex
                    );
                    highlights.add(highlight);
                } else {
                    // 정확히 매칭되지 않으면 유사한 텍스트 찾기 (간단한 구현)
                    int approximateIndex = findApproximateMatch(improvedText, originalText);
                    if (approximateIndex != -1) {
                        RiskHighlightDto highlight = new RiskHighlightDto(
                                risk.getSentenceId(),
                                risk.getRiskLevel(),
                                risk.getOriginalText(),
                                risk.getRiskReason(),
                                risk.getLegalReferences(),
                                approximateIndex,
                                approximateIndex + originalText.length()
                        );
                        highlights.add(highlight);
                    }
                }
            }
        }
        
        return highlights;
    }

    /*
    유사한 텍스트 찾기 헬퍼 메서드
     */
    private int findApproximateMatch(String text, String target) {
        // 간단한 키워드 기반 매칭
        String[] keywords = target.split("\\s+");
        for (String keyword : keywords) {
            if (keyword.length() > 3) { // 3글자 이상의 키워드만
                int index = text.indexOf(keyword);
                if (index != -1) {
                    return index;
                }
            }
        }
        return -1;
    }

}

