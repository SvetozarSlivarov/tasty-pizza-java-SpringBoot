package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDetailDto(
        Long orderId,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        String customerUsername,
        String guestToken,
        String deliveryPhone,
        String deliveryAddress,

        BigDecimal total,
        long itemCount,

        List<AdminOrderItemDto> items,
        List<AdminOrderStatusChangeDto> statusHistory
) {}
