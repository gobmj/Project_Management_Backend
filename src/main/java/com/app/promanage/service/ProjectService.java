package com.app.promanage.service;

import com.app.promanage.model.Project;
import com.app.promanage.model.Role;
import com.app.promanage.model.User;
import com.app.promanage.repository.ProjectRepository;
import com.app.promanage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public Project save(Project project) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();

                if (currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN) {
                    // Set creator and creation date
                    project.setCreatedBy(currentUser);
                    project.setCreatedOn(new Date());

                    // Resolve assignees from passed User IDs
                    if (project.getAssignees() != null && !project.getAssignees().isEmpty()) {
                        List<User> resolvedAssignees = new ArrayList<>();
                        for (User u : project.getAssignees()) {
                            userRepository.findById(u.getId()).ifPresent(resolvedAssignees::add);
                        }
                        project.setAssignees(resolvedAssignees);
                    }

                    return projectRepository.save(project);
                } else {
                    throw new SecurityException("Only MANAGER or ADMIN can create projects.");
                }
            }
        }
        throw new SecurityException("Unauthorized or unauthenticated access.");
    }

    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> getById(UUID id) {
        return projectRepository.findById(id);
    }
}