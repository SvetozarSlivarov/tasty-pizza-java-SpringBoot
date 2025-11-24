package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.PizzaIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PizzaIngredientRepository extends JpaRepository<PizzaIngredient, Long> {

    List<PizzaIngredient> findAllByPizza_Id(Long pizzaId);
}
