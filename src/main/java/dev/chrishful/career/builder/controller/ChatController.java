package dev.chrishful.career.builder.controller;

import dev.chrishful.career.builder.dto.JobApplicationDto;
import dev.chrishful.career.builder.dto.JobApplicationResponseDto;
import dev.chrishful.career.builder.dto.StatusUpdateRequestDto;
import dev.chrishful.career.builder.service.DatabaseService;
import dev.chrishful.career.builder.service.GeminiService;
import dev.chrishful.career.builder.tools.JobTrackerTool;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class ChatController {


    private final DatabaseService databaseService;

    private final JobTrackerTool jobTrackerTool;


    public ChatController(DatabaseService databaseService, JobTrackerTool jobTrackerTool) {
        this.databaseService = databaseService;
        this.jobTrackerTool = jobTrackerTool;
    }

    @GetMapping("/applications")
    public ResponseEntity<List<JobApplicationDto>> getApplications() {
        try {
            List<JobApplicationDto> apps = databaseService.getAllApplications();
            return ResponseEntity.ok(apps != null ? apps : Collections.emptyList());
        } catch (Exception e) {
            // Force the real error to print to your terminal logs
            System.err.println("--- CRITICAL BACKEND ERROR ---");
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @PostMapping("/applications")
    public ResponseEntity<JobApplicationResponseDto> updateApplications(@RequestBody JobApplicationDto application) {
        try {
            JobApplicationResponseDto response = databaseService.insertApplication(application);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Failed to insert application: " + e.getMessage());
            return ResponseEntity.status(500).body(new JobApplicationResponseDto(
                    false,
                    "Internal Server Error: " + e.getMessage(),
                    null,
                    null
            ));
        }
    }

    @DeleteMapping("/applications/{number}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID number) {
        try {
            boolean deleted = databaseService.deleteApplication(number);
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content is standard for successful deletes
            } else {
                return ResponseEntity.notFound().build(); // 404 if the number didn't exist
            }
        } catch (Exception e) {
            System.err.println("Failed to delete application: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PatchMapping("/applications/{id}")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequestDto request) {
        try {
            boolean updated = databaseService.updateApplicationStatus(id, request.status());
            return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Failed to update status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}