package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @ManyToOne
    private User createdBy;
}