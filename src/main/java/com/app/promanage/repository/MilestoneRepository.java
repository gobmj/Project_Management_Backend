package com.app.promanage.repository;

import com.app.promanage.model.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findByProjectId(UUID projectId);

    List<Milestone> findByProject_IdIn(List<UUID> projectIds);

    List<Milestone> findByCreatedBy_Id(UUID userId);
}
