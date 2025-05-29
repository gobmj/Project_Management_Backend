package com.app.promanage.service;

import com.app.promanage.model.Milestone;
import com.app.promanage.model.Project;
import com.app.promanage.model.Role;
import com.app.promanage.model.User;
import com.app.promanage.repository.MilestoneRepository;
import com.app.promanage.repository.ProjectRepository;
import com.app.promanage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public Milestone save(Milestone milestone) {
        User user = getAuthenticatedUser();

        if (milestone.getProject() == null || milestone.getProject().getId() == null) {
            throw new IllegalArgumentException("Milestone must contain a valid project ID.");
        }

        UUID projectId = milestone.getProject().getId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found for the given ID."));

        if (project.getCreatedBy() == null) {
            throw new IllegalStateException("Project does not have a creator.");
        }

        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You are not the creator of the project.");
        }

        if (!user.getRole().isAtLeast(Role.MANAGER)) {
            throw new SecurityException("Only MANAGER or ADMIN of the project can create a milestone.");
        }

        milestone.setCreatedBy(user);
        milestone.setProject(project);
        milestone.setCreatedOn(new Date());

        return milestoneRepository.save(milestone);
    }

    public Milestone update(UUID id, Milestone updatedMilestone) {
        User user = getAuthenticatedUser();

        Milestone existing = milestoneRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Milestone not found."));

        Project project = existing.getProject();

        if (project == null || project.getCreatedBy() == null ||
                !project.getCreatedBy().getId().equals(user.getId()) ||
                !user.getRole().isAtLeast(Role.MANAGER)) {
            throw new SecurityException("Only MANAGER or ADMIN who created the project can update the milestone.");
        }

        existing.setName(updatedMilestone.getName());
        existing.setStartDate(updatedMilestone.getStartDate());
        existing.setEndDate(updatedMilestone.getEndDate());
        existing.setDueDate(updatedMilestone.getDueDate());

        return milestoneRepository.save(existing);
    }

    public void delete(UUID id) {
        User user = getAuthenticatedUser();

        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Milestone not found."));

        Project project = milestone.getProject();

        if (project == null || project.getCreatedBy() == null ||
                !project.getCreatedBy().getId().equals(user.getId()) ||
                !user.getRole().isAtLeast(Role.MANAGER)) {
            throw new SecurityException("Only MANAGER or ADMIN who created the project can delete the milestone.");
        }

        milestoneRepository.deleteById(id);
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

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("User not found"));
        }
        throw new SecurityException("Unauthenticated access");
    }
}