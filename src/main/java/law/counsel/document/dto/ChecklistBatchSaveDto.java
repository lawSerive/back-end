package law.counsel.document.dto;

import java.util.List;

public record ChecklistBatchSaveDto(
        List<Item> items      // [{itemId, isChecked}, …]
) {
    public record Item(Long itemId, Boolean isChecked) {}
}