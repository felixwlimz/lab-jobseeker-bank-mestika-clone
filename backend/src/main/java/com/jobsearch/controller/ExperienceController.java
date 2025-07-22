package com.jobsearch.controller;

import com.jobsearch.model.Experience;
import com.jobsearch.repository.ExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experience")
public class ExperienceController {
    
    @Autowired
    private ExperienceRepository experienceRepository;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserExperience(@PathVariable Long userId) {
        // Vulnerable: No access control
        List<Experience> experience = experienceRepository.findByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("experience", experience);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<?> addExperience(@RequestBody Experience experience) {
        // Vulnerable: No authentication check, can add experience for any user
        experience.setCreatedAt(LocalDateTime.now());
        
        Experience savedExperience = experienceRepository.save(experience);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("experience", savedExperience);
        response.put("message", "Experience added successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExperience(@PathVariable Long id, @RequestBody Experience updatedExperience) {
        // Vulnerable: No authorization check
        Experience experience = experienceRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (experience == null) {
            response.put("success", false);
            response.put("message", "Experience not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        experience.setCompany(updatedExperience.getCompany());
        experience.setPosition(updatedExperience.getPosition());
        experience.setStartDate(updatedExperience.getStartDate());
        experience.setEndDate(updatedExperience.getEndDate());
        experience.setDescription(updatedExperience.getDescription());
        
        Experience savedExperience = experienceRepository.save(experience);
        
        response.put("success", true);
        response.put("experience", savedExperience);
        response.put("message", "Experience updated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExperience(@PathVariable Long id) {
        // Vulnerable: No authorization check
        Map<String, Object> response = new HashMap<>();
        
        if (experienceRepository.existsById(id)) {
            experienceRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Experience deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Experience not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
