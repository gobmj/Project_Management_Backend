package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;

    private String status;

    @ElementCollection
    private List<String> comments = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    private String priority;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    private Milestone milestone;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignees = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;
}