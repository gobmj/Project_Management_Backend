package com.app.promanage.dto;

import com.app.promanage.model.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectWithTaskStatsDTO {
    private Project project;
    private long totalTasks;
    private long doneTasks;
}