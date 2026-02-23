package com.example.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationRequest {
    String username;
    String password;   
}
