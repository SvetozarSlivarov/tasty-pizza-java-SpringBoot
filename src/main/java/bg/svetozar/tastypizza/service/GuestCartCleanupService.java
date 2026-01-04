package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestCartCleanupService {

    private final OrderRepository orderRepository;

    @Value("${tastypizza.cart.guest.cleanup.enabled}")
    private boolean enabled;

    @Value("${tastypizza.cart.guest.retention-days}")
    private int retentionDays;

    @Value("${tastypizza.cart.guest.cleanup.batch-size}")
    private int batchSize;

    @Scheduled(cron = "${tastypizza.cart.guest.cleanup.cron}")
    public void scheduledCleanup() {
        if (!enabled) {
            return;
        }
        cleanupOnce();
    }

    @Transactional
    public long cleanupOnce() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        long deleted = 0;
        while (true) {
            List<Long> ids = orderRepository
                    .findStaleGuestCartIds(OrderStatus.CART, cutoff, PageRequest.of(0, batchSize))
                    .getContent();

            if (ids.isEmpty()) {
                break;
            }

            for (Long id : ids) {
                orderRepository.deleteById(id);
                deleted++;
            }
        }

        return deleted;
    }
}
