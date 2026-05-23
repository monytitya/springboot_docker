package Online_Meansreang.Meangsreang.entity;

import Online_Meansreang.Meangsreang.enums.PaymentMethod;
import Online_Meansreang.Meangsreang.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
    indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_payments_user_id", columnList = "user_id"),
        @Index(name = "idx_payments_status", columnList = "payment_status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Merchant-generated unique order reference */
    @Column(name = "order_id", unique = true, nullable = false, length = 100)
    private String orderId;

    /** Bakong transaction reference returned after payment */
    @Column(name = "transaction_id", unique = true, length = 150)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.KHQR;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "phone", length = 25)
    private String phone;

    /** Raw KHQR string for QR rendering */
    @Column(name = "qr_string", columnDefinition = "TEXT")
    private String qrString;

    /** Webhook idempotency — prevent duplicate processing */
    @Column(name = "webhook_processed", nullable = false)
    @Builder.Default
    private boolean webhookProcessed = false;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
