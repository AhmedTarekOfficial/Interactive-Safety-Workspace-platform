package com.saftyhub.project1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "account_info")
public class Account_information {

    @Id
    @Column(name = "Account_id")  // Match your DB exactly (capital A)
    private int accountId;

    @Column(name = "account_email", unique = true, nullable = false)
    private String accountEmail;

    @Column(name = "account_password", nullable = false)
    private String accountPassword;

    // Add relationship to Users if needed
    @OneToOne
    @JoinColumn(name = "Account_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private Users user;
    
    // Explicit getters (in case Lombok isn't processing)
    public int getAccountId() {
        return accountId;
    }
    
    public String getAccountPassword() {
        return accountPassword;
    }
    
    public String getAccountEmail() {
        return accountEmail;
    }
    
    public Users getUser() {
        return user;
    }
    
    // Explicit setters (in case Lombok isn't processing)
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    
    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }
    
    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }
    
    public void setUser(Users user) {
        this.user = user;
    }
}
