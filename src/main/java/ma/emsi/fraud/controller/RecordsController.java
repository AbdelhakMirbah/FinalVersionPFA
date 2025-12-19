package ma.emsi.fraud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.emsi.fraud.model.FraudCheck;
import ma.emsi.fraud.repository.FraudCheckRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Slf4j
public class RecordsController {

    private final FraudCheckRepository repository;

    /**
     * Get all fraud check records (last 50)
     */
    @GetMapping
    public Flux<FraudCheck> getAllRecords() {
        log.info("Fetching all fraud check records");
        return Flux.fromIterable(repository.findTop50ByOrderByCreatedAtDesc());
    }

    /**
     * Get statistics about fraud checks
     */
    @GetMapping("/stats")
    public Mono<Map<String, Object>> getStatistics() {
        log.info("Fetching fraud check statistics");

        return Mono.fromCallable(() -> {
            var allRecords = repository.findAll();

            long total = allRecords.size();
            long highRisk = allRecords.stream().filter(r -> "HIGH".equals(r.getRisk())).count();
            long lowRisk = allRecords.stream().filter(r -> "LOW".equals(r.getRisk())).count();
            double avgScore = allRecords.stream()
                    .mapToDouble(FraudCheck::getScore)
                    .average()
                    .orElse(0.0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("highRisk", highRisk);
            stats.put("lowRisk", lowRisk);
            stats.put("avgScore", avgScore);

            return stats;
        });
    }
}
