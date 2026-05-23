package Online_Meansreang.Meangsreang.repository;

import Online_Meansreang.Meangsreang.entity.Payment;
import Online_Meansreang.Meangsreang.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByOrderId(String orderId);

    boolean existsByTransactionId(String transactionId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByUserUsernameOrderByCreatedAtDesc(String username);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    /** Payments pending more than X minutes — for expiry job */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.createdAt < :expiryTime")
    List<Payment> findExpiredPendingPayments(@Param("expiryTime") LocalDateTime expiryTime);

    /** Prevent duplicate webhook: find already-processed payment by orderId */
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.webhookProcessed = true")
    Optional<Payment> findAlreadyProcessedByOrderId(@Param("orderId") String orderId);
}
