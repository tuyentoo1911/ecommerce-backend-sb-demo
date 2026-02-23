package com.example.javaspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.javaspringboot.entity.InvalidatedToken;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedRepository extends JpaRepository<InvalidatedToken, String> {

}
