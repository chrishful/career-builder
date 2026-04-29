package dev.chrishful.career.builder.controller;

import com.google.adk.runner.Runner;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/v1")
public class AgentController {

    private static final String APP_NAME = "decision-agent";

    private final Runner runner;
    private final InMemorySessionService sessionService;

    public AgentController(LlmAgent decisionAgent) {
        this.sessionService = new InMemorySessionService();
        this.runner = Runner.builder()
                .appName(APP_NAME)
                .agent(decisionAgent)
                .sessionService(sessionService)
                .build();
    }

    @PostMapping("/talk")
    public ResponseEntity<String> talk(@RequestBody TalkRequest request) {
        Session session = sessionService.createSession(
                APP_NAME,
                request.userId(),
                new ConcurrentHashMap<>(),
                request.sessionId()
        ).blockingGet();

        Content userMessage = Content.fromParts(Part.fromText(request.message()));

        List<Event> events = runner.runAsync(request.userId(), session.id(), userMessage)
                .toList()
                .blockingGet();

        events.forEach(event -> {
            System.out.println("Event: " + event.getClass().getSimpleName());
            System.out.println("  " + event);
        });

        System.out.println("Events size: " + events.size());

        String response = events.stream()
                .filter(e -> {
                    if (e.content().isEmpty()) return false;
                    var parts = e.content().get().parts();
                    return parts.isPresent() && parts.get().getFirst().text().isPresent();
                })
                .reduce((first, second) -> second)
                .flatMap(e -> e.content().flatMap(Content::parts)
                        .map(parts -> parts.getFirst().text()
                                .orElse("No response")))
                .orElse("No response");

        return ResponseEntity.ok(response);
    }

    public record TalkRequest(String userId, String sessionId, String message) {}
}