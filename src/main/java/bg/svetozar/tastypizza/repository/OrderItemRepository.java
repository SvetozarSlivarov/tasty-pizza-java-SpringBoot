package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    @Query("""
        select distinct oi
        from OrderItem oi
        left join fetch oi.customizations c
        left join fetch c.ingredient
        where oi.order.id = :orderId
    """)
    List<OrderItem> fetchCustomizationsForOrder(@Param("orderId") Long orderId);
}
