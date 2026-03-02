package com.example.controller;

import com.example.Service.PaymentService;
import com.example.dto.request.ApiResponse;
import com.example.dto.response.OrderResponse;
import com.example.dto.response.PaymentUrlResponse;
import com.example.dto.vnpay.VNPayIpnResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    /**
     * Create VNPay payment URL. Frontend redirects user to the returned payUrl.
     */
    @PostMapping("/orders/{orderId}/vnpay")
    ApiResponse<PaymentUrlResponse> createVNPayPayment(
            @PathVariable String orderId,
            HttpServletRequest request) {
        String clientIp = getClientIp(request);
        return ApiResponse.<PaymentUrlResponse>builder()
                .result(paymentService.createVNPayPayment(orderId, clientIp))
                .build();
    }

    /**
     * Demo: mark order as paid immediately (no VNPay). For testing only.
     */
    @PostMapping("/orders/{orderId}/demo")
    ApiResponse<OrderResponse> completeDemoPayment(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(paymentService.completeDemoPayment(orderId))
                .build();
    }

    /**
     * IPN callback - VNPay server calls this (GET) with payment result.
     * Must be public (no auth). Returns JSON to VNPay.
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<VNPayIpnResponse> vnpayIpn(@RequestParam Map<String, String> params) {
        VNPayIpnResponse response = paymentService.handleVNPayIpn(params);
        return ResponseEntity.ok(response);
    }

    /**
     * Return URL - VNPay redirects user here after payment.
     * Frontend-facing; just verify signature and return result to frontend.
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<Map<String, Object>> vnpayReturn(@RequestParam Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String responseCode = params.get("vnp_ResponseCode");
        boolean success = "00".equals(responseCode);
        result.put("success", success);
        result.put("orderId", params.get("vnp_TxnRef"));
        result.put("amount", params.get("vnp_Amount") != null
                ? Long.parseLong(params.get("vnp_Amount")) / 100 : null);
        result.put("transactionNo", params.get("vnp_TransactionNo"));
        result.put("responseCode", responseCode);
        result.put("message", success ? "Payment successful" : "Payment failed (code: " + responseCode + ")");
        return ResponseEntity.ok(result);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For can be a comma-separated list; take the first
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }
}
