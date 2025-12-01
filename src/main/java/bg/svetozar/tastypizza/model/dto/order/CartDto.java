package bg.svetozar.tastypizza.model.dto.order;

import java.util.List;

public record CartDto(
        Long orderId,
        String status,
        String total,
        List<CartItemDto> items
) {
}
