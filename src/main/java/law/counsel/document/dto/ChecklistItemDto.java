package law.counsel.document.dto;

public record ChecklistItemDto(
        Long itemId,
        Integer itemIndex,
        String question,
        Boolean isChecked      // Boolean
) {}