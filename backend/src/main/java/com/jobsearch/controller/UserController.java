package com.jobsearch.controller;

import com.jobsearch.model.User;
import com.jobsearch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    // Vulnerable: No access control - any user can access any profile
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, HttpSession session) {
        // Vulnerable: No proper authorization check
        User currentUser = (User) session.getAttribute("user");
        
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Vulnerable: Can update any user's profile by changing the ID
        User user = userRepository.findById(updatedUser.getId()).orElse(null);
        
        if (user != null) {
            // Vulnerable: No input validation
            user.setFullName(updatedUser.getFullName());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setAddress(updatedUser.getAddress());
            user.setCompanyName(updatedUser.getCompanyName());
            user.setCompanyDescription(updatedUser.getCompanyDescription());
            user.setWebsite(updatedUser.getWebsite());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            session.setAttribute("user", savedUser);
            
            response.put("success", true);
            response.put("user", savedUser);
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file, 
                                     @RequestParam("userId") Long userId,
                                     HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Vulnerable: No file type validation, no size limit, no authentication check
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Please select a file");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Vulnerable: Directory traversal possible
            String uploadDir = "uploads/cv/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Vulnerable: No filename sanitization
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());
            
            // Vulnerable: Can upload CV for any user
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setCvFile(fileName);
                userRepository.save(user);
                
                response.put("success", true);
                response.put("fileName", fileName);
                response.put("message", "CV uploaded successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file,
                                               @RequestParam("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        // Vulnerable: No authentication, no file validation
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Please select a file");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Vulnerable: Directory traversal, no file type validation
            String uploadDir = "uploads/images/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());
            
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setProfileImage(fileName);
                userRepository.save(user);
                
                response.put("success", true);
                response.put("fileName", fileName);
                response.put("message", "Profile image uploaded successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
