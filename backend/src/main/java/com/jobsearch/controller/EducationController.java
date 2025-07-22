package com.jobsearch.controller;

import com.jobsearch.model.Education;
import com.jobsearch.repository.EducationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/education")
@Tag(name = "Education", description = "⚠️ VULNERABLE: Education management API with IDOR vulnerabilities")
public class EducationController {
    
    @Autowired
    private EducationRepository educationRepository;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserEducation(@PathVariable Long userId) {
        // Vulnerable: No access control
        List<Education> education = educationRepository.findByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("education", education);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<?> addEducation(@RequestBody Education education) {
        // Vulnerable: No authentication check, can add education for any user
        education.setCreatedAt(LocalDateTime.now());
        
        Education savedEducation = educationRepository.save(education);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("education", savedEducation);
        response.put("message", "Education added successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEducation(@PathVariable Long id, @RequestBody Education updatedEducation) {
        // Vulnerable: No authorization check
        Education education = educationRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (education == null) {
            response.put("success", false);
            response.put("message", "Education not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        education.setInstitution(updatedEducation.getInstitution());
        education.setDegree(updatedEducation.getDegree());
        education.setFieldOfStudy(updatedEducation.getFieldOfStudy());
        education.setStartDate(updatedEducation.getStartDate());
        education.setEndDate(updatedEducation.getEndDate());
        education.setDescription(updatedEducation.getDescription());
        
        Education savedEducation = educationRepository.save(education);
        
        response.put("success", true);
        response.put("education", savedEducation);
        response.put("message", "Education updated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete education", 
        description = "⚠️ VULNERABLE IDOR: Can delete ANY education record by ID without authorization"
    )
    public ResponseEntity<?> deleteEducation(
        @Parameter(description = "Education ID - can delete ANY user's education") @PathVariable Long id) {
        // VULNERABLE: No authorization check - classic IDOR vulnerability
        Map<String, Object> response = new HashMap<>();
        
        if (educationRepository.existsById(id)) {
            educationRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Education deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Education not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
