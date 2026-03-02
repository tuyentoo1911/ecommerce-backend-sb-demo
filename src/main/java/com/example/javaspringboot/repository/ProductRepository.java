package com.example.javaspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.javaspringboot.entity.Product;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategory(String category);
}
