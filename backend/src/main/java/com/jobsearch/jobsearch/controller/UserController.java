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
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        Map<String, Object> response = new HashMap<>();
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        response.put("success", true);
        response.put("user", currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (currentUser == null || !Objects.equals(currentUser.getId(), updatedUser.getId())) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(403).body(response);
        }

        currentUser.setFullName(updatedUser.getFullName());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setAddress(updatedUser.getAddress());
        currentUser.setCompanyName(updatedUser.getCompanyName());
        currentUser.setCompanyDescription(updatedUser.getCompanyDescription());
        currentUser.setWebsite(updatedUser.getWebsite());
        currentUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(currentUser);
        session.setAttribute("user", savedUser);

        response.put("success", true);
        response.put("user", savedUser);
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            response.put("success", false);
            response.put("message", "Only PDF files are allowed");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String safeFileName = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.]", "_");
            String uploadDir = "uploads/cv/";
            Files.createDirectories(Paths.get(uploadDir));
            Path filePath = Paths.get(uploadDir + safeFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            currentUser.setCvFile(safeFileName);
            userRepository.save(currentUser);

            response.put("success", true);
            response.put("fileName", safeFileName);
            response.put("message", "CV uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        String contentType = file.getContentType();
        if (file.isEmpty() || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            response.put("success", false);
            response.put("message", "Only JPG and PNG images are allowed");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String safeFileName = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.]", "_");
            String uploadDir = "uploads/images/";
            Files.createDirectories(Paths.get(uploadDir));
            Path filePath = Paths.get(uploadDir + safeFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            currentUser.setProfileImage(safeFileName);
            userRepository.save(currentUser);

            response.put("success", true);
            response.put("fileName", safeFileName);
            response.put("message", "Profile image uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
