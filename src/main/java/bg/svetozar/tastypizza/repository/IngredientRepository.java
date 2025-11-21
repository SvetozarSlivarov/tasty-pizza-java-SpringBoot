package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByDeletedFalse();

    List<Ingredient> findAllByDeletedTrue();

    Optional<Ingredient> findByIdAndDeletedFalse(Long id);

}
