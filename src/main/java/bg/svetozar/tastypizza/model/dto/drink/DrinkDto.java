package bg.svetozar.tastypizza.model.dto.drink;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record DrinkDto(
        Long id,
        String name,
        String description,
        String basePrice,
        String type,
        boolean deleted,
        LocalDateTime deleteAt,
        String imageUrl
) {
}
