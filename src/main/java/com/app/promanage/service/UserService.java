package com.app.promanage.service;

import com.app.promanage.dto.UserSummaryDTO;
import com.app.promanage.model.Milestone;
import com.app.promanage.model.Project;
import com.app.promanage.model.Task;
import com.app.promanage.model.User;
import com.app.promanage.repository.MilestoneRepository;
import com.app.promanage.repository.ProjectRepository;
import com.app.promanage.repository.TaskRepository;
import com.app.promanage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public UserSummaryDTO getUserSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        if (user.getRole().getLevel() == 1) { // ADMIN → Return all data
            int totalProjects = (int) projectRepository.count();
            int totalMilestones = (int) milestoneRepository.count();

            List<Task> allTasks = taskRepository.findAll();
            int totalTasks = allTasks.size();
            int completedTasks = (int) allTasks.stream()
                    .filter(task -> "3".equals(task.getStatus()))
                    .count();

            return new UserSummaryDTO(totalProjects, totalTasks, completedTasks, totalMilestones);
        }

        // For non-ADMIN users → return only their own/project-specific data
        List<Project> projects = projectRepository.findByAssignees_IdOrCreatedBy_Id(userId, userId);
        int totalProjects = projects.size();

        List<Task> tasks = taskRepository.findByAssignees_IdOrReporter_IdOrCreatedBy_Id(userId, userId, userId);
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream()
                .filter(task -> "3".equals(task.getStatus()))
                .count();

        // Milestones created by the user or in the projects they are part of
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        List<Milestone> milestonesInProjects = projectIds.isEmpty()
                ? List.of()
                : milestoneRepository.findByProject_IdIn(projectIds);
        List<Milestone> milestonesCreatedByUser = milestoneRepository.findByCreatedBy_Id(userId);

        Set<UUID> milestoneIds = new HashSet<>();
        milestonesInProjects.forEach(m -> milestoneIds.add(m.getId()));
        milestonesCreatedByUser.forEach(m -> milestoneIds.add(m.getId()));

        int totalMilestones = milestoneIds.size();

        return new UserSummaryDTO(totalProjects, totalTasks, completedTasks, totalMilestones);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(authority)
        );
    }

    public User setManager(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        user.setManager(true);
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getById(UUID id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}