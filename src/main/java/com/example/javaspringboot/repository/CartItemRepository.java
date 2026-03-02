package com.example.javaspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.javaspringboot.entity.CartItem;
import com.example.javaspringboot.entity.User;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProductId(User user, String productId);
    void deleteByUser(User user);
}
