package bg.svetozar.tastypizza.model.dto.order;

import bg.svetozar.tastypizza.model.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusChangeDTO(
        OrderStatus status,
        LocalDateTime changedAt
) {}
