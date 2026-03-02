package com.example.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String productId;
    String productName;
    Integer quantity;
    BigDecimal priceAtPurchase;
    BigDecimal subtotal;
}
