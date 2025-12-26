package ma.emsi.fraud.model;

public record FraudRequest(
                Double amount,
                Double oldBalance,
                Double newBalance,
                Integer type,
                Double oldBalanceDest,
                Double newBalanceDest,
                String ip,
                String email) {
}
