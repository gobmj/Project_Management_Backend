package com.app.promanage.repository;

import com.app.promanage.model.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findByProjectId(UUID projectId);
}