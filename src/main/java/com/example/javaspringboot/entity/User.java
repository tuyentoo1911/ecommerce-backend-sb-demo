package com.example.javaspringboot.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    String id;
    String username;
    String email;
    String password;
    String firstName;
    String lastName;
    LocalDate dob;
    @ElementCollection(fetch = FetchType.EAGER)
    Set<String> roles;
}
