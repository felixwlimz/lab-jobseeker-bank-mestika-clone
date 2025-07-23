package com.jobsearch.controller;

import com.jobsearch.model.Education;
import com.jobsearch.model.User;
import com.jobsearch.repository.EducationRepository;
import com.jobsearch.service.AuthService;
import com.jobsearch.util.SanitizerUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/education")
@Tag(name = "Education", description = "Education management API (Secure version)")
public class EducationController {

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private AuthService authService;

    private Long getUserIdFromToken(String token) {
        try {
            Map<String, Object> claims = authService.validateToken(token);
            return ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyEducation(@CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        List<Education> educationList = educationRepository.findByUserId(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "education", educationList
        ));
    }

    @PostMapping
    public ResponseEntity<?> addEducation(
            @RequestBody Education education,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
        Education sanitized = SanitizerUtil.sanitizeInputRecursive(education);
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        sanitized.setUserId(userId); // Enforce ownership
        sanitized.setCreatedAt(LocalDateTime.now());

        Education savedEducation = educationRepository.save(sanitized);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "education", savedEducation,
                "message", "Education added successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEducation(
            @PathVariable Long id,
            @RequestBody Education updatedEducation,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
        Education sanitized = SanitizerUtil.sanitizeInputRecursive(updatedEducation);
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Education> optEducation = educationRepository.findById(id);
        if (optEducation.isEmpty() || !Objects.equals(optEducation.get().getUserId(), userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden or not found"));
        }

        Education education = optEducation.get();
        education.setInstitution(sanitized.getInstitution());
        education.setDegree(sanitized.getDegree());
        education.setFieldOfStudy(sanitized.getFieldOfStudy());
        education.setStartDate(sanitized.getStartDate());
        education.setEndDate(sanitized.getEndDate());
        education.setDescription(sanitized.getDescription());

        Education savedEducation = educationRepository.save(education);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "education", savedEducation,
                "message", "Education updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete education",
        description = "Secure version: only allows deleting your own education"
    )
    public ResponseEntity<?> deleteEducation(
            @PathVariable Long id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Education> optEducation = educationRepository.findById(id);
        if (optEducation.isEmpty() || !Objects.equals(optEducation.get().getUserId(), userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden or not found"));
        }

        educationRepository.deleteById(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Education deleted successfully"
        ));
    }
}
