package ie.shep.carwatchdog.web;

import ie.shep.carwatchdog.domain.SearchDefinition;
import ie.shep.carwatchdog.service.CarWatchService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

record CreateSearchRequest(
        @NotBlank String name,
        String make,
        String model,
        String fuel,
        String engineSize,
        @Min(0) Integer minAgeYears,
        @Min(0) Integer maxAgeYears,
        @Email @NotBlank String notifyEmail
) {}

@RestController
@RequestMapping("/api/searches")
public class SearchController {

    private final CarWatchService service;

    public SearchController(CarWatchService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SearchDefinition> create(@RequestBody CreateSearchRequest request) {
        SearchDefinition definition = new SearchDefinition();

        definition.setName(request.name());
        definition.setMake(request.make());
        definition.setModel(request.model());
        definition.setFuel(request.fuel());
        definition.setEngineSize(request.engineSize());
        definition.setMinAgeYears(request.minAgeYears());
        definition.setMaxAgeYears(request.maxAgeYears());
        definition.setNotifyEmail(request.notifyEmail());

        SearchDefinition saved = service.createSearch(definition);

        return ResponseEntity
                .created(URI.create("/api/searches/" + saved.getId()))
                .body(saved);
    }

    @PostMapping("/{id}/poll")
    public ResponseEntity<Void> poll(@PathVariable Long id) {
        service.pollById(id);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.deleteSearch(id);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
