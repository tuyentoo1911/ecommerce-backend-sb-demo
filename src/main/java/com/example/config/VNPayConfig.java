package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    /** Terminal code provided by VNPay (vnp_TmnCode) */
    private String tmnCode = "";
    /** Hash secret for HMAC-SHA512 signature */
    private String hashSecret = "";
    /** VNPay payment gateway base URL */
    private String baseUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    /** URL VNPay redirects user back to after payment */
    private String returnUrl = "http://localhost:3000/payment/return";
    /** URL VNPay calls server-to-server for payment result (IPN) */
    private String ipnUrl = "http://localhost:8080/payments/vnpay/ipn";
    /** API version */
    private String version = "2.1.0";
    /** Command */
    private String command = "pay";
    /** Currency code */
    private String currCode = "VND";
    /** Locale: vn | en */
    private String locale = "vn";
    /** Order type */
    private String orderType = "other";

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        return new RestTemplate(factory);
    }
}
