package com.byteCore.demo.domain;

import com.byteCore.demo.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_tb")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,  unique = true)
    @Email
    private String email;

    @Column(nullable = false,  unique = true)
    private String password;


    private String repeatPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)

    private Role role;


}

