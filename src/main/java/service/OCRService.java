package service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;

@Service
@Slf4j
public class OCRService {

    @Value("${tesseract.data-path:/usr/share/tesseract-ocr/4.00/tessdata}")
    private String tesseractDataPath;

    @Value("${tesseract.language:kor+eng}")
    private String tesseractLanguage;

    private ITesseract tesseract;

    @PostConstruct
    public void initialize() {
        tesseract = new Tesseract();

        // Tesseract 데이터 경로 설정
        if (new File(tesseractDataPath).exists()) {
            tesseract.setDatapath(tesseractDataPath);
        }

        // 언어 설정 (한국어 + 영어)
        tesseract.setLanguage(tesseractLanguage);

        // OCR 성능 향상을 위한 설정
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);

        log.info("Tesseract OCR initialized with language: {}", tesseractLanguage);
    }

    /**
     * 파일에서 텍스트 추출
     */
    public String extractTextFromFile(File imageFile) throws OCRException {
        try {
            log.info("Starting OCR processing for file: {}", imageFile.getName());
            String result = tesseract.doOCR(imageFile);
            log.info("OCR processing completed for file: {}", imageFile.getName());
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            log.error("OCR processing failed for file: {}", imageFile.getName(), e);
            throw new OCRException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * BufferedImage에서 텍스트 추출
     */
    public String extractTextFromImage(BufferedImage image) throws OCRException {
        try {
            log.info("Starting OCR processing for BufferedImage");
            String result = tesseract.doOCR(image);
            log.info("OCR processing completed for BufferedImage");
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            log.error("OCR processing failed for BufferedImage", e);
            throw new OCRException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * OCR 전용 예외 클래스
     */
    public static class OCRException extends Exception {
        public OCRException(String message) {
            super(message);
        }

        public OCRException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}