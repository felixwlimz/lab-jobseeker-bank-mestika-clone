package com.jobsearch.controller;

import com.jobsearch.model.User;
import com.jobsearch.model.UserDTO;
import com.jobsearch.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            User savedUser = authService.register(user);
            response.put("success", true);
            response.put("message", "Registrasi berhasil");
            response.put("user", new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getRole().toString()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registrasi gagal: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
    String username = credentials.get("username");
    String password = credentials.get("password");
    if (username == null || password == null) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username atau password kosong"));
    }
    try {
        User user = authService.login(username, password, response);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Login berhasil",
            "user", Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole())
        ));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("success", false, "message", "Login gagal: " + e.getMessage()));
    }
}


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(Map.of("success", true, "message", "Logout berhasil"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        if (token == null) {
            response.put("success", false);
            response.put("message", "Belum login");
            return ResponseEntity.status(401).body(response);
        }
        try {
            var claims = authService.validateToken(token);
            Long userId = (Long) claims.get("userId");
            String username = (String) claims.get("username");
            String role = (String) claims.get("role");

            response.put("success", true);
            response.put("user", new UserDTO(userId, username, role));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token tidak valid atau kadaluwarsa");
            return ResponseEntity.status(401).body(response);
        }
    }
}
