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
            
            // Tesseract 기본 설정 확인 (실제 테스트는 하지 않음)
            log.info("Tesseract configuration completed");
            
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
            log.error("Tesseract not available for file: {}", imageFile.getName());
            throw new OCRException("OCR 엔진이 사용 불가능합니다. Tesseract 설정을 확인해주세요.");
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
            log.error("Tesseract not available for BufferedImage");
            throw new OCRException("OCR 엔진이 사용 불가능합니다. Tesseract 설정을 확인해주세요.");
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