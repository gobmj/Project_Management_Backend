package com.app.promanage.controller;

import com.app.promanage.dto.ProjectWithTaskStatsDTO;
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

    // Get all projects of the currently logged-in user WITH TASK COUNTS
    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectWithTaskStatsDTO>> getMyProjects() {
        List<ProjectWithTaskStatsDTO> projectsWithStats = projectService.getProjectsOfCurrentUserWithTaskStats();
        return ResponseEntity.ok(projectsWithStats);
    }

    // Delete a project by ID (only creator who is ADMIN or MANAGER)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("Project deleted successfully.");
    }
}