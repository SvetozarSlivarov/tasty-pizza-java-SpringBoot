package bg.svetozar.tastypizza.model.dto.admin;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserPageDto(
        List<AdminUserDto> content,
        List<AdminUserDto> items,
        long totalElements,
        long total,
        int page,
        int number,
        int size,
        int totalPages
) {
    public static AdminUserPageDto from(Page<AdminUserDto> page) {
        return new AdminUserPageDto(
                page.getContent(),
                page.getContent(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getNumber(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }
}