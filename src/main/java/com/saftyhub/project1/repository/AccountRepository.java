package com.saftyhub.project1.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.saftyhub.project1.model.Account_information;

public interface AccountRepository extends JpaRepository<Account_information, Integer> {
    
    Optional<Account_information> findByAccountEmail(String accountEmail);
    
    Optional<Account_information> findByAccountPassword(String accountPassword);
    
    // Native delete query to handle trigger conflicts - runs in separate transaction
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = "DELETE FROM account_info WHERE Account_id = :accountId", nativeQuery = true)
    void deleteByAccountIdNative(@Param("accountId") int accountId);
}