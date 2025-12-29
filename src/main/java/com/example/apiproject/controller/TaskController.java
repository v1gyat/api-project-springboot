package com.example.apiproject.controller;

import com.example.apiproject.entity.InternTask;
import com.example.apiproject.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * POST /tasks - Create a new task
     * 
     * @param task - The task object from request body
     * @return The saved task with generated ID
     */
    @PostMapping
    public ResponseEntity<InternTask> createTask(@RequestBody InternTask task) {
        InternTask savedTask = taskRepository.save(task);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    /**
     * GET /tasks - Retrieve all tasks
     * 
     * @return List of all tasks in the database
     */
    @GetMapping
    public ResponseEntity<List<InternTask>> getAllTasks() {
        List<InternTask> tasks = taskRepository.findAll();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
}
