package com.app.promanage.service;

import com.app.promanage.model.Project;
import com.app.promanage.model.Task;
import com.app.promanage.model.User;
import com.app.promanage.repository.TaskRepository;
import com.app.promanage.repository.UserRepository;
import com.app.promanage.repository.ProjectRepository;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        String email = authentication.getName();
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("User not found"));

        Project project = task.getProject();
        if (project == null || !projectRepository.existsById(project.getId())) {
            throw new IllegalArgumentException("Valid project must be linked to task");
        }

        List<User> assignees = task.getAssignees() != null ? task.getAssignees() : new ArrayList<>();
        boolean isCreatorAssigned = assignees.stream()
                .anyMatch(user -> user.getId().equals(creator.getId()));

        if (!isCreatorAssigned) {
            throw new SecurityException("Creator must be one of the assignees");
        }

        task.setCreatedBy(creator);
        task.setReporter(creator);
        task.setCreatedOn(new Date());

        if (task.getParentTask() != null) {
            UUID parentId = task.getParentTask().getId();
            Task parentTask = taskRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent task not found"));

            if (!parentTask.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Parent task must belong to the same project");
            }
            task.setParentTask(parentTask);
        }

        return taskRepository.save(task);
    }

    public Task updateTask(UUID id, Task updatedTask) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("User not found"));

        if (!user.getId().equals(existing.getCreatedBy().getId()) &&
                !user.getId().equals(existing.getReporter().getId())) {
            throw new SecurityException("Only the creator or reporter can update the task");
        }

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());
        existing.setStartDate(updatedTask.getStartDate());
        existing.setEndDate(updatedTask.getEndDate());
        existing.setDueDate(updatedTask.getDueDate());

        if (updatedTask.getAssignees() != null) {
            existing.setAssignees(updatedTask.getAssignees());
        }

        if (updatedTask.getParentTask() != null) {
            Task parent = taskRepository.findById(updatedTask.getParentTask().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent task not found"));
            if (!parent.getProject().getId().equals(existing.getProject().getId())) {
                throw new IllegalArgumentException("Parent task must belong to the same project");
            }
            existing.setParentTask(parent);
        }

        return taskRepository.save(existing);
    }

    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("User not found"));

        if (!user.getId().equals(task.getCreatedBy().getId()) &&
                !user.getId().equals(task.getReporter().getId())) {
            throw new SecurityException("Only the creator or reporter can delete this task");
        }

        taskRepository.delete(task);
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
}