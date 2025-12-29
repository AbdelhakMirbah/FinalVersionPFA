package ma.emsi.fraud.service;

import ma.emsi.fraud.model.FraudCheck;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class FraudStreamService {

    // Sinks.many().multicast().onBackpressureBuffer() creates a hot hot publisher
    // that broadcasts messages to all subscribers.
    private final Sinks.Many<FraudCheck> sink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Push a new fraud check event to all connected clients
     */
    public void pushEvent(FraudCheck fraudCheck) {
        sink.tryEmitNext(fraudCheck);
    }

    /**
     * Get the stream of fraud check events
     */
    public Flux<FraudCheck> getFraudStream() {
        return sink.asFlux();
    }
}
