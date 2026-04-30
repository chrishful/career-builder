package dev.chrishful.career.builder.filters;

import com.google.api.client.util.Value;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class WebhookAuthFilter extends OncePerRequestFilter {

    private final String secret;

    public WebhookAuthFilter(String webhookSecret) {
         this.secret = webhookSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
            String header = request.getHeader("X-Webhook-Secret");

            if (!secret.equals(header)) {
                System.out.println("Unauthorized webhook request: missing or invalid secret");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        filterChain.doFilter(request, response);
    }
}