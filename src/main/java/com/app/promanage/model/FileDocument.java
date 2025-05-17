package com.app.promanage.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class FileDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;

    @Lob
    private byte[] data;
}