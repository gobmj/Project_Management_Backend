package com.app.promanage.controller;

import com.app.promanage.model.Task;
import com.app.promanage.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired private TaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        task.setCreatedOn(LocalDate.now());
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @GetMapping
    public List<Task> getAll() {
        return taskRepository.findAll();
    }
}