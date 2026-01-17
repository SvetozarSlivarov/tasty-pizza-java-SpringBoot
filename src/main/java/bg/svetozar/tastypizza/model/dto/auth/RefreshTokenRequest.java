package bg.svetozar.tastypizza.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_REFRESH_TOKEN;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = REQUIRED_REFRESH_TOKEN)
    private String refreshToken;
}
