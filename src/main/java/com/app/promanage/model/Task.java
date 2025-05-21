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

    @ManyToOne
    private Project project;

    @ManyToOne
    private Milestone milestone;

    @ManyToOne
    private User reporter;

    @ManyToMany
    private List<User> assignees;
}