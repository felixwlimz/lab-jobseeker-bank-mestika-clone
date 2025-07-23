package com.jobsearch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_id")
    private Long jobId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        pending, reviewed, accepted, rejected
    }
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    // Constructors
    public JobApplication() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}
