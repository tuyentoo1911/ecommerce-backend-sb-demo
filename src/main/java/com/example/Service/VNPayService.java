package com.example.Service;

import com.example.config.VNPayConfig;
import com.example.exception.AppException;
import com.example.exception.Errorcode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayService {

    VNPayConfig vnPayConfig;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Build the VNPay payment redirect URL.
     * Amount is in VND; VNPay requires amount * 100 in the request.
     */
    public String createPaymentUrl(String orderId, long amountVnd, String orderInfo, String clientIp) {
        if (vnPayConfig.getTmnCode() == null || vnPayConfig.getTmnCode().isBlank()
                || vnPayConfig.getHashSecret() == null || vnPayConfig.getHashSecret().isBlank()) {
            log.error("VNPay credentials not configured (tmnCode/hashSecret is empty)");
            throw new AppException(Errorcode.PAYMENT_CREATE_FAILED);
        }

        // TreeMap auto-sorts by key alphabetically (required by VNPay spec)
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CreateDate", LocalDateTime.now().format(DATE_FMT));
        params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
        params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
        params.put("vnp_Locale", vnPayConfig.getLocale());
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", vnPayConfig.getOrderType());
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_TxnRef", orderId);

        // Hash data uses URL-encoded values (VNPay spec)
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
            if (!first) {
                hashData.append('&');
                query.append('&');
            }
            hashData.append(entry.getKey()).append('=').append(encodedValue);
            query.append(entry.getKey()).append('=').append(encodedValue);
            first = false;
        }

        String secureHash = hmacSha512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnPayConfig.getBaseUrl() + "?" + query;
    }

    /**
     * Verify the HMAC-SHA512 signature from VNPay IPN/return callback.
     * Returns true if the signature is valid.
     */
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // Remove hash fields before verification
        Map<String, String> signParams = new TreeMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
                if (!first) hashData.append('&');
                hashData.append(entry.getKey()).append('=').append(encodedValue);
                first = false;
            }
        }

        String expected = hmacSha512(vnPayConfig.getHashSecret(), hashData.toString());
        return expected.equalsIgnoreCase(receivedHash);
    }

    private static String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HmacSHA512 error", e);
        }
    }
}
