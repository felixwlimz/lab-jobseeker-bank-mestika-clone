package com.jobsearch.controller;

import com.jobsearch.model.Job;
import com.jobsearch.model.User;
import com.jobsearch.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    
    @Autowired
    private JobRepository jobRepository;
    
    @GetMapping
    public ResponseEntity<?> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("jobs", jobs);
        
        return ResponseEntity.ok(response);
    }
    
    // Vulnerable: SQL injection in search
    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(@RequestParam String keyword) {
        List<Job> jobs = jobRepository.searchJobs(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("jobs", jobs);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        Job job = jobRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        if (job != null) {
            response.put("success", true);
            response.put("job", job);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Job not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, HttpSession session) {
        // Vulnerable: No proper authorization check
        User user = (User) session.getAttribute("user");
        
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Vulnerable: Can create job for any company by manipulating companyId
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job.setStatus("active");
        
        Job savedJob = jobRepository.save(job);
        
        response.put("success", true);
        response.put("job", savedJob);
        response.put("message", "Job created successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getJobsByCompany(@PathVariable Long companyId) {
        // Vulnerable: No access control - any user can see any company's jobs
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("jobs", jobs);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody Job updatedJob, HttpSession session) {
        // Vulnerable: No authorization check
        Job job = jobRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (job == null) {
            response.put("success", false);
            response.put("message", "Job not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Vulnerable: Can update any job without ownership verification
        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setRequirements(updatedJob.getRequirements());
        job.setSalaryMin(updatedJob.getSalaryMin());
        job.setSalaryMax(updatedJob.getSalaryMax());
        job.setLocation(updatedJob.getLocation());
        job.setJobType(updatedJob.getJobType());
        job.setStatus(updatedJob.getStatus());
        job.setUpdatedAt(LocalDateTime.now());
        
        Job savedJob = jobRepository.save(job);
        
        response.put("success", true);
        response.put("job", savedJob);
        response.put("message", "Job updated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, HttpSession session) {
        // Vulnerable: No authorization check
        Map<String, Object> response = new HashMap<>();
        
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Job deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Job not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Controller works");
        return ResponseEntity.ok(response);
    }
}
