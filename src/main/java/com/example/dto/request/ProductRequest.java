package com.example.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "NAME_REQUIRED")
    String name;
    
    String description;
    
    @NotNull(message = "PRICE_REQUIRED")
    @DecimalMin(value = "0.0", message = "PRICE_INVALID")
    BigDecimal price;
    
    @NotNull(message = "STOCK_REQUIRED")
    @Min(value = 0, message = "STOCK_INVALID")
    Integer stock;
    
    String category;
}
