package dev.chrishful.career.builder.service;

import dev.chrishful.career.builder.dto.JobApplicationDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    @Value("${job.hunt.spreadsheet.location}")
    private String spreadsheetLocation;

    public List<JobApplicationDto> readApplications() throws Exception {
        List<JobApplicationDto> applications = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(spreadsheetLocation);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // rows 0-2 are header/title rows, data starts at row 3
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                if (firstCell == null || firstCell.getCellType() == CellType.BLANK) continue;

                applications.add(new JobApplicationDto(
                        (int) row.getCell(0).getNumericCellValue(),       // #
                        getString(row.getCell(1)),                         // Company
                        getString(row.getCell(2)),                         // Role
                        getDate(row.getCell(3)),                           // Date Applied
                        getString(row.getCell(4)),                         // Status
                        getString(row.getCell(5)),                         // Interested?
                        getString(row.getCell(6)),                         // Salary Est.
                        "Yes".equalsIgnoreCase(getString(row.getCell(7))), // Remote?
                        getDate(row.getCell(8)),                           // Last Updated
                        getString(row.getCell(9))                          // Notes
                ));
            }
        }

        return applications;
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private LocalDate getDate(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }
}