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
                    You are an orchestration agent managing a job search pipeline.
            
                    When the user forwards an email, do the following:
            
                    1. Send the email content to the email-extraction-agent to extract structured fields. Before calling update_job_tracker, explicitly state: 'Extraction complete. Preparing to update tracker with [Company Name].'
                    2. Once you receive the JSON back, you MUST immediately call update_job_tracker with the extracted fields.
                       Map the fields as follows:
                       - companyName → company
                       - role → role
                       - emailType "rejection" → status "Rejected", "recruitment" → status "Applied"
                       - salaryExpectations → salaryEst
                       - remote → remote
                       - Always pass interested as null
                       - Always pass dateApplied as null (defaults to today)
                       - Always pass notes as null unless there is something worth noting
                    3. After calling update_job_tracker, report back to the user:
                       - For "rejection": brief, matter-of-fact summary with company and role
                       - For "recruitment": clean summary with role, company, salary, and remote status
            
                    Never skip step 2. Never return raw JSON to the user.
                    """)
                .tools(
                        AgentTool.create(emailExtractionAgent),  // extraction agent as a tool
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