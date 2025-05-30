package com.app.promanage.service;

import com.app.promanage.dto.ProjectWithTaskStatsDTO;
import com.app.promanage.model.Project;
import com.app.promanage.model.Task;
import com.app.promanage.model.User;
import com.app.promanage.repository.ProjectRepository;
import com.app.promanage.repository.TaskRepository;
import com.app.promanage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public Task save(Task task) {
        User creator = getAuthenticatedUser();

        if (task.getProject() == null || task.getProject().getId() == null) {
            throw new IllegalArgumentException("Task must be associated with a valid project.");
        }

        UUID projectId = task.getProject().getId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found with ID: " + projectId));

        boolean isUserAssignedToProject = project.getAssignees() != null &&
                project.getAssignees().stream().anyMatch(user -> user.getId().equals(creator.getId()));
        boolean isCreator = project.getCreatedBy() != null &&
                project.getCreatedBy().getId().equals(creator.getId());

        if (!isUserAssignedToProject && !isCreator) {
            throw new SecurityException("Only project assignees or the creator can create tasks.");
        }

        // Resolve assignees
        List<User> resolvedAssignees = new ArrayList<>();
        if (task.getAssignees() != null) {
            for (User u : task.getAssignees()) {
                User assignee = userRepository.findById(u.getId())
                        .orElseThrow(() -> new NoSuchElementException("Assignee not found with ID: " + u.getId()));
                resolvedAssignees.add(assignee);
            }
        }

        // Handle parent task if it's a subtask
        if (task.getParentTask() != null && task.getParentTask().getId() != null) {
            Task parentTask = taskRepository.findById(task.getParentTask().getId())
                    .orElseThrow(() -> new NoSuchElementException("Parent task not found with ID: " + task.getParentTask().getId()));
            if (!parentTask.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Parent task must belong to the same project.");
            }
            task.setParentTask(parentTask);
        } else {
            task.setParentTask(null);
        }

        task.setAssignees(resolvedAssignees);
        task.setCreatedBy(creator);
        task.setReporter(creator);
        task.setCreatedOn(new Date());
        task.setProject(project);

        return taskRepository.save(task);
    }

    public Task update(UUID id, Task updatedTask) {
        User user = getAuthenticatedUser();
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with ID: " + id));

        Project project = existingTask.getProject();
        boolean isUserAssigned = project.getAssignees() != null &&
                project.getAssignees().stream().anyMatch(u -> u.getId().equals(user.getId()));
        boolean isCreator = project.getCreatedBy() != null &&
                project.getCreatedBy().getId().equals(user.getId());

        if (!isUserAssigned && !isCreator) {
            throw new SecurityException("You are not authorized to update this task.");
        }

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setStatus(updatedTask.getStatus());

        // Update assignees
        List<User> updatedAssignees = new ArrayList<>();
        if (updatedTask.getAssignees() != null) {
            for (User u : updatedTask.getAssignees()) {
                User assignee = userRepository.findById(u.getId())
                        .orElseThrow(() -> new NoSuchElementException("Assignee not found with ID: " + u.getId()));
                updatedAssignees.add(assignee);
            }
        }
        existingTask.setAssignees(updatedAssignees);

        // Handle parent task if changed
        if (updatedTask.getParentTask() != null && updatedTask.getParentTask().getId() != null) {
            Task parent = taskRepository.findById(updatedTask.getParentTask().getId())
                    .orElseThrow(() -> new NoSuchElementException("Parent task not found."));
            if (!parent.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Parent task must belong to the same project.");
            }
            existingTask.setParentTask(parent);
        } else {
            existingTask.setParentTask(null);
        }

        return taskRepository.save(existingTask);
    }

    public List<ProjectWithTaskStatsDTO> getProjectsOfCurrentUserWithTaskStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                UUID userId = currentUser.getId();

                List<Project> userProjects = projectRepository.findAll().stream()
                        .filter(project ->
                                (project.getCreatedBy() != null && userId.equals(project.getCreatedBy().getId())) ||
                                        (project.getAssignees() != null &&
                                                project.getAssignees().stream().anyMatch(assignee -> userId.equals(assignee.getId())))
                        )
                        .toList();

                List<ProjectWithTaskStatsDTO> result = new ArrayList<>();

                for (Project project : userProjects) {
                    UUID projectId = project.getId();

                    long totalTasks = taskRepository.countByProjectId(projectId);
                    long doneTasks = taskRepository.countByProjectIdAndStatus(projectId, 3); // Assuming status 3 = DONE

                    result.add(new ProjectWithTaskStatsDTO(project, totalTasks, doneTasks));
                }

                return result;
            }
        }
        throw new SecurityException("Unauthorized access.");
    }

    public void delete(UUID id) {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with ID: " + id));

        Project project = task.getProject();
        boolean isUserAssigned = project.getAssignees() != null &&
                project.getAssignees().stream().anyMatch(u -> u.getId().equals(user.getId()));
        boolean isCreator = project.getCreatedBy() != null &&
                project.getCreatedBy().getId().equals(user.getId());

        if (!isUserAssigned && !isCreator) {
            throw new SecurityException("You are not authorized to delete this task.");
        }

        taskRepository.deleteById(id);
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> getById(UUID id) {
        return taskRepository.findById(id);
    }

    public List<Task> getByProjectId(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("Authenticated user not found with email: " + email));
        }
        throw new SecurityException("Unauthenticated access.");
    }
}