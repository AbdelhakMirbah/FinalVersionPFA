package ma.emsi.fraud.model;

public record FraudResponse(
        Float score,
        String risk) {
}
