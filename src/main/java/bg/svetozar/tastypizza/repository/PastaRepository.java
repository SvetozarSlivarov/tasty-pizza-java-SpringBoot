package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pasta;
import bg.svetozar.tastypizza.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PastaRepository extends JpaRepository<Pasta, Long> {

    @Query("""
        SELECT p FROM Pasta p
        JOIN FETCH p.product prod
        WHERE prod.deleted = false AND p.id = :id
    """)
    Optional<Pasta> findByIdLight(long id);

    @Query("""
        SELECT p FROM Pasta p
        JOIN FETCH p.product prod
        WHERE prod.deleted = false
    """)
    List<Pasta> findAllLight();

    @Query("""
        SELECT p FROM Pasta p
        JOIN FETCH p.product prod
        WHERE prod.deleted = true
    """)
    List<Pasta> findDeletedLight();

    @Query("""
        SELECT p FROM Pasta p
        JOIN FETCH p.product prod
        WHERE p.id = :id
    """)
    Optional<Pasta> findByIdFull(Long id);

    Optional<Pasta> findByProduct(Product product);
}
