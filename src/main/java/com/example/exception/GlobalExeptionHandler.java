package com.example.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.example.dto.request.ApiResponse;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExeptionHandler {

    private static final String MIN_ATTRIBUTE = "min";
    private static final String MAX_ATTRIBUTE = "max";

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(Errorcode.UNCATEGORIZED_ERROR.getCode());
        apiResponse.setMessage(Errorcode.UNCATEGORIZED_ERROR.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        Errorcode errorcode = ex.getErrorcode();
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorcode.getCode());
        apiResponse.setMessage(errorcode.getMessage());
        return ResponseEntity.status(errorcode.getStatuscode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> HandlingAccessDeniedException(AccessDeniedException ex) {
        Errorcode errorcode = Errorcode.ACCESS_DENIED;
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorcode.getCode());
        apiResponse.setMessage(ex.getMessage());
        return ResponseEntity.status(errorcode.getStatuscode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        Errorcode errorcode = Errorcode.INVALID_KEY;

        Map<String, Object> attributes = null;

        try {
            errorcode = Errorcode.valueOf(errorMessage);
            var constraintViolation = ex.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> extractedAttributes = (Map<String, Object>) constraintViolation
                    .getConstraintDescriptor().getAttributes();
            attributes = extractedAttributes;
            log.info(attributes.toString());

        } catch (Exception e) {
            log.debug("Failed to extract constraint attributes", e);
        }

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorcode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ? mapAttributesToMessage(errorcode.getMessage(), attributes)
                : errorcode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttributesToMessage(String message, Map<String, Object> attributes) {
        String result = message;

        if (attributes.containsKey(MIN_ATTRIBUTE)) {
            String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
            result = result.replace("{" + MIN_ATTRIBUTE + "}", minValue);
        }
        if (attributes.containsKey(MAX_ATTRIBUTE)) {
            String maxValue = String.valueOf(attributes.get(MAX_ATTRIBUTE));
            result = result.replace("{" + MAX_ATTRIBUTE + "}", maxValue);
        }

        return result;
    }
}
