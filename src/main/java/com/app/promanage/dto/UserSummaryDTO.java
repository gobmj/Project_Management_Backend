package com.app.promanage.dto;

public class UserSummaryDTO {
    private int totalProjects;
    private int totalTasks;
    private int totalMilestones;

    public UserSummaryDTO(int totalProjects, int totalTasks, int totalMilestones) {
        this.totalProjects = totalProjects;
        this.totalTasks = totalTasks;
        this.totalMilestones = totalMilestones;
    }

    public int getTotalProjects() {
        return totalProjects;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getTotalMilestones() {
        return totalMilestones;
    }
}