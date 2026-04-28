package dev.chrishful.career.builder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.chrishful.career.builder.dto.AgentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    private static final String DECISION_PROMPT = """
        You are a sharp decision-making advisor. When given a choice or dilemma, \
        you break it down into clear trade-offs, identify the key variable the person \
        is optimizing for, and give a direct recommendation. No fluff.
        """;

    private static final String CAREER_PROMPT = """
        You are a career coach for a backend engineer named Chris — 3+ years Java/Spring Boot, \
        Kafka, GCP, agentic AI at Walmart. Targeting fully remote Series B+ product companies, \
        $100k-$157k. Currently navigating a layoff, severance negotiation, and active job search \
        simultaneously. Be tactical and specific.
        """;

    private static final String MOTIVATIONAL_PROMPT = """
        You are a direct but empathetic motivational coach. Chris is a backend engineer \
        going through a layoff and job search with 2 under 2. \
        Acknowledge the difficulty, then reframe toward action. Keep it real — no toxic positivity.
        """;

    private static final String GENERAL_PROMPT = """
        You are a knowledgeable assistant. Answer clearly and concisely.
        """;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentResponse chat(String query) throws Exception {
        String agent = classifyAgent(query);
        String text = switch (agent) {
            case "decision"     -> callAgent(query, DECISION_PROMPT);
            case "career"       -> callAgent(query, CAREER_PROMPT);
            case "motivational" -> callAgent(query, MOTIVATIONAL_PROMPT);
            default             -> callAgent(query, GENERAL_PROMPT);
        };
        return new AgentResponse(agent, text);
    }

    private String classifyAgent(String query) throws Exception {
        String prompt = """
            Classify this query into exactly one of these agents: decision, career, motivational, general.
            
            - decision: weighing options, should I do X or Y, trade-offs, choices
            - career: resume, job search, interviews, salary, applications, companies
            - motivational: feeling stuck, burnout, self-doubt, encouragement needed
            - general: anything else
            
            Query: "%s"
            
            Respond with only the single word label.
            """.formatted(query);

        return callGemini(prompt, null).strip().toLowerCase();
    }

    private String callAgent(String query, String systemPrompt) throws Exception {
        return callGemini(query, systemPrompt);
    }

    private String callGemini(String query, String systemPrompt) throws Exception {
        var contents = List.of(Map.of("parts", List.of(Map.of("text", query))));

        Map<String, Object> body = systemPrompt != null
                ? Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", contents)
                : Map.of("contents", contents);

        String requestBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body())
                .at("/candidates/0/content/parts/0/text").asText();
    }
}