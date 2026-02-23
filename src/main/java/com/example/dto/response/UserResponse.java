package com.example.dto.response;

import java.time.LocalDate;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    String id;
    String username;
    String email;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<String> roles;
    
}
