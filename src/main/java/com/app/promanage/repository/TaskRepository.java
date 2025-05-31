package com.app.promanage.repository;

import com.app.promanage.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByMilestoneId(UUID milestoneId);

    List<Task> findByAssignees_IdOrReporter_IdOrCreatedBy_Id(UUID assigneeId, UUID reporterId, UUID createdById);
    int countByProject_Id(UUID projectId);
    int countByProject_IdAndStatus(UUID projectId, String status);
}
