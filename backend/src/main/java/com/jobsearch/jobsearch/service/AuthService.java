package com.jobsearch.service;

import com.jobsearch.model.User;
import com.jobsearch.repository.UserRepository;
import com.jobsearch.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public boolean isAdmin(String token) throws Exception {
        Map<String, Object> claims = validateToken(token);
        return "admin".equals(claims.get("role"));
    }

    public boolean isEmployer(String token) throws Exception {
        Map<String, Object> claims = validateToken(token);
        return "employer".equals(claims.get("role"));
    }

    public User register(User user) throws Exception {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null ||
            user.getUsername().isEmpty() || user.getPassword().isEmpty() || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Username, email, dan password tidak boleh kosong");
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email sudah digunakan");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        if (user.getRole() == null) {
            user.setRole(User.Role.member);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User login(String username, String rawPassword, HttpServletResponse response) throws Exception {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            String token = UUID.randomUUID().toString(); // Bisa diganti JWT
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(30 * 60);
            response.addCookie(cookie);
            return user;
        } else {
            throw new IllegalArgumentException("Username atau password salah");
        }
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("SESSION_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public Map<String, Object> validateToken(String token) throws Exception {
        return jwtUtil.validateToken(token).getBody();
    }
}
