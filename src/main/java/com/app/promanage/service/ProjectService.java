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

                if (currentUser.getRole().isAtLeast(Role.MANAGER)) {
                    project.setCreatedBy(currentUser);
                    project.setCreatedOn(new Date());

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

    public List<Project> getProjectsOfCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                UUID userId = currentUser.getId();

                return projectRepository.findAll().stream()
                        .filter(project ->
                                (project.getCreatedBy() != null && userId.equals(project.getCreatedBy().getId())) ||
                                        (project.getAssignees() != null &&
                                                project.getAssignees().stream().anyMatch(assignee -> userId.equals(assignee.getId())))
                        )
                        .toList();
            }
        }
        throw new SecurityException("Unauthorized access.");
    }

    public void deleteProject(UUID projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            Optional<Project> projectOpt = projectRepository.findById(projectId);

            if (userOpt.isPresent() && projectOpt.isPresent()) {
                User currentUser = userOpt.get();
                Project project = projectOpt.get();

                boolean isCreator = project.getCreatedBy() != null &&
                        project.getCreatedBy().getId().equals(currentUser.getId());
                boolean isPrivileged = currentUser.getRole().isAtLeast(Role.MANAGER);

                if (isCreator && isPrivileged) {
                    projectRepository.deleteById(projectId);
                } else {
                    throw new SecurityException("Only the project creator with MANAGER or ADMIN role can delete this project.");
                }
            } else {
                throw new NoSuchElementException("Project or user not found.");
            }
        }
        throw new SecurityException("Unauthorized access.");
    }
}
