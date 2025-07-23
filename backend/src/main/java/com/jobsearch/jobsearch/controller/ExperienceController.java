package com.jobsearch.controller;

import com.jobsearch.model.Education;
import com.jobsearch.model.Experience;
import com.jobsearch.repository.ExperienceRepository;
import com.jobsearch.service.AuthService;
import com.jobsearch.util.SanitizerUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/experience")
public class ExperienceController {

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private AuthService authService;

    // Helper method to extract userId from JWT token
    private Long getUserIdFromToken(String token) {
        try {
            Map<String, Object> claims = authService.validateToken(token);
            return ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyExperience(@CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        List<Experience> experienceList = experienceRepository.findByUserId(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "experience", experienceList
        ));
    }

    @PostMapping
    public ResponseEntity<?> addExperience(
            @RequestBody Experience experience,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {

        Experience sanitized = SanitizerUtil.sanitizeInputRecursive(experience);
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        sanitized.setUserId(userId); // Securely associate with current user
        sanitized.setCreatedAt(LocalDateTime.now());

        Experience savedExperience = experienceRepository.save(sanitized);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "experience", savedExperience,
                "message", "Experience added successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExperience(
            @PathVariable Long id,
            @RequestBody Experience updatedExperience,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
         Experience sanitized = SanitizerUtil.sanitizeInputRecursive(updatedExperience);
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Experience> optExperience = experienceRepository.findById(id);
        if (optExperience.isEmpty() || !Objects.equals(optExperience.get().getUserId(), userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden or not found"));
        }

        Experience experience = optExperience.get();
        experience.setCompany(sanitized.getCompany());
        experience.setPosition(sanitized.getPosition());
        experience.setStartDate(sanitized.getStartDate());
        experience.setEndDate(sanitized.getEndDate());
        experience.setDescription(sanitized.getDescription());

        Experience savedExperience = experienceRepository.save(experience);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "experience", savedExperience,
                "message", "Experience updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExperience(
            @PathVariable Long id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Experience> optExperience = experienceRepository.findById(id);
        if (optExperience.isEmpty() || !Objects.equals(optExperience.get().getUserId(), userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden or not found"));
        }

        experienceRepository.deleteById(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Experience deleted successfully"
        ));
    }
}
