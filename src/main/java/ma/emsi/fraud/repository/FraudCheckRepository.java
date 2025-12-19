package ma.emsi.fraud.repository;

import ma.emsi.fraud.model.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudCheckRepository extends JpaRepository<FraudCheck, Long> {

    /**
     * Find last 50 fraud checks ordered by creation date (most recent first)
     */
    List<FraudCheck> findTop50ByOrderByCreatedAtDesc();
}
