package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pasta;
import bg.svetozar.tastypizza.model.entity.PastaSauce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PastaSauceRepository extends JpaRepository<PastaSauce, Long> {

    List<PastaSauce> findAllByPasta_Id(Long pastaId);

    @Query("""
        SELECT ps FROM PastaSauce ps
        JOIN FETCH ps.ingredient ing
        WHERE ps.pasta = :pasta
    """)
    List<PastaSauce> findAllByPastaWithIngredient(@Param("pasta") Pasta pasta);

    boolean existsByPasta_IdAndIngredient_Id(Long pastaId, Long ingredientId);

    Optional<PastaSauce> findByIdAndPasta_Id(Long id, Long pastaId);
}
