package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.PizzaAllowedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PizzaAllowedIngredientRepository extends JpaRepository<PizzaAllowedIngredient, Long> {

    List<PizzaAllowedIngredient> findAllByPizza_Id(Long pizzaId);
}
