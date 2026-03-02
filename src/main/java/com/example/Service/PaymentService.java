package com.example.Service;

import com.example.dto.response.OrderItemResponse;
import com.example.dto.response.OrderResponse;
import com.example.dto.response.PaymentUrlResponse;
import com.example.dto.vnpay.VNPayIpnResponse;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentMethod;
import com.example.enums.PaymentStatus;
import com.example.exception.AppException;
import com.example.exception.Errorcode;
import com.example.javaspringboot.entity.Order;
import com.example.javaspringboot.entity.Payment;
import com.example.javaspringboot.repository.OrderRepository;
import com.example.javaspringboot.repository.PaymentRepository;
import com.example.javaspringboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;
    UserRepository userRepository;
    VNPayService vnPayService;

    /**
     * Create VNPay payment URL for an order.
     * Returns the redirect URL that the frontend should navigate the user to.
     */
    @Transactional
    public PaymentUrlResponse createVNPayPayment(String orderId, String clientIp) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(Errorcode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AppException(Errorcode.ACCESS_DENIED);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(Errorcode.ORDER_CANNOT_CANCEL);
        }

        long amountVnd = order.getTotalPrice().setScale(0, RoundingMode.HALF_UP).longValue();
        if (amountVnd < 5000 || amountVnd > 1_000_000_000) {
            log.warn("VNPay amount out of range: {}", amountVnd);
            throw new AppException(Errorcode.PAYMENT_CREATE_FAILED);
        }

        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toan don hang " + orderId;
        String payUrl = vnPayService.createPaymentUrl(orderId, amountVnd, orderInfo, clientIp);

        Payment payment = Payment.builder()
                .id(requestId)
                .order(order)
                .amount(amountVnd)
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.VNPAY)
                .requestId(requestId)
                .payUrl(payUrl)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        return PaymentUrlResponse.builder()
                .payUrl(payUrl)
                .orderId(orderId)
                .amountVnd(amountVnd)
                .build();
    }

    /**
     * Handle IPN callback from VNPay (server-to-server GET request).
     * Verifies signature and updates payment + order status.
     */
    @Transactional
    public VNPayIpnResponse handleVNPayIpn(Map<String, String> params) {
        if (!vnPayService.verifySignature(params)) {
            log.warn("VNPay IPN invalid signature, params={}", params);
            return VNPayIpnResponse.invalidSignature();
        }

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String vnpAmount = params.get("vnp_Amount");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("VNPay IPN order not found: orderId={}", orderId);
            return VNPayIpnResponse.orderNotFound();
        }

        Payment payment = paymentRepository.findByOrder(order).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (payment == null) {
            log.warn("VNPay IPN no pending payment for orderId={}", orderId);
            return VNPayIpnResponse.alreadyConfirmed();
        }

        // Validate amount (VNPay sends amount * 100)
        if (vnpAmount != null) {
            long receivedAmount = Long.parseLong(vnpAmount) / 100;
            if (receivedAmount != payment.getAmount()) {
                log.warn("VNPay IPN amount mismatch: expected={}, received={}", payment.getAmount(), receivedAmount);
                return VNPayIpnResponse.invalidAmount();
            }
        }

        boolean success = "00".equals(responseCode);
        payment.setTransactionId(transactionNo);
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setPaidAt(success ? LocalDateTime.now() : null);
        paymentRepository.save(payment);

        if (success) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("VNPay payment success: orderId={}, transactionNo={}", orderId, transactionNo);
        } else {
            log.info("VNPay payment failed: orderId={}, responseCode={}", orderId, responseCode);
        }

        return VNPayIpnResponse.ok();
    }

    /**
     * Demo: mark order as paid immediately without calling VNPay. For testing only.
     */
    @Transactional
    public OrderResponse completeDemoPayment(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(Errorcode.ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AppException(Errorcode.ACCESS_DENIED);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(Errorcode.ORDER_CANNOT_CANCEL);
        }
        long amountVnd = order.getTotalPrice().setScale(0, RoundingMode.HALF_UP).longValue();
        String requestId = "demo-" + UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .amount(amountVnd)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(PaymentMethod.DEMO)
                .requestId(requestId)
                .transactionId(requestId)
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);
        log.info("Demo payment completed: orderId={}", orderId);
        return toOrderResponse(saved);
    }

    private OrderResponse toOrderResponse(Order order) {
        var items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private String getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Errorcode.USER_NOT_FOUND))
                .getId();
    }
}
