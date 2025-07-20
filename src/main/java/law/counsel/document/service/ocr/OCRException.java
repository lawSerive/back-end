package law.counsel.document.service.ocr;

/**
 * OCR 처리 중 발생하는 예외를 나타내는 클래스
 */
public class OCRException extends Exception {

    /**
     * 기본 생성자
     */
    public OCRException() {
        super();
    }

    /**
     * 메시지와 함께 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public OCRException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인과 함께 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @param cause 예외의 원인
     */
    public OCRException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인과 함께 예외를 생성합니다.
     *
     * @param cause 예외의 원인
     */
    public OCRException(Throwable cause) {
        super(cause);
    }
}