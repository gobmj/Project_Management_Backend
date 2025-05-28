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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (milestone.getProject() == null || milestone.getProject().getId() == null) {
                    throw new IllegalArgumentException("Milestone must contain a valid project ID.");
                }

                UUID projectId = milestone.getProject().getId();
                Optional<Project> projectOpt = projectRepository.findById(projectId);

                if (projectOpt.isEmpty()) {
                    throw new NoSuchElementException("Project not found for the given ID.");
                }

                Project project = projectOpt.get();

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
                milestone.setProject(project); // set fully-loaded project
                milestone.setCreatedOn(new Date());

                return milestoneRepository.save(milestone);
            }
        }

        throw new SecurityException("Unauthorized or unauthenticated access.");
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
