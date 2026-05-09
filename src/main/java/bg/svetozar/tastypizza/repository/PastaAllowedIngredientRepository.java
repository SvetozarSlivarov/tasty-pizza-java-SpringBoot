package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pasta;
import bg.svetozar.tastypizza.model.entity.PastaAllowedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PastaAllowedIngredientRepository extends JpaRepository<PastaAllowedIngredient, Long> {

    List<PastaAllowedIngredient> findAllByPasta_Id(Long pastaId);

    @Query("""
        SELECT pai FROM PastaAllowedIngredient pai
        JOIN FETCH pai.ingredient ing
        WHERE pai.pasta = :pasta
    """)
    List<PastaAllowedIngredient> findAllByPastaWithIngredient(@Param("pasta") Pasta pasta);

    boolean existsByPasta_IdAndIngredient_Id(Long pastaId, Long ingredientId);

    Optional<PastaAllowedIngredient> findByIdAndPasta_Id(Long id, Long pastaId);
}
