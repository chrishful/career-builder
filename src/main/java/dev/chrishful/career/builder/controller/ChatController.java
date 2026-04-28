package dev.chrishful.career.builder.controller;

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
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) throws Exception {
        String response = geminiService.chat(request.query());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-applications")
    public ResponseEntity<List<JobApplicationDto>> getApplications() throws Exception {
        return ResponseEntity.ok(excelService.readApplications());
    }
}