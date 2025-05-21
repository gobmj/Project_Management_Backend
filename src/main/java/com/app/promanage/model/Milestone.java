package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Milestone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate createdOn;
    private String createdBy;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    private Project project;
}