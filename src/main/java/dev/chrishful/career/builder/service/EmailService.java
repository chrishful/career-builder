package dev.chrishful.career.builder.service;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import dev.chrishful.career.builder.dto.EmailRequest;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final InMemoryRunner runner;

    // Inject the LlmAgent bean you already defined in DecisionAgentConfig
    public EmailService(LlmAgent decisionAgent) {
        this.runner = new InMemoryRunner(decisionAgent);
    }

    public String forward(EmailRequest email) {
        String userId = "email-forwarder";

        // Create a fresh session per email (or persist one if you want conversation history)
        Session session = runner.sessionService()
                .createSession(runner.appName(), userId)
                .blockingGet();

        String formatted = """
                Forwarded Email: %s
                ---------------
                """.formatted(email.getPlain());

        Content userMsg = Content.fromParts(Part.fromText(formatted));

        Flowable<Event> events = runner.runAsync(userId, session.id(), userMsg);

        // Collect the final text response from the agent's events
        StringBuilder response = new StringBuilder();
        events.blockingForEach(event -> {
            if (event.finalResponse()) {
                event.content()
                        .flatMap(c -> c.parts().isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(c.parts()))
                        .ifPresent(parts -> parts.stream()
                                .flatMap(p -> p.getFirst().text().stream())
                                .forEach(response::append));
            }
        });
        System.out.println(response);
        return response.toString();
    }


}
