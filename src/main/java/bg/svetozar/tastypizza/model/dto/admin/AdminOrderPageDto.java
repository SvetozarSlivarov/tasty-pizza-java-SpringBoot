package bg.svetozar.tastypizza.model.dto.admin;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminOrderPageDto(
        List<AdminOrderListDto> items,
        long total,
        int page,
        int size
) {
    public static AdminOrderPageDto from(Page<AdminOrderListDto> p) {
        return new AdminOrderPageDto(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
    }
}
