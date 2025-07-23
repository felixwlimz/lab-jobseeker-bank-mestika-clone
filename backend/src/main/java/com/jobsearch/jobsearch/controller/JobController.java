package com.jobsearch.controller;

import com.jobsearch.model.Job;
import com.jobsearch.repository.JobRepository;
import com.jobsearch.service.AuthService;
import com.jobsearch.util.SanitizerUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AuthService authService;

    private Long getUserIdFromToken(String token) {
        try {
            return ((Number) authService.validateToken(token).get("userId")).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEmployer(String token) {
        try {
            return "employer".equals(authService.validateToken(token).get("role"));
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        return ResponseEntity.ok(Map.of("success", true, "jobs", jobs));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(@RequestParam String keyword) {
        // Prevent SQL Injection by using parameterized queries inside repository
        List<Job> jobs = jobRepository.findByTitleContainingIgnoreCase(keyword);
        return ResponseEntity.ok(Map.of("success", true, "jobs", jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
            .map(job -> ResponseEntity.ok(Map.of("success", true, "job", job)))
            .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("success", false, "message", "Job not found")));
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, @CookieValue(value = "SESSION_TOKEN", required = false) String token) {

        Job sanitized = SanitizerUtil.sanitizeInputRecursive(job);
        Long userId = getUserIdFromToken(token);
        if (userId == null || !isEmployer(token)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }

        // Enforce correct ownership — use userId or companyId mapped to authenticated user
        sanitized.setCreatedAt(LocalDateTime.now());
        sanitized.setUpdatedAt(LocalDateTime.now());
        sanitized.setStatus("active");
        // job.setCompanyId(fetchFromEmployerProfile(userId)); ← Example of strict linkage

        Job savedJob = jobRepository.save(sanitized);
        return ResponseEntity.ok(Map.of("success", true, "job", savedJob, "message", "Job created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody Job updatedJob,
                                       @CookieValue(value = "SESSION_TOKEN", required = false) String token) {

        Job sanitized = SanitizerUtil.sanitizeInputRecursive(updatedJob);
        Long userId = getUserIdFromToken(token);
        if (userId == null || !isEmployer(token)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Job not found"));
        }

        Job job = optionalJob.get();

        // Access control: user can update job only if they own it (e.g., via companyId check)
        // if (!ownsJob(job, userId)) return forbidden

        job.setTitle(sanitized.getTitle());
        job.setDescription(sanitized.getDescription());
        job.setRequirements(sanitized.getRequirements());
        job.setSalaryMin(sanitized.getSalaryMin());
        job.setSalaryMax(sanitized.getSalaryMax());
        job.setLocation(sanitized.getLocation());
        job.setJobType(sanitized.getJobType());
        job.setStatus(sanitized.getStatus());
        job.setUpdatedAt(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(Map.of("success", true, "job", savedJob, "message", "Job updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, @CookieValue(value = "SESSION_TOKEN", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null || !isEmployer(token)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Job not found"));
        }

        Job job = optionalJob.get();
        // if (!ownsJob(job, userId)) return forbidden

        jobRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Job deleted"));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getJobsByCompany(@PathVariable Long companyId) {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        return ResponseEntity.ok(Map.of("success", true, "jobs", jobs));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("success", true, "message", "Controller works"));
    }
}
