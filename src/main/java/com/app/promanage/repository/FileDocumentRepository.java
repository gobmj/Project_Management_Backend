package com.app.promanage.repository;

import com.app.promanage.model.FileDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {}