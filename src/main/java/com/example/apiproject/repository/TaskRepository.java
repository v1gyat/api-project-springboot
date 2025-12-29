package com.example.apiproject.repository;

import com.example.apiproject.entity.InternTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<InternTask, Long> {
    // JpaRepository provides all basic CRUD operations
    // No need to write custom methods for basic operations like save() and
    // findAll()
}
