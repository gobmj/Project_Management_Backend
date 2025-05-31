package com.app.promanage.service;

import com.app.promanage.model.Milestone;
import com.app.promanage.model.Project;
import com.app.promanage.model.Task;
import com.app.promanage.repository.MilestoneRepository;
import com.app.promanage.repository.ProjectRepository;
import com.app.promanage.repository.TaskRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private TaskRepository taskRepository;

    public byte[] generateProjectReport(UUID projectId, String type) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        LocalDate now = LocalDate.now();
        LocalDate fromDate = "monthly".equalsIgnoreCase(type) ? now.minusMonths(1) : now.minusWeeks(1);

        List<Milestone> milestones = milestoneRepository.findByProjectId(projectId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("📋 Project Report - " + project.getName())
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Report Type: " + type.toUpperCase()));
            document.add(new Paragraph("Date Range: " + fromDate + " to " + now));
            document.add(new Paragraph("--------------------------------------------------"));

            for (Milestone milestone : milestones) {
                document.add(new Paragraph("Milestone: " + milestone.getName())
                        .setBold().setFontSize(14).setMarginTop(15));

                List<Task> tasks = taskRepository.findByMilestoneId(milestone.getId());

                if (tasks.isEmpty()) {
                    document.add(new Paragraph("  No tasks found."));
                } else {
                    for (Task task : tasks) {
                        document.add(new Paragraph("  - Task: " + task.getTitle()
                                + " | Status: " + translateStatus(task.getStatus())
                                + " | Assignees: " + getAssigneeNames(task)));
                    }
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private String translateStatus(String statusCode) {
        return switch (statusCode) {
            case "1" -> "Open";
            case "2" -> "In Progress";
            case "3" -> "Completed";
            default -> "Unknown";
        };
    }

    private String getAssigneeNames(Task task) {
        if (task.getAssignees() == null || task.getAssignees().isEmpty()) {
            return "None";
        }
        return task.getAssignees().stream()
                .map(user -> user.getName() != null ? user.getName() : "Unnamed")
                .collect(Collectors.joining(", "));
    }
}