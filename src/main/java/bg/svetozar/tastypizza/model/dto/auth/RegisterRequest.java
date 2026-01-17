package bg.svetozar.tastypizza.model.dto.auth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_FULL_NAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_FULL_NAME_BETWEEN_2_100_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PASSWORD;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_USERNAME;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_USERNAME_BETWEEN_3_50_CHARS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PASSWORD_BETWEEN_6_255_CHARS;

@Data
public class RegisterRequest {
    @NotBlank(message = REQUIRED_FULL_NAME)
    @Size(min = 2, max = 100, message = INVALID_FULL_NAME_BETWEEN_2_100_CHARS)
    private String fullname;

    @NotBlank(message = REQUIRED_USERNAME)
    @Size(min = 3, max = 50, message = INVALID_USERNAME_BETWEEN_3_50_CHARS)
    private String username;

    @NotBlank(message = REQUIRED_PASSWORD)
    @Size(min = 6, max = 255, message =INVALID_PASSWORD_BETWEEN_6_255_CHARS)
    private String password;
}