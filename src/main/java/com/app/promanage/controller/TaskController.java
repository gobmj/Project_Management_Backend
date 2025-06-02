package com.app.promanage.controller;

import com.app.promanage.model.Task;
import com.app.promanage.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.save(task));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAll() {
        return ResponseEntity.ok(taskService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable UUID id) {
        return taskService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getByProjectId(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getByProjectId(projectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable UUID id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.update(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/milestone/{milestoneId}")
    public ResponseEntity<Map<String, Object>> getTasksByMilestoneId(@PathVariable UUID milestoneId) {
        List<Task> tasks = taskService.getTasksByMilestoneId(milestoneId);
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(task -> "3".equals(task.getStatus())).count();

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", tasks);
        response.put("totalTasks", total);
        response.put("completedTasks", completed);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/add-assignee")
    public ResponseEntity<String> addAssigneeToTask(
            @PathVariable UUID taskId,
            @RequestParam String email) {
        try {
            taskService.addAssigneeToTask(taskId, email);
            return ResponseEntity.ok("User added as assignee to task successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }
    @GetMapping("/{userId}/due-dates")
    public ResponseEntity<List<Map<String, String>>> getTaskDueDatesByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.getTaskDueDatesByUserId(userId));
    }

}