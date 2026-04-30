package dev.chrishful.career.builder.controller;

import dev.chrishful.career.builder.dto.AgentResponse;
import dev.chrishful.career.builder.dto.ChatRequest;
import dev.chrishful.career.builder.dto.JobApplicationDto;
import dev.chrishful.career.builder.service.ExcelService;
import dev.chrishful.career.builder.service.GeminiService;
import dev.chrishful.career.builder.tools.JobTrackerTool;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class ChatController {

    private final GeminiService geminiService;

    private final ExcelService excelService;

    private final JobTrackerTool jobTrackerTool;


    public ChatController(GeminiService geminiService, ExcelService excelService, JobTrackerTool jobTrackerTool) {
        this.geminiService = geminiService;
        this.excelService = excelService;
        this.jobTrackerTool = jobTrackerTool;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@RequestBody ChatRequest request) throws Exception {
        return ResponseEntity.ok(geminiService.chat(request.query()));
    }

    @GetMapping("/get-applications")
    public ResponseEntity<List<JobApplicationDto>> getApplications() {
        try {
            List<JobApplicationDto> apps = excelService.readApplications();
            // Return an empty list instead of null to prevent frontend crashes
            return ResponseEntity.ok(apps != null ? apps : Collections.emptyList());
        } catch (Exception e) {
            // Log the error and return a 500 with an empty list fallback
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @PostMapping("/update-applications")
    public ResponseEntity<String> updateApplications(@RequestBody JobApplicationDto application) throws Exception {
        jobTrackerTool.updateJobTracker(application);
        return ResponseEntity.ok("Not implemented yet");
    }
}