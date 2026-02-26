package com.byteCore.demo.domain;

import com.byteCore.demo.enums.Role;
import com.byteCore.demo.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_tb")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,  unique = true)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    private BigDecimal averageRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SellerVerificationEntity> verifications = new ArrayList<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<ProductEntity> products = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(length = 100)
    private String pixKey; // chave pix cadastrada para saques

    @OneToMany(mappedBy = "seller")
    private List<WithdrawalRequestEntity> withdrawals = new ArrayList<>();

    @Version
    private Long balanceVersion;







    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public void addToAvailableBalance(BigDecimal amount) {
        validateAmount(amount);
        availableBalance = availableBalance.add(amount);
    }

    public void addToPendingBalance(BigDecimal amount) {
        validateAmount(amount);
        pendingBalance = pendingBalance.add(amount);
    }

    public void movePendingToAvailable(BigDecimal amount) {
        validateAmount(amount);

        // saldo pendente < valor solicitado
        if (pendingBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient pending balance");
        }

        pendingBalance = pendingBalance.subtract(amount);
        availableBalance = availableBalance.add(amount);
    }

    public synchronized void deductFromAvailable(BigDecimal amount) {
        validateAmount(amount);

        if (this.availableBalance == null) {
            this.availableBalance = BigDecimal.ZERO;
        }

        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }

        this.availableBalance = this.availableBalance.subtract(amount);
    }


    public boolean canWithdraw(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }

    public boolean  isVerifiedSeller() {
        return role == Role.VERIFIED_SELLER;
    }

    public boolean  hasPendingVerification() {

        for (SellerVerificationEntity verification : verifications) {

            if (verification.getStatus() == VerificationStatus.PENDING) {
                return true;
            }
        }
        return false;
    }

    public void addVerification(SellerVerificationEntity verification) {
        verifications.add(verification);
        verification.setUser(this);
    }

    public void addSellerProduct(ProductEntity product) {
        products.add(product);
        product.setSeller(this);
    }
}

