package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaAllowedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


import java.util.List;

public interface PizzaAllowedIngredientRepository extends JpaRepository<PizzaAllowedIngredient, Long> {

    List<PizzaAllowedIngredient> findAllByPizza_Id(Long pizzaId);

    @Query("""
        select pai from PizzaAllowedIngredient pai
        join fetch pai.ingredient ing
        where pai.pizza = :pizza
    """)
    List<PizzaAllowedIngredient> findAllByPizzaWithIngredient(@Param("pizza") Pizza pizza);

    boolean existsByPizza_IdAndIngredient_Id(Long pizzaId, Long ingredientId);

    Optional<PizzaAllowedIngredient> findByIdAndPizza_Id(Long id, Long pizzaId);
    ;
}
