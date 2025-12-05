package bg.svetozar.tastypizza.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthDto {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;
}