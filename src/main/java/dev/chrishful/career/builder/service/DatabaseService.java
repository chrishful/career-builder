package dev.chrishful.career.builder.service;

import java.util.List;
import java.util.UUID;

import dev.chrishful.career.builder.dto.JobApplicationDto;
import dev.chrishful.career.builder.dto.JobApplicationResponseDto;
import dev.chrishful.career.builder.dto.UpdateApplicationEntryDto;
import org.springframework.stereotype.Service;

import org.springframework.jdbc.core.simple.JdbcClient;

@Service
public class DatabaseService {

    private final JdbcClient jdbcClient;

    public DatabaseService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<JobApplicationDto> getAllApplications() {
        String sql = """
            SELECT 
                id,
                number,
                company,
                role,
                to_char(date_applied, 'Mon DD') as dateApplied,
                status,
                interested,
                salary_estimate as salaryEstimate,
                remote,
                to_char(last_updated, 'YYYY-MM-DD HH24:MI:SS') as lastUpdated,
                notes
            FROM applications
            ORDER BY number ASC
            """;



        List<JobApplicationDto> results = jdbcClient.sql(sql)
                .query(JobApplicationDto.class)
                .list();

        System.out.println("Query returned " + results.size() + " applications");

        return results;
    }

    public JobApplicationResponseDto insertApplication(JobApplicationDto dto) {
        String sql = """
        INSERT INTO applications (company, role, status, interested, salary_estimate, remote, notes)
        VALUES (:company, :role, :status, :interested, :salaryEstimate, :remote, :notes)
        RETURNING number, to_char(last_updated, 'YYYY-MM-DD HH24:MI:SS') as lastUpdated
        """;

        return jdbcClient.sql(sql)
                .param("company", dto.company())
                .param("role", dto.role())
                .param("status", dto.status() != null ? dto.status() : "Applied")
                .param("interested", dto.interested() != null ? dto.interested() : "Maybe")
                .param("salaryEstimate", dto.salaryEstimate())
                .param("remote", dto.remote())
                .param("notes", dto.notes())
                .query((rs, rowNum) -> new JobApplicationResponseDto(
                        true,
                        "Application for " + dto.company() + " successfully tracked.",
                        rs.getInt("number"),
                        rs.getString("lastUpdated")
                ))
                .single();
    }

    public boolean deleteApplication(UUID id) {
        String sql = """
        DELETE FROM applications 
        WHERE id = :id::uuid
        """;

        int rowsAffected = jdbcClient.sql(sql)
                .param("id", id) // This key must perfectly match the token above!
                .update();

        return rowsAffected > 0;
    }

    public boolean updateApplication(UUID id, UpdateApplicationEntryDto update) {
        if (update != null && update.status() != null && !update.status().isEmpty()){
            return updateApplicationStatus(id, update.status());
        }
        if (update != null && update.notes()!= null && !update.notes().isEmpty()){
            return updateApplicationNotes(id, update.notes());
        }
        return false;
    }

    public boolean updateApplicationStatus(UUID id, String newStatus) {
        String sql = """
        UPDATE applications
        SET status = :status,
            last_updated = CURRENT_TIMESTAMP
        WHERE id = :id::uuid
        """;

        int rowsAffected = jdbcClient.sql(sql)
                .param("status", newStatus)
                .param("id", id)
                .update();

        return rowsAffected > 0;
    }

    public boolean updateApplicationNotes(UUID id, String newNotes) {
        String sql = """
            UPDATE applications
            SET notes = :notes,
                last_updated = CURRENT_TIMESTAMP
            WHERE id = :id::uuid
        """;

        int rowsAffected = jdbcClient.sql(sql)
                .param("notes", newNotes)
                .param("id", id)
                .update();

        return rowsAffected > 0;
    }

    public String upsertApplicationByCompanyAndRole(JobApplicationDto dto) {
        String sql = """
        INSERT INTO applications (company, role, status, interested, salary_estimate, remote, notes)
        VALUES (:company, :role, :status, :interested, :salaryEstimate, :remote, :notes)
        ON CONFLICT (company, role)
        DO UPDATE SET 
            status = EXCLUDED.status,
            interested = COALESCE(NULLIF(EXCLUDED.interested, ''), applications.interested),
            salary_estimate = COALESCE(NULLIF(EXCLUDED.salary_estimate, ''), applications.salary_estimate),
            remote = EXCLUDED.remote,
            notes = COALESCE(applications.notes || ' | ' || EXCLUDED.notes, EXCLUDED.notes),
            last_updated = CURRENT_TIMESTAMP
        RETURNING company, role
        """;

        return jdbcClient.sql(sql)
                .param("company", dto.company())
                .param("role", dto.role())
                .param("status", dto.status())
                .param("interested", dto.interested())
                .param("salaryEstimate", dto.salaryEstimate())
                .param("remote", dto.remote())
                .param("notes", dto.notes())
                .query((rs, rowNum) -> {
                    String company = rs.getString("company");
                    String role = rs.getString("role");
                    return "Successfully processed tracking sync for: " + company + " - " + role;
                })
                .single();
    }
    public String fetchRoleByCompany(String company) {
        return jdbcClient.sql("SELECT role FROM applications WHERE company ilike :company ORDER BY last_updated DESC LIMIT 1")
                .param("company", company)
                .query(String.class)
                .optional()
                .orElse(null);
    }
}