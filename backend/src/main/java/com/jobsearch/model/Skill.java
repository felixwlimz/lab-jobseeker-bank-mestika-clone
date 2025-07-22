package com.jobsearch.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "skill_name", nullable = false)
    private String skillName;
    
    @Enumerated(EnumType.STRING)
    private Level level;

    public enum Level {
        beginner, intermediate, advanced, expert
    }
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Skill() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
