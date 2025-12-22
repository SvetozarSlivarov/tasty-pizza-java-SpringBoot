package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pizza;
import bg.svetozar.tastypizza.model.entity.PizzaIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PizzaIngredientRepository extends JpaRepository<PizzaIngredient, Long> {

    List<PizzaIngredient> findAllByPizza_Id(Long pizzaId);

    @Query("""
        select pi from PizzaIngredient pi
        join fetch pi.ingredient ing
        where pi.pizza = :pizza
    """)
    List<PizzaIngredient> findAllByPizzaWithIngredient(@Param("pizza") Pizza pizza);

    boolean existsByPizza_IdAndIngredient_Id(Long pizzaId, Long ingredientId);

    Optional<PizzaIngredient> findByIdAndPizza_Id(Long id, Long pizzaId);
}
