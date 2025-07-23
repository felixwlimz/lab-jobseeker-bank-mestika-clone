package com.jobsearch.controller;

import com.jobsearch.model.JobApplication;
import com.jobsearch.repository.JobApplicationRepository;
import com.jobsearch.service.AuthService;
import com.jobsearch.util.SanitizerUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

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

    @PostMapping
    public ResponseEntity<?> applyForJob(
            @RequestBody JobApplication application,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
         JobApplication sanitized = SanitizerUtil.sanitizeInputRecursive(application);
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        sanitized.setUserId(userId); // Prevent ID injection
        sanitized.setAppliedAt(LocalDateTime.now());
        sanitized.setStatus(JobApplication.Status.pending);

        JobApplication saved = jobApplicationRepository.save(sanitized);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "application", saved,
                "message", "Application submitted successfully"
        ));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyApplications(@CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        List<JobApplication> applications = jobApplicationRepository.findByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "applications", applications));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
        
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<JobApplication> optionalApp = jobApplicationRepository.findById(id);
        if (optionalApp.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Application not found"));
        }

        JobApplication application = optionalApp.get();

        // TODO: Only allow employer/admin to change status (implement role check)
        if (!Objects.equals(application.getUserId(), userId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden"));
        }

        try {
            application.setStatus(JobApplication.Status.valueOf(statusUpdate.get("status")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid status"));
        }

        JobApplication saved = jobApplicationRepository.save(application);
        return ResponseEntity.ok(Map.of("success", true, "application", saved, "message", "Status updated"));
    }

    // Admin-only: get all applications
    @GetMapping
    public ResponseEntity<?> getAllApplications(@CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        try {
            if (userId == null || !authService.isAdmin(token)) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admin access only"));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<JobApplication> apps = jobApplicationRepository.findAll();
        return ResponseEntity.ok(Map.of("success", true, "applications", apps));
    }

    // Admin or employer endpoint example
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsForJob(
            @PathVariable Long jobId,
            @CookieValue(value = "SESSION_TOKEN", required = false) String token
    ) {
        Long userId = getUserIdFromToken(token);
        try {
            if (userId == null || !authService.isEmployer(token)) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Forbidden"));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<JobApplication> apps = jobApplicationRepository.findByJobId(jobId);
        return ResponseEntity.ok(Map.of("success", true, "applications", apps));
    }
}
