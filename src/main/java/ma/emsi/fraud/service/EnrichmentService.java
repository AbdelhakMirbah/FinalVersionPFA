package ma.emsi.fraud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;

@Service
@Slf4j
public class EnrichmentService {

    private final Random random = new Random();

    /**
     * Simule l'appel parallèle à 2 services externes (IP & Email)
     * Utilise Mono.zip pour exécuter en parallèle
     */
    public Mono<EnrichmentResult> enrich(String ip, String email) {
        log.debug("Starting enrichment for IP: {} and Email: {}", ip, email);

        // Simulation appel API IP (200ms)
        Mono<Boolean> ipCheck = Mono.fromCallable(() -> {
            Thread.sleep(200);
            boolean isProxy = random.nextBoolean();
            log.debug("IP Check completed: isProxy={}", isProxy);
            return isProxy;
        }).subscribeOn(Schedulers.boundedElastic());

        // Simulation appel API Email (200ms)
        Mono<Boolean> emailCheck = Mono.fromCallable(() -> {
            Thread.sleep(200);
            boolean isLeaked = random.nextBoolean();
            log.debug("Email Check completed: isLeaked={}", isLeaked);
            return isLeaked;
        }).subscribeOn(Schedulers.boundedElastic());

        // Exécution en PARALLÈLE avec Mono.zip
        return Mono.zip(ipCheck, emailCheck)
                .map(tuple -> new EnrichmentResult(tuple.getT1(), tuple.getT2()))
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(result -> log.debug("Enrichment completed: {}", result))
                .doOnError(error -> log.error("Enrichment failed", error));
    }

    public record EnrichmentResult(boolean isProxy, boolean isEmailLeaked) {
    }
}
