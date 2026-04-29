package dev.chrishful.career.builder.agents;

import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.adk.tools.AgentTool;
import dev.chrishful.career.builder.tools.JobTrackerTool;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DecisionAgentConfig {

    @Bean("decisionAgent")
    public LlmAgent buildDecisionAgent(LlmAgent emailExtractionAgent, JobTrackerTool jobTrackerTool) {
        return LlmAgent.builder()
                .name("decision-agent")
                .description("Orchestrates email processing. Routes forwarded emails to the extraction agent then takes action based on whether it is a recruitment or rejection email.")
                .model("gemini-2.5-flash")
                .instruction("""
                    You are a helpful assistant. Reply 'Ready' and nothing else.
                    """)
                .tools(

                        jobTrackerTool.asTool()                  // your Excel tool
                )
                .build();
    }
    private static Maybe<LlmResponse> beforeModel(CallbackContext callbackContext, LlmRequest.Builder builder) {
        // Runs before the LLM is called.
        // Return Maybe.empty() to let the request proceed normally.
        // Return Maybe.just(response) to short-circuit and skip the LLM entirely.
        System.out.println("BeforeModel callback triggered for decision agent");
        return Maybe.empty();
    }

    private static Maybe<LlmResponse> afterModel(CallbackContext ctx, LlmResponse response) {
        // Runs after the LLM responds.
        // Return Maybe.empty() to pass the response through unchanged.
        // Return Maybe.just(modified) to override the response.
        return Maybe.empty();
    }
}