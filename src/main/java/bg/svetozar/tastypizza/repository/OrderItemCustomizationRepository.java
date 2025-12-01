package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemCustomizationRepository extends JpaRepository<OrderItemCustomization, Long> {

    List<OrderItemCustomization> findByOrderItem(OrderItem orderItem);
}
