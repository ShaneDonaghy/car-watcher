package ie.shep.carwatchdog.repository;

import ie.shep.carwatchdog.domain.SeenAd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeenAdRepository extends JpaRepository<SeenAd, Long> {

    List<SeenAd> findBySearchId(Long searchId);

    void deleteBySearchIdAndSourceAndExternalIdIn(
            Long searchId,
            String source,
            List<String> externalIds
    );
}
