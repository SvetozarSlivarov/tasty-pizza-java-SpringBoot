package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.PizzaVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PizzaVariantRepository extends JpaRepository<PizzaVariant, Long> {
}
