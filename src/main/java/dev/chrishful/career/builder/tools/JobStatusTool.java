package dev.chrishful.career.builder.tools;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import dev.chrishful.career.builder.dto.JobApplicationDto;
import dev.chrishful.career.builder.service.DatabaseService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JobStatusTool {

    private final DatabaseService databaseService;
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public JobStatusTool(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    // ── 1. Overall summary ────────────────────────────────────────────────────

    @Schema(description = """
            Returns an overall job hunt summary for the given lookback window.
            Includes: total applications, counts by status (Applied, Interview, Rejected, Withdrawn),
            response rate (anything beyond Applied / total), and active pipeline count.
            Use for questions like "how am I doing this week?" or "give me a job hunt update".
            """)
    public Map<String, Object> getJobHuntSummary(
            @Schema(description = "Lookback window in days (e.g. 7 = this week, 30 = this month, 0 = all time)") int lookbackDays
    ) {
        List<JobApplicationDto> apps = getFiltered(lookbackDays);

        Map<String, Long> byStatus = apps.stream()
                .collect(Collectors.groupingBy(
                        a -> a.status() != null ? a.status() : "Unknown",
                        Collectors.counting()
                ));

        long total = apps.size();
        long applied = byStatus.getOrDefault("Applied", 0L);
        long progressed = total - applied; // anything past initial Applied
        double responseRate = total > 0 ? (double) progressed / total * 100 : 0;

        long active = apps.stream()
                .filter(a -> !"Rejected".equalsIgnoreCase(a.status())
                        && !"Withdrawn".equalsIgnoreCase(a.status()))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lookbackDays", lookbackDays == 0 ? "all time" : lookbackDays + " days");
        result.put("totalApplications", total);
        result.put("byStatus", byStatus);
        result.put("activePipeline", active);
        result.put("responseRatePct", String.format("%.1f%%", responseRate));
        return result;
    }

    // ── 2. Application count ──────────────────────────────────────────────────

    @Schema(description = """
            Returns the number of job applications submitted within the lookback window.
            Use for questions like "how many applications have I sent this month?"
            """)
    public Map<String, Object> getApplicationCount(
            @Schema(description = "Lookback window in days (7 = this week, 30 = this month, 0 = all time)") int lookbackDays
    ) {
        List<JobApplicationDto> apps = getFiltered(lookbackDays);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lookbackDays", lookbackDays == 0 ? "all time" : lookbackDays + " days");
        result.put("applicationCount", apps.size());
        return result;
    }

    // ── 3. Rejection count ────────────────────────────────────────────────────

    @Schema(description = """
            Returns rejection count and rejection rate for the lookback window.
            Use for questions like "how many rejections have I received?" or "what's my rejection rate?"
            """)
    public Map<String, Object> getRejectionCount(
            @Schema(description = "Lookback window in days (7 = this week, 30 = this month, 0 = all time)") int lookbackDays
    ) {
        List<JobApplicationDto> apps = getFiltered(lookbackDays);
        long total = apps.size();
        long rejections = apps.stream()
                .filter(a -> "Rejected".equalsIgnoreCase(a.status()))
                .count();

        double rate = total > 0 ? (double) rejections / total * 100 : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lookbackDays", lookbackDays == 0 ? "all time" : lookbackDays + " days");
        result.put("rejections", rejections);
        result.put("totalInWindow", total);
        result.put("rejectionRatePct", String.format("%.1f%%", rate));
        return result;
    }

    // ── 4. Stalled applications ───────────────────────────────────────────────

    @Schema(description = """
            Returns open applications (not rejected, not withdrawn) that have had no update
            for more than staleDaysThreshold days.
            Use for questions like "are there any open applications from over a month ago?"
            or "which applications have gone quiet?"
            """)
    public List<Map<String, String>> getStalledApplications(
            @Schema(description = "Flag applications with no update older than this many days (e.g. 30)") int staleDaysThreshold
    ) {
        LocalDate cutoff = LocalDate.now().minusDays(staleDaysThreshold);

        return databaseService.getAllApplications().stream()
                .filter(a -> !"Rejected".equalsIgnoreCase(a.status())
                        && !"Withdrawn".equalsIgnoreCase(a.status()))
                .filter(a -> parseLastUpdated(a.lastUpdated()).isBefore(cutoff))
                .map(a -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("company", a.company());
                    entry.put("role", a.role());
                    entry.put("status", a.status());
                    entry.put("lastUpdated", a.lastUpdated());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    // ── asTool() wiring ───────────────────────────────────────────────────────

    public FunctionTool summaryTool() {
        return FunctionTool.create(this, "getJobHuntSummary");
    }

    public FunctionTool applicationCountTool() {
        return FunctionTool.create(this, "getApplicationCount");
    }

    public FunctionTool rejectionCountTool() {
        return FunctionTool.create(this, "getRejectionCount");
    }

    public FunctionTool stalledApplicationsTool() {
        return FunctionTool.create(this, "getStalledApplications");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<JobApplicationDto> getFiltered(int lookbackDays) {
        List<JobApplicationDto> all = databaseService.getAllApplications();
        if (lookbackDays == 0) return all;

        LocalDate cutoff = LocalDate.now().minusDays(lookbackDays);
        return all.stream()
                .filter(a -> parseLastUpdated(a.lastUpdated()).isAfter(cutoff))
                .collect(Collectors.toList());
    }

    /**
     * Parses lastUpdated from DB format "YYYY-MM-DD HH24:MI:SS".
     * Falls back to epoch-start so the record still shows up as stalled rather than throwing.
     */
    private LocalDate parseLastUpdated(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.EPOCH;
        try {
            return LocalDate.parse(raw, DB_FMT);
        } catch (DateTimeParseException e) {
            return LocalDate.EPOCH;
        }
    }
}