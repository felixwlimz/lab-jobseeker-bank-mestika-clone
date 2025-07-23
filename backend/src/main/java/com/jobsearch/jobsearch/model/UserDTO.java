package com.jobsearch.model;

public class UserDTO {
    private Long id;
    private String username;
    private String role;

    public UserDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getters and setters (or use Lombok if preferred)

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}