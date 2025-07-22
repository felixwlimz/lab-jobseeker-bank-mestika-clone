package com.jobsearch.controller;

import com.jobsearch.model.Skill;
import com.jobsearch.repository.SkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "⚠️ VULNERABLE: Skills management API with no access control")
public class SkillController {
    
    @Autowired
    private SkillRepository skillRepository;
    
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get user skills", 
        description = "⚠️ VULNERABLE: No access control - can view any user's skills",
        responses = {
            @ApiResponse(responseCode = "200", description = "Skills retrieved successfully")
        }
    )
    public ResponseEntity<?> getUserSkills(
        @Parameter(description = "User ID - can be any user ID") @PathVariable Long userId) {
        // Vulnerable: No access control
        List<Skill> skills = skillRepository.findByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("skills", skills);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(
        summary = "Add new skill", 
        description = "⚠️ VULNERABLE: No authentication - can add skill for any user"
    )
    public ResponseEntity<?> addSkill(@RequestBody Skill skill) {
        // Vulnerable: No authentication check, can add skill for any user
        skill.setCreatedAt(LocalDateTime.now());
        
        Skill savedSkill = skillRepository.save(skill);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("skill", savedSkill);
        response.put("message", "Skill added successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update skill", 
        description = "⚠️ VULNERABLE: No authorization - can update any user's skill"
    )
    public ResponseEntity<?> updateSkill(@PathVariable Long id, @RequestBody Skill updatedSkill) {
        // Vulnerable: No authorization check
        Skill skill = skillRepository.findById(id).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        
        if (skill == null) {
            response.put("success", false);
            response.put("message", "Skill not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        skill.setSkillName(updatedSkill.getSkillName());
        skill.setLevel(updatedSkill.getLevel());
        
        Skill savedSkill = skillRepository.save(skill);
        
        response.put("success", true);
        response.put("skill", savedSkill);
        response.put("message", "Skill updated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete skill", 
        description = "⚠️ VULNERABLE IDOR: Can delete ANY skill by ID without authorization check"
    )
    public ResponseEntity<?> deleteSkill(
        @Parameter(description = "Skill ID - can delete ANY user's skill") @PathVariable Long id) {
        // VULNERABLE: No authorization check - classic IDOR vulnerability
        Map<String, Object> response = new HashMap<>();
        
        if (skillRepository.existsById(id)) {
            skillRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Skill deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Skill not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
