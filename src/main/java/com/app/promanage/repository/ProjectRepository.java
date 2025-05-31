package com.app.promanage.repository;

import com.app.promanage.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByAssignees_Id(UUID userId);
    List<Project> findByAssignees_IdOrCreatedBy_Id(UUID assigneeId, UUID createdById);
}
