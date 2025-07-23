package com.jobsearch.controller;

import com.jobsearch.model.Job;
import com.jobsearch.model.Skill;
import com.jobsearch.model.User;
import com.jobsearch.repository.SkillRepository;
import com.jobsearch.util.SanitizerUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Secure Skills management API with proper access control")
public class SkillController {

    @Autowired
    private SkillRepository skillRepository;

    @GetMapping
    @Operation(
        summary = "Get skills of logged-in user",
        responses = @ApiResponse(responseCode = "200", description = "Skills retrieved successfully")
    )
    public ResponseEntity<?> getMySkills(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        List<Skill> skills = skillRepository.findByUserId(currentUser.getId());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "skills", skills
        ));
    }

    @PostMapping
    @Operation(summary = "Add new skill for the logged-in user")
    public ResponseEntity<?> addSkill(@RequestBody Skill skill, HttpSession session) {

        Skill sanitized = SanitizerUtil.sanitizeInputRecursive(skill);
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        sanitized.setUserId(currentUser.getId());
        sanitized.setCreatedAt(LocalDateTime.now());

        Skill savedSkill = skillRepository.save(sanitized);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "skill", savedSkill,
            "message", "Skill added successfully"
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a skill owned by the logged-in user")
    public ResponseEntity<?> updateSkill(@PathVariable Long id, @RequestBody Skill updatedSkill, HttpSession session) {

        Skill sanitized = SanitizerUtil.sanitizeInputRecursive(updatedSkill);
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Skill skill = skillRepository.findById(id).orElse(null);
        if (skill == null || !skill.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        skill.setSkillName(sanitized.getSkillName());
        skill.setLevel(sanitized.getLevel());

        Skill savedSkill = skillRepository.save(skill);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "skill", savedSkill,
            "message", "Skill updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a skill owned by the logged-in user")
    public ResponseEntity<?> deleteSkill(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Skill skill = skillRepository.findById(id).orElse(null);
        if (skill == null || !skill.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        skillRepository.deleteById(id);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Skill deleted successfully"
        ));
    }
}
