package com.example.config;

import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import com.example.javaspringboot.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.UUID;
import com.example.enums.Role;
import com.example.javaspringboot.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(prefix = "spring", name = "datasource.driver-class-name", havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository){
    log.info("Init Application..........");
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());
                User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles(roles)
                .build();
                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin, please change it");
            } else {
                log.info("Admin user already exists in database");
            }
        };
    }
}
