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
    private List<String> comments;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Milestone milestone;

    @ManyToOne
    private User reporter;

    @ManyToMany
    private List<User> assignees;

    @ManyToOne
    private Task parentTask;
}