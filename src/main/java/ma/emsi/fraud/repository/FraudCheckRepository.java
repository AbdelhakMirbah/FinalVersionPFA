package ma.emsi.fraud.repository;

import ma.emsi.fraud.model.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudCheckRepository extends JpaRepository<FraudCheck, Long> {
}
