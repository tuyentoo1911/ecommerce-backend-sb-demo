package com.example.config;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.MediaType;
import com.example.exception.Errorcode;
import com.example.dto.request.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthencationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
       Errorcode errorcode = Errorcode.UNAUTHENTICATED;
       response.setStatus(errorcode.getStatuscode().value());
       response.setContentType(MediaType.APPLICATION_JSON_VALUE);
       ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
           .code(errorcode.getCode())
           .message(errorcode.getMessage())
           .build();

       ObjectMapper objectMapper = new ObjectMapper();
       response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
       response.flushBuffer();
    }
}
