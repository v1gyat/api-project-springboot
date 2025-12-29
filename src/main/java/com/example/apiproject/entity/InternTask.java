package com.example.apiproject.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "intern_tasks")
public class InternTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    // Default constructor (required by JPA)
    public InternTask() {
    }

    // Constructor with fields
    public InternTask(String description, Boolean isCompleted) {
        this.description = description;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    @Override
    public String toString() {
        return "InternTask{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
