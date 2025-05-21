package com.app.promanage.controller;

import com.app.promanage.model.Milestone;
import com.app.promanage.repository.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/milestones")
public class MilestoneController {
    @Autowired private MilestoneRepository milestoneRepository;

    @PostMapping
    public ResponseEntity<Milestone> create(@RequestBody Milestone milestone) {
        milestone.setCreatedOn(LocalDate.now());
        return ResponseEntity.ok(milestoneRepository.save(milestone));
    }

    @GetMapping
    public List<Milestone> getAll() {
        return milestoneRepository.findAll();
    }
}