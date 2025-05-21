package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private Date dueDate;

    @ManyToOne
    private Project project;
}