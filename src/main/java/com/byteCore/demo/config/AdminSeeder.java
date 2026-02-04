package com.byteCore.demo.config;

import com.byteCore.demo.domain.User;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${admin_email}")
    private String adminEmail;

    @Value("${admin_password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        log.info("Checking if admin user exists...");

        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            log.info("Admin user not found. Creating admin...");

            User admin  = userMapper.toAdmin(
                    "Crono",
                    adminEmail,
                    passwordEncoder.encode(adminPassword)
            );

            userRepository.save(admin);

            log.info("Admin user created: {}", adminEmail);

        } else {
            log.info("Admin user already exists.");
        }

    }

    }
