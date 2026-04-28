package dev.chrishful.career.builder.controller;

import dev.chrishful.career.builder.dto.AgentResponse;
import dev.chrishful.career.builder.dto.ChatRequest;
import dev.chrishful.career.builder.dto.JobApplicationDto;
import dev.chrishful.career.builder.service.ExcelService;
import dev.chrishful.career.builder.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class ChatController {

    private final GeminiService geminiService;

    private final ExcelService excelService;


    public ChatController(GeminiService geminiService, ExcelService excelService) {
        this.geminiService = geminiService;
        this.excelService = excelService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@RequestBody ChatRequest request) throws Exception {
        return ResponseEntity.ok(geminiService.chat(request.query()));
    }

    @GetMapping("/get-applications")
    public ResponseEntity<List<JobApplicationDto>> getApplications() throws Exception {
        return ResponseEntity.ok(excelService.readApplications());
    }
}