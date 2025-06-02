package com.app.promanage.service;

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

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EmailService emailService;

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

    public Task addAssigneeToTask(UUID taskId, String assigneeEmail) {
        User currentUser = getAuthenticatedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Task not found with ID: " + taskId));
        Project project = task.getProject();

        boolean isUserAssignedToProject = project.getAssignees() != null &&
                project.getAssignees().stream().anyMatch(u -> u.getId().equals(currentUser.getId()));
        boolean isCreator = project.getCreatedBy() != null &&
                project.getCreatedBy().getId().equals(currentUser.getId());

        if (!isUserAssignedToProject && !isCreator) {
            throw new SecurityException("Only project assignees or the creator can add task assignees.");
        }

        User newAssignee = userRepository.findByEmail(assigneeEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + assigneeEmail));

        List<User> assignees = task.getAssignees() != null ? new ArrayList<>(task.getAssignees()) : new ArrayList<>();

        boolean alreadyAssigned = assignees.stream().anyMatch(u -> u.getId().equals(newAssignee.getId()));
        if (alreadyAssigned) {
            throw new IllegalArgumentException("User is already an assignee.");
        }

        assignees.add(newAssignee);
        task.setAssignees(assignees);
        Task savedTask = taskRepository.save(task);

        // Send email notification
        emailService.sendEmail(
                newAssignee.getEmail(),
                "You have been assigned to a task: " + task.getTitle(),
                "Hello " + newAssignee.getName() + ",\n\n" +
                        "You have been assigned as an assignee to the task \"" + task.getTitle() + "\" in project \"" + project.getName() + "\".\n\n" +
                        "Please check the task details.\n\nRegards,\nPromanage Team"
        );

        return savedTask;
    }

    public List<Task> getTasksByMilestoneId(UUID milestoneId) {
        return taskRepository.findByMilestoneId(milestoneId);
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

    public List<Map<String, String>> getTaskDueDatesByUserId(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        User user = optionalUser.get();
        List<Task> createdTasks = taskRepository.findAllByReporter(user);
        List<Task> assignedTasks = taskRepository.findAllByAssigneesContains(user);

        Set<Task> combinedTasks = new HashSet<>();
        combinedTasks.addAll(createdTasks);
        combinedTasks.addAll(assignedTasks);

        List<Map<String, String>> dueDates = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        for (Task task : combinedTasks) {
            if (task.getDueDate() != null) {
                Map<String, String> entry = new HashMap<>();
                entry.put("date", formatter.format(task.getDueDate()));
                entry.put("name", task.getTitle());
                dueDates.add(entry);
            }
        }

        return dueDates;
    }

}