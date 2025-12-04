package bg.svetozar.tastypizza.model.mapper;

import bg.svetozar.tastypizza.model.dto.user.UserDto;
import bg.svetozar.tastypizza.model.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFullname(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}
