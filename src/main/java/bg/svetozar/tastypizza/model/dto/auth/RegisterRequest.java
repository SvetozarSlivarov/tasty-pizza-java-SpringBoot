package bg.svetozar.tastypizza.model.dto.auth;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullname;
    private String username;
    private String password;
}