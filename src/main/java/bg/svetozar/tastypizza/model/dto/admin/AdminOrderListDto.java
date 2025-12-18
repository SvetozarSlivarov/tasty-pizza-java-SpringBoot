package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderListDto(
        Long orderId,
        OrderStatus status,
        BigDecimal total,
        Long itemCount,
        LocalDateTime createdAt,
        String customerUsername,
        String deliveryPhone,
        String deliveryAddress
) {
    public AdminOrderListDto {
        if (total == null) total = BigDecimal.ZERO;
        if (itemCount == null) itemCount = 0L;
    }
}
