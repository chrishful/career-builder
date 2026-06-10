package dev.chrishful.career.builder.agents;

import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.adk.tools.AgentTool;
import dev.chrishful.career.builder.tools.JobStatusTool;
import dev.chrishful.career.builder.tools.JobTrackerTool;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DecisionAgentConfig {

        @Bean("decisionAgent")
        public LlmAgent buildDecisionAgent(LlmAgent emailExtractionAgent, JobTrackerTool jobTrackerTool, JobStatusTool jobStatusTool) {
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
                    
                    Triggered by questions like: "how am I doing this week?", "how many applications have I sent?",
                    "any rejections lately?", "are there open apps going quiet?"
                    
                    Route to the appropriate tool based on intent:
                    
                    | Question type                        | Tool                       | Default lookbackDays |
                    |--------------------------------------|----------------------------|----------------------|
                    | Overall summary / "how am I doing"   | get_job_hunt_summary        | 7                    |
                    | Count of applications sent           | get_application             | 30                   |
                    | Rejection count or rejection rate    | get_rejection_count         | 30                   |
                    | Stale / quiet / no-response apps     | get_stalled_applications    | 30 (staleDays param) |
                    
                    Infer the lookback window from the user's phrasing:
                    - "today" / "last 24 hours" → 1
                    - "this week"               → 7
                    - "this month" / "lately"   → 30
                    - "all time" / no qualifier on summary questions → 0
                    - Stalled app questions with no qualifier → default staleDaysThreshold: 30
                    
                    You may call multiple tools in one turn if the question spans topics
                    (e.g. "how many apps and how many rejections this month?" → call both).
                    
                    Present results as a clean, conversational summary. Do not print raw JSON.
                    Highlight any companies in "Interview" status. Flag stalled apps by name.
                    ---
                    GLOBAL RULES
                    - NEVER call database update operations for pure progress metric queries.
                    - Always normalize status strings to uppercase first letters ("Applied", "Interview", "Rejected") to respect database constraints.
                    - Keep conversational output concise, readable, and structured.
                    """)
                    .tools(
                            jobTrackerTool.asTool(),
                            jobStatusTool.summaryTool(),
                            jobStatusTool.applicationsTool(),
                            jobStatusTool.rejectionCountTool(),
                            jobStatusTool.stalledApplicationsTool()
                    )
                    .subAgents(emailExtractionAgent)
                    .build();
        }
}