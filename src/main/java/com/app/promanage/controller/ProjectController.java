package com.app.promanage.controller;

import com.app.promanage.model.Project;
import com.app.promanage.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    @Autowired private ProjectRepository projectRepository;

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody Project project) {
        project.setCreatedOn(LocalDate.now());
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @GetMapping
    public List<Project> getAll() {
        return projectRepository.findAll();
    }
}