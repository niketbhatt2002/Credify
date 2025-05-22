package com.example.demo.util;

import com.example.demo.model.CreditCard;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for reading credit card data from Excel files.
 * Uses Apache POI library to parse Excel (.xlsx) files and convert rows
 * into CreditCard objects.
 */
public class ExcelReader {

    // =========================================
    // Public API Method
    // =========================================

    /**
     * Reads credit card data from an Excel file and converts it to a list of CreditCard objects.
     * 
     * @param filePath Path to the Excel file
     * @return List of CreditCard objects parsed from the file
     * @throws RuntimeException if there's an error reading the file
     */
    public static List<CreditCard> readCreditCardsFromExcel(String filePath) {
        List<CreditCard> creditCards = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(new File(filePath))) {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            // Process each data row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                creditCards.add(createCreditCardFromRow(row));
            }

            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }

        return creditCards;
    }

    // =========================================
    // Private Helper Methods
    // =========================================

    /**
     * Creates a CreditCard object from an Excel row.
     * 
     * @param row The Excel row containing credit card data
     * @return CreditCard object populated with row data
     */
    private static CreditCard createCreditCardFromRow(Row row) {
        return new CreditCard(
                getCellValue(row.getCell(0)), // Card Title
                getCellValue(row.getCell(1)), // Card Images
                getCellValue(row.getCell(2)), // Annual Fees
                getCellValue(row.getCell(3)), // Purchase Interest Rate
                getCellValue(row.getCell(4)), // Cash Interest Rate
                getCellValue(row.getCell(5)), // Product Value Prop
                getCellValue(row.getCell(6)), // Product Benefits
                getCellValue(row.getCell(7)),  // Bank Name
                getCellValue(row.getCell(8))
        );
    }

    /**
     * Extracts string value from an Excel cell, handling different cell types.
     * 
     * @param cell The Excel cell to read
     * @return String representation of the cell's value
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getFormulaCellValue(cell);
            default:
                return "";
        }
    }

    /**
     * Handles formula cells by evaluating and returning their value.
     * 
     * @param cell The formula cell to evaluate
     * @return String representation of the formula result
     */
    private static String getFormulaCellValue(Cell cell) {
        try {
            return String.valueOf(cell.getNumericCellValue());
        } catch (IllegalStateException e) {
            return cell.getStringCellValue().trim();
        }
    }
}