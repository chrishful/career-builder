package dev.chrishful.career.builder.controller;

import dev.chrishful.career.builder.dto.ChatRequest;
import dev.chrishful.career.builder.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chat")
public class ChatController {

    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) throws Exception {
        String response = geminiService.chat(request.query());
        return ResponseEntity.ok(response);
    }
}