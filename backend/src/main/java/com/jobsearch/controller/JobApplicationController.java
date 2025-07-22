package com.jobsearch.controller;

import com.jobsearch.model.JobApplication;
import com.jobsearch.model.User;
import com.jobsearch.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @PostMapping
    public ResponseEntity<?> applyForJob(@RequestBody JobApplication application, HttpSession session) {
        // Vulnerable: No proper authentication check
        User user = (User) session.getAttribute("user");
        
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Vulnerable: Can apply for job as any user by manipulating userId
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus(JobApplication.Status.pending);
        
        JobApplication savedApplication = jobApplicationRepository.save(application);
        
        response.put("success", true);
        response.put("application", savedApplication);
        response.put("message", "Application submitted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserApplications(@PathVariable Long userId) {
        // Vulnerable: No access control - any user can see any user's applications
        List<JobApplication> applications = jobApplicationRepository.findByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("applications", applications);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getJobApplications(@PathVariable Long jobId) {
        // Vulnerable: No access control - any user can see applications for any job
        List<JobApplication> applications = jobApplicationRepository.findByJobId(jobId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("applications", applications);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id, 
                                                    @RequestBody Map<String, String> statusUpdate,
                                                    HttpSession session) {
        // Vulnerable: No authorization check
        JobApplication application = jobApplicationRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (application == null) {
            response.put("success", false);
            response.put("message", "Application not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Vulnerable: Can update any application status
        String status = statusUpdate.get("status");
        application.setStatus(JobApplication.Status.valueOf(status));
        
        JobApplication savedApplication = jobApplicationRepository.save(application);
        
        response.put("success", true);
        response.put("application", savedApplication);
        response.put("message", "Application status updated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<?> getAllApplications() {
        // Vulnerable: No access control - exposes all applications
        List<JobApplication> applications = jobApplicationRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("applications", applications);
        
        return ResponseEntity.ok(response);
    }
}
