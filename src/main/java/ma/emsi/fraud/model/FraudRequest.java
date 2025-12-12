package ma.emsi.fraud.model;

public record FraudRequest(
        Double amount,
        Double oldBalance,
        Double newBalance,
        String ip,
        String email) {
}
