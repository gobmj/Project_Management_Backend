package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}