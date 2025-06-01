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
        // Projects where user is assignee or creator
        List<Project> projects = projectRepository.findByAssignees_IdOrCreatedBy_Id(userId, userId);
        int totalProjects = projects.size();

        // Tasks where user is assignee, reporter, or creator
        List<Task> tasks = taskRepository.findByAssignees_IdOrReporter_IdOrCreatedBy_Id(userId, userId, userId);
        int totalTasks = tasks.size();

        int completedTasks = (int) tasks.stream()
                .filter(task -> "3".equals(task.getStatus()))
                .count();

        // Collect project IDs for milestone query
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();

        // Milestones related to those projects
        List<Milestone> milestonesInProjects = projectIds.isEmpty() ? List.of() : milestoneRepository.findByProject_IdIn(projectIds);

        // Milestones created by user directly
        List<Milestone> milestonesCreatedByUser = milestoneRepository.findByCreatedBy_Id(userId);

        // Combine milestones into a set to avoid duplicates
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