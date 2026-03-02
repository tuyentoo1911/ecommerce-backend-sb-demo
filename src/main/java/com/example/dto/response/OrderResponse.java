package com.example.dto.response;

import com.example.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String id;
    String userId;
    String username;
    BigDecimal totalPrice;
    OrderStatus status;
    LocalDateTime createdAt;
    List<OrderItemResponse> items;
}
