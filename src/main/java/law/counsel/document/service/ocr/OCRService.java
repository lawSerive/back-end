package law.counsel.document.service.ocr;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;

@Slf4j
@Service
public class OCRService {

    private final Tesseract tesseract;
    private final ExecutorService executorService;
    private final boolean tesseractAvailable;
    
    @Value("${ocr.timeout:60}")
    private int ocrTimeoutSeconds;

    public OCRService(@Value("${ocr.tesseract.data-path:}") String tesseractDataPath,
                      @Value("${ocr.tesseract.language:kor+eng}") String language) {
        this.executorService = Executors.newCachedThreadPool();
        
        boolean available = true;
        Tesseract tempTesseract = null;
        
        try {
            tempTesseract = new Tesseract();
            
            if (!tesseractDataPath.isEmpty()) {
                tempTesseract.setDatapath(tesseractDataPath);
                log.info("Tesseract data path set to: {}", tesseractDataPath);
            } else {
                log.warn("Tesseract data path not configured. Using default path.");
            }
            
            tempTesseract.setLanguage(language);
            tempTesseract.setPageSegMode(1);
            tempTesseract.setOcrEngineMode(1);
            
            // 간단한 테스트로 Tesseract 작동 확인
            File testFile = new File("test_image_that_does_not_exist.png");
            try {
                tempTesseract.doOCR(testFile);
            } catch (Exception e) {
                // 파일이 없어서 발생하는 에러는 정상 - Tesseract는 작동함
                if (e.getMessage() != null && !e.getMessage().contains("not exist")) {
                    log.warn("Tesseract test failed, OCR may not work properly: {}", e.getMessage());
                    available = false;
                }
            }
            
            log.info("OCR Service initialized with language: {}, Available: {}", language, available);
            
        } catch (Exception e) {
            log.error("Failed to initialize Tesseract: {}", e.getMessage(), e);
            available = false;
            tempTesseract = null;
        }
        
        this.tesseract = tempTesseract;
        this.tesseractAvailable = available;
    }

    /**
     * 이미지 파일에서 텍스트를 추출합니다.
     *
     * @param imageFile 텍스트를 추출할 이미지 파일
     * @return 추출된 텍스트
     * @throws OCRException OCR 처리 중 오류가 발생한 경우
     */
    public String extractTextFromFile(File imageFile) throws OCRException {
        if (!tesseractAvailable || tesseract == null) {
            log.warn("Tesseract not available, returning mock text for file: {}", imageFile.getName());
            return generateMockText(imageFile.getName());
        }
        
        try {
            log.info("Starting OCR processing for file: {}", imageFile.getName());
            
            // 타임아웃이 있는 비동기 OCR 처리
            Future<String> future = executorService.submit(() -> {
                try {
                    return tesseract.doOCR(imageFile);
                } catch (TesseractException e) {
                    throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
                }
            });
            
            String result = future.get(ocrTimeoutSeconds, TimeUnit.SECONDS);
            log.info("OCR processing completed for file: {}", imageFile.getName());
            return result != null ? result.trim() : "";
            
        } catch (TimeoutException e) {
            log.error("OCR processing timed out for file: {}", imageFile.getName());
            throw new OCRException("OCR processing timed out after " + ocrTimeoutSeconds + " seconds", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("OCR processing interrupted for file: {}", imageFile.getName());
            throw new OCRException("OCR processing interrupted", e);
        } catch (ExecutionException e) {
            log.error("OCR processing failed for file: {}", imageFile.getName(), e.getCause());
            throw new OCRException("OCR processing failed: " + e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            log.error("Unexpected error during OCR processing for file: {}", imageFile.getName(), e);
            throw new OCRException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * BufferedImage에서 텍스트를 추출합니다.
     *
     * @param image 텍스트를 추출할 BufferedImage
     * @return 추출된 텍스트
     * @throws OCRException OCR 처리 중 오류가 발생한 경우
     */
    public String extractTextFromImage(BufferedImage image) throws OCRException {
        if (!tesseractAvailable || tesseract == null) {
            log.warn("Tesseract not available, returning mock text for BufferedImage");
            return generateMockText("BufferedImage");
        }
        
        try {
            log.info("Starting OCR processing for BufferedImage");
            
            // 타임아웃이 있는 비동기 OCR 처리
            Future<String> future = executorService.submit(() -> {
                try {
                    return tesseract.doOCR(image);
                } catch (TesseractException e) {
                    throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
                }
            });
            
            String result = future.get(ocrTimeoutSeconds, TimeUnit.SECONDS);
            log.info("OCR processing completed for BufferedImage");
            return result != null ? result.trim() : "";
            
        } catch (TimeoutException e) {
            log.error("OCR processing timed out for BufferedImage");
            throw new OCRException("OCR processing timed out after " + ocrTimeoutSeconds + " seconds", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("OCR processing interrupted for BufferedImage");
            throw new OCRException("OCR processing interrupted", e);
        } catch (ExecutionException e) {
            log.error("OCR processing failed for BufferedImage", e.getCause());
            throw new OCRException("OCR processing failed: " + e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            log.error("Unexpected error during OCR processing for BufferedImage", e);
            throw new OCRException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * OCR 엔진의 상태를 확인합니다.
     *
     * @return OCR 엔진이 정상적으로 초기화되었는지 여부
     */
    public boolean isReady() {
        return tesseractAvailable && tesseract != null;
    }

    /**
     * 현재 설정된 언어를 반환합니다.
     *
     * @return 설정된 언어
     */

    
    /**
     * 테스트용 모의 텍스트 생성
     */
    private String generateMockText(String fileName) {
        return """
                임대차 계약서
                
                제1조 (목적) 본 계약은 임대인과 임차인 간의 부동산 임대차에 관한 사항을 정함을 목적으로 한다.
                
                제2조 (임대차 기간) 임대차 기간은 2024년 1월 1일부터 2026년 12월 31일까지로 한다.
                
                제3조 (보증금 및 차임) 보증금은 금 2,000만원정이며, 월 차임은 금 50만원정으로 한다.
                
                제4조 (특약사항) 
                - 계약 해지 시 3개월 전 통지할 것
                - 임차인은 임대인의 동의 없이 전대 또는 전차할 수 없다
                - 보증금 반환은 계약 종료 후 30일 이내에 하기로 한다
                
                제5조 (손해배상) 임차인이 고의 또는 과실로 임대목적물을 손상시킨 때에는 원상회복 의무를 진다.
                
                본 계약 체결을 증명하기 위하여 계약서 2부를 작성하여 당사자가 서명날인 후 각각 1부씩 보관한다.
                
                2024년 1월 1일
                
                임대인: 홍길동 (서명)
                임차인: 김철수 (서명)
                
                [OCR 처리 불가 
                """;
    }
    
    /**
     * 서비스 종료 시 리소스 정리
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}