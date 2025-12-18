package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByDeletedFalse();

    List<Ingredient> findAllByDeletedTrue();

    Optional<Ingredient> findByIdAndDeletedFalse(Long id);

    @Query("select i from Ingredient i join fetch i.type")
    List<Ingredient> findAllWithType();

    @Query("select i from Ingredient i join fetch i.type where i.deleted = false")
    List<Ingredient> findAllActiveWithType();

    @Query("select i from Ingredient i join fetch i.type where i.deleted = true")
    List<Ingredient> findAllDeletedWithType();

    @Query("select i from Ingredient i join fetch i.type where i.id = :id")
    Optional<Ingredient> findByIdWithType(@Param("id") Long id);

    @Query("select i from Ingredient i join fetch i.type where i.id = :id and i.deleted = false")
    Optional<Ingredient> findByIdAndDeletedFalseWithType(@Param("id") Long id);

    long countByType_Id(Long typeId);

}
