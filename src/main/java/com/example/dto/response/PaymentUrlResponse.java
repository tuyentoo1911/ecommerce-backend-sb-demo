package com.example.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentUrlResponse {
    /** Redirect user to this URL to complete payment on VNPay */
    String payUrl;
    String orderId;
    Long amountVnd;
}
