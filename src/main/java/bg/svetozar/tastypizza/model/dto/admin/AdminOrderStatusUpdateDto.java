package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.OrderStatus;

import java.time.LocalDateTime;

public record AdminOrderStatusUpdateDto(
        Long orderId,
        OrderStatus status,
        LocalDateTime updatedAt
) {}
