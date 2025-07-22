package com.jobsearch.controller;

import com.jobsearch.model.User;
import com.jobsearch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Add MD5 hashing method
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, HttpSession session) {
        try {
            // Vulnerable: No input validation
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            
            // Vulnerable: Store user in session without proper security
            session.setAttribute("user", savedUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", savedUser);
            response.put("message", "Registration successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, 
                                  HttpSession session, 
                                  HttpServletRequest request) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        User user = userRepository.findByUsernameAndPassword(username, password);
        
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            // Clear any existing session data
            session.invalidate();
            session = request.getSession(true); // Create new session
            
            // Store user in session
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole().toString());
            
            // Set session timeout
            session.setMaxInactiveInterval(30 * 60);
            
            System.out.println("Session ID: " + session.getId());
            System.out.println("User stored in session: " + user.getUsername());
            
            response.put("success", true);
            response.put("user", user);
            response.put("sessionId", session.getId());
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        System.out.println("Profile check - Session ID: " + session.getId());
        User user = (User) session.getAttribute("user");
        System.out.println("User from session: " + (user != null ? user.getUsername() : "null"));
        
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
