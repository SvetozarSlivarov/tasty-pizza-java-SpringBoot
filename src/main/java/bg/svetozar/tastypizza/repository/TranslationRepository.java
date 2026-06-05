package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    Optional<Translation> findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType,
            Long entityId,
            String fieldName,
            String language
    );

    List<Translation> findAllByEntityTypeAndEntityId(String entityType, Long entityId);
}
