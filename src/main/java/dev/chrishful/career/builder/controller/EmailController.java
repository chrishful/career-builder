package dev.chrishful.career.builder.controller;

import dev.chrishful.career.builder.dto.EmailRequest;
import dev.chrishful.career.builder.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/v1")
public class EmailController {

    private final EmailService emailForwardService;

    public EmailController(EmailService emailForwardService) {
        this.emailForwardService = emailForwardService;
    }

    /**
     * POST /emails/forward
     * Body:
     * {
     *   "from":    "sender@example.com",
     *   "subject": "Hello!",
     *   "body":    "Email content here..."
     * }
     */
    @PostMapping("/forward")
    public ResponseEntity<String> forwardEmail(@RequestBody EmailRequest emailRequest) {
        try {
            System.out.printf("Received email to forward: %s%n, at %s%n", emailRequest.getPlain(), Instant.now());
            String agentResponse = emailForwardService.forward(emailRequest);
            return ResponseEntity.ok(agentResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to forward email to decisionAgent: " + e.getMessage());
        }
    }
}