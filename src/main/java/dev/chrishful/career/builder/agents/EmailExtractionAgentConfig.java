package dev.chrishful.career.builder.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.CallbackContext;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailExtractionAgentConfig {

    @Bean("emailExtractionAgent")
    public static LlmAgent create() {
        return LlmAgent.builder()
                .name("email-extraction-agent")
                .description("Extracts structured fields from forwarded recruitment or rejection emails. Returns companyName, dateProcessed, salaryExpectations, role, remote, and emailType as JSON.")
                .model("gemini-2.5-flash")
                .instruction("""
                    You are a document extraction agent. Your sole job is to extract structured data from forwarded emails and return it as a valid JSON object. You never converse or explain — only return JSON.
        
                    Extract the following fields from the email content provided:
        
                    - companyName: The name of the company the email is from or referencing
                    - dateProcessed: Today's date in ISO 8601 format (YYYY-MM-DD)
                    - salaryExpectations: Any mentioned salary, compensation range, or pay. Null if not mentioned.
                    - role: The job title or position being discussed
                    - remote: true if the role is remote or hybrid, false if on-site only, null if not mentioned
                    - emailType: Either "recruitment" if this is an outreach or job opportunity, or "rejection" if this is a rejection or pass
        
                    Rules:
                    - Always return a single flat JSON object, no markdown, no code blocks, no explanation
                    - If a field cannot be determined from the email, set it to null
                    - Do not infer or guess values that are not present in the email
        
                    Example output:
                    {"companyName":"Acme Corp","dateProcessed":"2026-04-28","salaryExpectations":"$120,000 - $140,000","role":"Senior Software Engineer","remote":true,"emailType":"recruitment"}
                    """)
                .build();
    }

    private static Maybe<LlmResponse> beforeModel(CallbackContext callbackContext, LlmRequest.Builder builder) {
        // Runs before the LLM is called.
        // Return Maybe.empty() to let the request proceed normally.
        // Return Maybe.just(response) to short-circuit and skip the LLM entirely.
        System.out.println("BeforeModel callback triggered for email extraction agent");
        return Maybe.empty();
    }

    private static Maybe<LlmResponse> afterModel(CallbackContext ctx, LlmResponse response) {
        // Runs after the LLM responds.
        // Return Maybe.empty() to pass the response through unchanged.
        // Return Maybe.just(modified) to override the response.
        return Maybe.empty();
    }
}