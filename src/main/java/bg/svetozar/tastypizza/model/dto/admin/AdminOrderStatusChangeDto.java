package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.OrderStatus;

import java.time.LocalDateTime;

public record AdminOrderStatusChangeDto(
        OrderStatus status,
        LocalDateTime changedAt
) {}
