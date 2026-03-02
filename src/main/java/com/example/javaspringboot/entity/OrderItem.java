package com.example.javaspringboot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id
    String id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;
    
    Integer quantity;
    BigDecimal priceAtPurchase;
}
