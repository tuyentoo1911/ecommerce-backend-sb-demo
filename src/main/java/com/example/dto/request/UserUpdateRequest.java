package com.example.dto.request;

import java.time.LocalDate;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.Builder;
import com.example.Validator.DobConstraint;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    @Email(message = "EMAIL_INVALID")
    String email;
    @Size(min = 8, max = 20, message = "PASSWORD_INVALID")
    String password;
    @Size(min = 3, max = 20, message = "FIRSTNAME_INVALID")
    String firstName;
    @Size(min = 3, max = 20, message = "LASTNAME_INVALID")
    String lastName;
    @DobConstraint(min = 18, max = 100, message = "DOB_INVALID")
    LocalDate dob;

}
