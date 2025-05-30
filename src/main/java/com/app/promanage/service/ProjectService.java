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

    public Project updateProject(UUID projectId, Project updatedProject) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            Optional<Project> projectOpt = projectRepository.findById(projectId);

            if (userOpt.isPresent() && projectOpt.isPresent()) {
                User currentUser = userOpt.get();
                Project existingProject = projectOpt.get();

                boolean isCreator = existingProject.getCreatedBy() != null &&
                        existingProject.getCreatedBy().getId().equals(currentUser.getId());
                boolean isPrivileged = currentUser.getRole().isAtLeast(Role.MANAGER);

                if (isCreator && isPrivileged) {
                    existingProject.setName(updatedProject.getName());
                    existingProject.setDescription(updatedProject.getDescription());
                    existingProject.setStartDate(updatedProject.getStartDate());
                    existingProject.setEndDate(updatedProject.getEndDate());
                    existingProject.setPriority(updatedProject.getPriority()); // <-- priority added

                    if (updatedProject.getAssignees() != null) {
                        List<User> resolvedAssignees = new ArrayList<>();
                        for (User u : updatedProject.getAssignees()) {
                            userRepository.findById(u.getId()).ifPresent(resolvedAssignees::add);
                        }
                        existingProject.setAssignees(resolvedAssignees);
                    }

                    return projectRepository.save(existingProject);
                } else {
                    throw new SecurityException("Only the project creator with MANAGER or ADMIN role can update this project.");
                }
            } else {
                throw new NoSuchElementException("Project or user not found.");
            }
        }
        throw new SecurityException("Unauthorized access.");
    }

    public void addAssigneeByEmail(UUID projectId, String userEmailToAssign) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUserEmail = authentication.getName();

            Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            Optional<User> userToAssignOpt = userRepository.findByEmail(userEmailToAssign);

            if (currentUserOpt.isEmpty() || projectOpt.isEmpty()) {
                throw new NoSuchElementException("Project or authenticated user not found.");
            }

            if (userToAssignOpt.isEmpty()) {
                throw new IllegalArgumentException("User with given email not found.");
            }

            User currentUser = currentUserOpt.get();
            Project project = projectOpt.get();
            User userToAssign = userToAssignOpt.get();

            // Only MANAGER or ADMIN who created the project can add assignees
            boolean isCreator = project.getCreatedBy() != null &&
                    project.getCreatedBy().getId().equals(currentUser.getId());
            boolean isPrivileged = currentUser.getRole().isAtLeast(Role.MANAGER);

            if (!(isCreator && isPrivileged)) {
                throw new SecurityException("Only the project creator with MANAGER or ADMIN role can assign users.");
            }

            // Check if already assigned
            List<User> assignees = project.getAssignees() != null ? project.getAssignees() : new ArrayList<>();
            boolean alreadyAssigned = assignees.stream().anyMatch(u -> u.getId().equals(userToAssign.getId()));

            if (alreadyAssigned) {
                throw new IllegalArgumentException("User is already an assignee.");
            }

            assignees.add(userToAssign);
            project.setAssignees(assignees);
            projectRepository.save(project);
        } else {
            throw new SecurityException("Unauthorized access.");
        }
    }
}