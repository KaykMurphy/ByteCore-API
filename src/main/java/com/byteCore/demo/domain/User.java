package com.byteCore.demo.domain;

import com.byteCore.demo.enums.Role;
import com.byteCore.demo.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SellerVerification> verifications = new ArrayList<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public boolean  isVerifiedSeller() {
        return role == Role.VERIFIED_SELLER;
    }

    public boolean  hasPendingVerification() {

        for (SellerVerification verification : verifications) {

            if (verification.getStatus() == VerificationStatus.PENDING) {
                return true;
            }
        }
        return false;
    }

    public void addVerification(SellerVerification verification) {
        verifications.add(verification);
        verification.setUser(this);
    }

    public void addSellerProduct(Product  product) {
        products.add(product);
        product.setSeller(this);
    }
}

