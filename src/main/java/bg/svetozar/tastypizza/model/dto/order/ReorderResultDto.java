package bg.svetozar.tastypizza.model.dto.order;

import java.util.List;

public record ReorderResultDto(
        CartDto cart,
        int added,
        int skipped,
        List<String> messages
) {}
