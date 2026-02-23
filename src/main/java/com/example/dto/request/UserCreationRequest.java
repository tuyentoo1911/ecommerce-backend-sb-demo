package com.example.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import com.example.Validator.DobConstraint;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreationRequest {
    @Size(min = 3, max = 20, message = "USERNAME_INVALID")
    String username;
    @Email(message = "EMAIL_INVALID")
    String email;
    @Size(min = 8, max = 20, message = "PASSWORD_INVALID")
    String password;
    String firstName;
    String lastName;

    @DobConstraint(min = 18, max = 100, message = "DOB_INVALID")
    LocalDate dob;
}
