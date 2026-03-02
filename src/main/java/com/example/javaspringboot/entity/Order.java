package com.example.javaspringboot.entity;

import com.example.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    
    BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    OrderStatus status;
    
    LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> items;
}
