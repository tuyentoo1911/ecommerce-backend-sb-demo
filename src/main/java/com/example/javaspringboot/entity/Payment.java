package com.example.javaspringboot.entity;

import com.example.enums.PaymentMethod;
import com.example.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    /** Amount in VND */
    Long amount;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50) check (payment_method in ('VNPAY','DEMO'))")
    PaymentMethod paymentMethod;

    /** Request id for idempotency */
    String requestId;

    /** Transaction ID from VNPay (vnp_TransactionNo) */
    String transactionId;

    /** VNPay redirect URL — stored as TEXT because URL with params exceeds 255 chars */
    @Column(columnDefinition = "TEXT")
    String payUrl;
    String qrCodeUrl;
    String deeplink;

    LocalDateTime createdAt;
    LocalDateTime paidAt;
}
