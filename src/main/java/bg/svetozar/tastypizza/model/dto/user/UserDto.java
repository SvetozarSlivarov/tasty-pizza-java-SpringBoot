package bg.svetozar.tastypizza.model.dto.user;

public record UserDto(
        Long id,
        String fullname,
        String username,
        String role
) {
}
