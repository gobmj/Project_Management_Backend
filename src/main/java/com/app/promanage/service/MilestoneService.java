package com.app.promanage.service;

import com.app.promanage.model.Milestone;
import com.app.promanage.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;

    public Milestone save(Milestone milestone) {
        return milestoneRepository.save(milestone);
    }

    public List<Milestone> getAll() {
        return milestoneRepository.findAll();
    }

    public Optional<Milestone> getById(UUID id) {
        return milestoneRepository.findById(id);
    }

    public List<Milestone> getByProjectId(UUID projectId) {
        return milestoneRepository.findByProjectId(projectId);
    }
}
