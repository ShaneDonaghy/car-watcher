package ie.shep.carwatchdog.repository;

import ie.shep.carwatchdog.domain.SearchDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchDefinitionRepository
        extends JpaRepository<SearchDefinition, Long> {

    Optional<SearchDefinition> findBySearchKey(String searchKey);
}
