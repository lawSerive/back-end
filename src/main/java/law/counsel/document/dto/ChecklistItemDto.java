package law.counsel.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체크리스트 항목")
public record ChecklistItemDto(
        @Schema(description = "항목 ID", example = "1")
        Long itemId,
        
        @Schema(description = "항목 순서", example = "1")
        Integer itemIndex,
        
        @Schema(description = "체크리스트 질문", example = "계약서에 서명이 있나요?")
        String question,
        
        @Schema(description = "체크 여부", example = "true")
        Boolean isChecked
) {}