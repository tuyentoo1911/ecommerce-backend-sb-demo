package com.example.javaspringboot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "cart_items")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {
    @Id
    String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;
    
    Integer quantity;
}
