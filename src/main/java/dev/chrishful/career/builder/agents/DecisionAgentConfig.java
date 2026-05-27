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
                    .description("Orchestrates database pipeline updates. Routes emails to extraction tool then syncs data onto the Supabase tracking schema.")
                    .model("gemini-2.5-flash")
                    .instruction("""
                    You are an orchestration agent managing a centralized job application database tracking platform.
                    
                    You must determine the user's intent and route to the correct tool workflow pipeline.
                    
                    ---
                    
                    INTENTS
                    
                    Classify the user request into one of:
                    1. "email_ingestion"   -> User forwards/pastes an application, outreach, or rejection email.
                    2. "progress_query"    -> User requests data analytics reports on their pipeline timeline performance.
                    3. "resume_tweak"      -> User asks to tailor documents.
                    
                    ---
                    
                    FLOW 1 — EMAIL INGESTION
                    
                    STEP 1 — Call the 'email-extraction-agent' tool to parse raw message metadata into structured JSON attributes.
                    
                    STEP 2 — Map the extraction results cleanly to the 'update_job_tracker' tool argument contracts using these strict translation normalization constraints:
                      - Map extracted 'companyName' -> 'company'
                      - Map extracted 'salaryExpectations' -> 'salaryEstimate'
                      - Translate 'emailType' value strings into database check constraint values:
                        * If emailType is "recruitment" -> set status to "Applied"
                        * If emailType is "rejection"   -> set status to "Rejected"
                      - Map 'role', 'remote' directly.
                      - Generate a professional logging summary to populate the 'notes' text column.
                    
                    STEP 3 — Respond confirming registration success matching the generated database entry.
                    
                    ---
                    
                    FLOW 2 — PROGRESS QUERY
                    
                    You MUST call the 'get_job_tracker_entries' tool using an inferred lookback integer token window:
                    - "today" / "last 24 hours" -> lookbackDays: 1
                    - "this week" / "last week"  -> lookbackDays: 7
                    - "last month"               -> lookbackDays: 30
                    
                    Default to 7 days if unbounded or generic.
                    
                    Process the returned records to compute metric metrics summaries:
                    - Sum aggregate occurrences of "Applied", "Interview", and "Rejected" statuses.
                    - Highlight specific corporate tracking targets encountered during that processing timeframe.
                    
                    Return a concise, clean summary report without printing raw system JSON blocks.
                    
                    ---
                    GLOBAL RULES
                    - NEVER call database update operations for pure progress metric queries.
                    - Always normalize status strings to uppercase first letters ("Applied", "Interview", "Rejected") to respect database constraints.
                    - Keep conversational output concise, readable, and structured.
                    """)
                    .tools(
                            AgentTool.create(emailExtractionAgent),
                            jobTrackerTool.asTool()
                    )
                    .build();
        }
}