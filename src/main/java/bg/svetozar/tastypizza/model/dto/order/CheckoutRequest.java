package bg.svetozar.tastypizza.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PHONE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PHONE_NUMBER_MAX_30_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_ADDRESS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ADDRESS_MAX_300_CHARS;

public record CheckoutRequest(
        @NotBlank(message = REQUIRED_PHONE)
        @Size(max = 30, message = INVALID_PHONE_NUMBER_MAX_30_CHARS)
        String phone,

        @NotBlank(message = REQUIRED_ADDRESS)
        @Size(max = 300, message = INVALID_ADDRESS_MAX_300_CHARS)
        String address
) {}
