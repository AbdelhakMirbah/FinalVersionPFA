package ma.emsi.fraud.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_checks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Float score;

    @Column(nullable = false, length = 10)
    private String risk;

    @Column(name = "transaction_type")
    private Integer transactionType;

    @Column(name = "old_balance")
    private Double oldBalance;

    @Column(name = "new_balance")
    private Double newBalance;

    @Column(name = "old_balance_dest")
    private Double oldBalanceDest;

    @Column(name = "new_balance_dest")
    private Double newBalanceDest;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "email")
    private String email;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
