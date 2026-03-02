package com.example.javaspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.javaspringboot.entity.Order;
import com.example.javaspringboot.entity.User;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
