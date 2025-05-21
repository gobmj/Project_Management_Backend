package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String assignee;
    private String reporter;
    private String comments;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate createdOn;
    private String createdBy;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Milestone milestone;

    @ManyToOne
    private Task parent;
}