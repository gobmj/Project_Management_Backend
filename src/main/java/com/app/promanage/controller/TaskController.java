package com.app.promanage.controller;

import com.app.promanage.model.Task;
import com.app.promanage.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepo;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskRepo.save(task));
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        return taskRepo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody Task updated) {
        return taskRepo.findById(id).map(t -> {
            t.setTitle(updated.getTitle());
            t.setDescription(updated.getDescription());
            return ResponseEntity.ok(taskRepo.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}