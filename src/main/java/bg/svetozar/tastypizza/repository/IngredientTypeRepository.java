package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.IngredientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IngredientTypeRepository extends JpaRepository<IngredientType, Long> {

    boolean existsByNameIgnoreCase(String name);

    int deleteAllByNameIgnoreCase(String name);
}
