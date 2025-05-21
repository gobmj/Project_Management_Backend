package com.app.promanage.service;

import com.app.promanage.model.Task;
import com.app.promanage.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public Task save(Task task) {
        return taskRepository.save(task);
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
