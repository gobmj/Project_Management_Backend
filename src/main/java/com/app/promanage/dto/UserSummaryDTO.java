package com.app.promanage.dto;

public class UserSummaryDTO {
    private int totalProjects;
    private int totalTasks;
    private int completedTasks;
    private int totalMilestones;

    public UserSummaryDTO(int totalProjects, int totalTasks, int completedTasks, int totalMilestones) {
        this.totalProjects = totalProjects;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.totalMilestones = totalMilestones;
    }

    public int getTotalProjects() {
        return totalProjects;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getTotalMilestones() {
        return totalMilestones;
    }
}