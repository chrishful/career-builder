package dev.chrishful.career.builder.tools;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import dev.chrishful.career.builder.dto.JobApplicationDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Component
public class JobTrackerTool {

    private static final int COL_NUM        = 0;
    private static final int COL_COMPANY    = 1;
    private static final int COL_ROLE       = 2;
    private static final int COL_DATE_APP   = 3;
    private static final int COL_STATUS     = 4;
    private static final int COL_INTERESTED = 5;
    private static final int COL_SALARY     = 6;
    private static final int COL_REMOTE     = 7;
    private static final int COL_LAST_UPD   = 8;
    private static final int COL_NOTES      = 9;
    private static final int DATA_START_ROW = 3;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Value("${job.hunt.spreadsheet.location}")
    private String spreadsheetPath;

    // ADK picks this up via FunctionTool.create() — @Schema drives the LLM's understanding
    @Schema(description = """
            Adds a new job application or updates an existing one in the Excel job tracker.
            Call this whenever the user mentions applying somewhere, a rejection, an interview,
            withdrawing, or any status change. Updates the row in place if company exists, appends if new.
            """)
    public Map<String, String> updateJobTracker(
            @Schema(description = "Company name") String company,
            @Schema(description = "Job title / role") String role,
            @Schema(description = "Status: Applied, Rejected, Phone Screen, First Interview, Withdrawn") String status,
            @Schema(description = "Interest level: YES, No, Maybe. Null if unknown.") String interested,
            @Schema(description = "Salary estimate e.g. '$130k-160k'. Null if unknown.") String salaryEst,
            @Schema(description = "Remote: Yes, No, or null if unknown.") String remote,
            @Schema(description = "Date applied MM/dd/yyyy. Defaults to today if null.") String dateApplied,
            @Schema(description = "Any notes. Null if none.") String notes
    ) {
        System.out.println("updateJobTracker called for: " + company);

        try (FileInputStream fis = new FileInputStream(spreadsheetPath);

             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            Date today = Date.from(Instant.from(Instant.now()));

            Row targetRow = findRowByCompany(sheet, company);
            boolean isNew = (targetRow == null);

            if (isNew) {
                int nextRowIdx = findNextEmptyRow(sheet);
                targetRow = sheet.createRow(nextRowIdx);
                int rowNum = nextRowIdx - DATA_START_ROW + 1;
                setCell(targetRow, COL_NUM, rowNum);
                setCell(targetRow, COL_DATE_APP, today);
            }

            setCell(targetRow, COL_COMPANY, company);
            setCell(targetRow, COL_ROLE, role != null ? role : "");
            setCell(targetRow, COL_STATUS, status != null ? status : "Applied");
            setCell(targetRow, COL_INTERESTED, interested != null ? interested : "");
            setCell(targetRow, COL_SALARY, salaryEst != null ? salaryEst : "");
            setCell(targetRow, COL_REMOTE, remote != null ? remote : "");
            setCell(targetRow, COL_LAST_UPD, today);
            setCell(targetRow, COL_NOTES, notes != null ? notes : "");

            try (FileOutputStream fos = new FileOutputStream(spreadsheetPath)) {
                wb.write(fos);
            }

            String message = isNew
                    ? "Added new row for %s (%s) with status: %s".formatted(company, role, status)
                    : "Updated existing row for %s — status is now: %s".formatted(company, status);

            return Map.of("status", "success", "message", message);

        } catch (IOException e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    public void updateJobTracker(JobApplicationDto dto) {
        updateJobTracker(
                dto.company(),
                dto.role(),
                dto.status(),
                dto.interested(),
                dto.salaryEstimate(),
                dto.remote() ? "Yes" : "No",
                dto.dateApplied() != null ? dto.dateApplied().format(DATE_FMT) : null,
                dto.notes()
        );
    }

    // Call this to get a FunctionTool the agent can use
    public FunctionTool asTool() {
        return FunctionTool.create(this, "updateJobTracker");
    }

    private Row findRowByCompany(Sheet sheet, String company) {
        for (int i = DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Cell cell = row.getCell(COL_COMPANY);
            if (cell != null
                    && cell.getCellType() == CellType.STRING
                    && company.equalsIgnoreCase(cell.getStringCellValue().trim())) {
                return row;
            }
        }
        return null;
    }

    private int findNextEmptyRow(Sheet sheet) {
        int last = sheet.getLastRowNum();
        for (int i = DATA_START_ROW; i <= last; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) return i;
        }
        return last + 1;
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            switch (cell.getCellType()) {
                case BLANK -> {}
                case STRING -> { if (!cell.getStringCellValue().isBlank()) return false; }
                default -> { return false; } // NUMERIC, BOOLEAN, FORMULA = not empty
            }
        }
        return true;
    }

    private void setCell(Row row, int colIdx, String value) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(value != null ? value : "");
    }

    private void setCell(Row row, int colIdx, int value) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(value);
    }

    private void setCell(Row row, int colIdx, Date value) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) cell = row.createCell(colIdx);
        cell.setCellValue(value);
    }
}