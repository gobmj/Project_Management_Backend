package com.app.promanage.controller;

import com.app.promanage.service.EmailService;
import com.app.promanage.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private EmailService emailService;

    /**
     * Endpoint to generate and send a project report to the specified email.
     *
     * @param projectId The UUID of the project
     * @param type      The type of report: "weekly" or "monthly"
     * @param email     The recipient's email address
     * @return A success message if sent successfully
     */
    @GetMapping("/send")
    public ResponseEntity<String> sendProjectReport(
            @RequestParam UUID projectId,
            @RequestParam String type,
            @RequestParam String email
    ) {
        try {
            byte[] reportPdf = reportService.generateProjectReport(projectId, type);
            emailService.sendEmailWithAttachment(
                    email,
                    "📊 Project Report - " + type.toUpperCase(),
                    "Please find the attached " + type + " project report.",
                    reportPdf
            );
            return ResponseEntity.ok("✅ Report sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Failed to send report: " + e.getMessage());
        }
    }

    /**
     * Endpoint to generate and download a project report PDF.
     *
     * @param projectId The UUID of the project
     * @param type      The type of report: "weekly" or "monthly"
     * @return PDF file as a byte stream
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadProjectReport(
            @RequestParam UUID projectId,
            @RequestParam String type
    ) {
        try {
            byte[] reportPdf = reportService.generateProjectReport(projectId, type);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "project-report-" + type + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportPdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}