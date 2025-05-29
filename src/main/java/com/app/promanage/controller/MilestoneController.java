package com.app.promanage.controller;

import com.app.promanage.model.Milestone;
import com.app.promanage.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {
    private final MilestoneService milestoneService;

    @PostMapping
    public ResponseEntity<Milestone> create(@RequestBody Milestone milestone) {
        return ResponseEntity.ok(milestoneService.save(milestone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Milestone> update(@PathVariable UUID id, @RequestBody Milestone updatedMilestone) {
        return ResponseEntity.ok(milestoneService.update(id, updatedMilestone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        milestoneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Milestone>> getAll() {
        return ResponseEntity.ok(milestoneService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Milestone> getById(@PathVariable UUID id) {
        return milestoneService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Milestone>> getByProjectId(@PathVariable UUID projectId) {
        return ResponseEntity.ok(milestoneService.getByProjectId(projectId));
    }
}
