package com.app.promanage.repository;

import com.app.promanage.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByMilestoneId(UUID milestoneId);
}
