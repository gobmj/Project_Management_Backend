package com.app.promanage.controller;

import com.app.promanage.model.Project;
import com.app.promanage.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Create project
    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project) {
        return ResponseEntity.ok(projectService.save(project));
    }

    // Get all projects (admin-level access)
    @GetMapping
    public ResponseEntity<List<Project>> getAll() {
        return ResponseEntity.ok(projectService.getAll());
    }

    // Get project by ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable UUID id) {
        return projectService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-projects")
    public ResponseEntity<List<Project>> getMyProjects() {
        return ResponseEntity.ok(projectService.getProjectsOfCurrentUser());
    }

    // Delete a project by ID (only creator who is ADMIN or MANAGER)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("Project deleted successfully.");
    }

    // Update project by ID
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable UUID id, @RequestBody Project updatedProject) {
        try {
            Project project = projectService.updateProject(id, updatedProject);
            return ResponseEntity.ok(project);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @PatchMapping("/{projectId}/add-assignee")
    public ResponseEntity<String> addAssigneeToProject(
            @PathVariable UUID projectId,
            @RequestParam String email) {
        try {
            projectService.addAssigneeByEmail(projectId, email);
            return ResponseEntity.ok("User added as assignee successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PatchMapping("/{projectId}/remove-assignee")
    public ResponseEntity<String> removeAssigneeFromProject(
            @PathVariable UUID projectId,
            @RequestParam String email) {
        try {
            projectService.removeAssigneeByEmail(projectId, email);
            return ResponseEntity.ok("User removed from project successfully.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

}
