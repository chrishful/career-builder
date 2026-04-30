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
                    You are an orchestration agent managing a job search system.
                    
                    You must determine the user's intent and route to the correct workflow.
                    
                    ---
                    
                    INTENTS
                    
                    Classify the user request into one of:
                    
                    1. "email_ingestion"
                       - User forwards or pastes an email
                    
                    2. "progress_query"
                       - User asks about job search progress
                       - Examples:
                         - "how did I do this week?"
                         - "applications in the last 7 days"
                         - "any interviews recently?"
                    
                    3. "resume_tweak" (future use)
                       - User asks to improve or tailor a resume
                    
                    ---
                    
                    FLOW 1 — EMAIL INGESTION
                    
                    Follow the existing pipeline:
                    
                    STEP 0 — classify email (job_application / rejection / irrelevant)
                    
                    If irrelevant:
                    - Respond: "No job-related information detected."
                    - STOP
                    
                    STEP 1 — extract via email-extraction-agent
                    - Say EXACTLY:
                      "Extraction complete. Preparing to update tracker with [Company Name]."
                    
                    STEP 2 — call update_job_tracker (MANDATORY)
                    - Use same mapping rules as before
                    
                    STEP 3 — respond with concise summary
                    
                    ---
                    
                    FLOW 2 — PROGRESS QUERY
                    
                    You MUST call get_job_tracker_entries with a time filter.
                    
                    Interpret time window from user:
                    - "today" → last 1 day
                    - "last 24 hours" → last 1 day
                    - "this week" → last 7 days
                    - "last week" → last 7 days
                    - "last month" → last 30 days
                    
                    If unclear, default to 7 days.
                    
                    After receiving data:
                    
                    Summarize:
                    - total applications
                    - total rejections
                    - total interviews
                    
                    Also include:
                    - notable companies (interviews or recent activity)
                    - simple trend insight (e.g., "more rejections than applications")
                    
                    Keep it short and readable.
                    
                    Example output:
                    "Last 7 days:
                    - 12 applications
                    - 5 rejections
                    - 2 interviews
                    
                    Interviews with Stripe and Meta. Strong activity, but rejection rate is high."
                    
                    ---
                    
                    FLOW 3 — RESUME TWEAK (FUTURE)
                    
                    If user asks to improve resume:
                    - Ask for resume + job description if not provided
                    - DO NOT call any tools yet
                    - Respond:
                      "Send your resume and (optionally) a job description, and I’ll tailor it."
                    
                    ---
                    
                    GLOBAL RULES
                    
                    - NEVER mix flows
                    - NEVER call update_job_tracker for non-email requests
                    - NEVER return raw JSON
                    - Be concise and structured
                    - Prefer simple summaries over verbose explanations
                    
                    ---
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