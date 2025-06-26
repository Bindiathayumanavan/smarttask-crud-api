package com.smarttask.backend.controller;

import com.smarttask.backend.model.Task;
import com.smarttask.backend.model.User;
import com.smarttask.backend.repository.TaskRepository;
import com.smarttask.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all tasks
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    // Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        if (task.getUser() == null || task.getUser().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        return userRepository.findById(task.getUser().getId())
                .map(user -> {
                    task.setUser(user); // Set the full user object
                    Task savedTask = taskRepository.save(task);
                    return ResponseEntity.ok(savedTask);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a task
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task taskDetails) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(taskDetails.getTitle());
                    task.setDescription(taskDetails.getDescription());
                    task.setCompleted(taskDetails.isCompleted());

                    if (taskDetails.getUser() != null && taskDetails.getUser().getId() != null) {
                        userRepository.findById(taskDetails.getUser().getId()).ifPresent(task::setUser);
                    }

                    Task updatedTask = taskRepository.save(task);
                    return ResponseEntity.ok(updatedTask);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a task
    @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    return taskRepository.findById(id)
            .map(task -> {
                taskRepository.delete(task);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
}


    // Get tasks by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(taskRepository.findByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
