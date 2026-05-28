package dev.chrishful.career.builder.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class WebhookAuthFilter extends OncePerRequestFilter {

    @Value("${career.builder.webhook.username}")
    private String expectedUser;

    @Value("${career.builder.webhook.token}")
    private String expectedToken;

    /**
     * This method intercepts requests BEFORE they hit doFilterInternal.
     * Returning 'true' completely skips the authentication logic for that route.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Skip anything matching your frontend dashboard data endpoints
        return path.startsWith("/v1/applications");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Allow pre-flight requests to pass through the filter immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            rejectRequest(response, "Missing or invalid authorization scheme.");
            return;
        }

        try {
            String base64Credentials = authHeader.substring(6).trim();
            byte[] decoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decoded, StandardCharsets.UTF_8);

            String[] values = credentials.split(":", 2);

            if (values.length != 2 || !values[0].equals(expectedUser) || !values[1].equals(expectedToken)) {
                rejectRequest(response, "Invalid webhook credentials.");
                return;
            }
        } catch (Exception e) {
            rejectRequest(response, "Malformed authentication block.");
            return;
        }

        // Credentials are valid, let the request through to your webhook controller
        filterChain.doFilter(request, response);
    }

    private void rejectRequest(HttpServletResponse response, String msg) throws IOException {
        System.err.println("Unauthorized webhook trigger blocked: " + msg);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("text/plain");
        response.getWriter().write(msg);
    }
}