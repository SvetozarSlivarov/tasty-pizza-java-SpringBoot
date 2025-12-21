package bg.svetozar.tastypizza.model.dto.admin;

import bg.svetozar.tastypizza.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminUserDto(
        Long id,
        String username,
        String fullname,
        UserRole role,
        boolean deleted,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime deletedAt,

        int tokenVersion,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
