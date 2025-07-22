package com.jobsearch.repository;

import com.jobsearch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Vulnerable: Direct SQL injection possible
    @Query(value = "SELECT * FROM users WHERE username = :username AND password = :password", nativeQuery = true)
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    User findByUsername(String username);
    User findByEmail(String email);
}