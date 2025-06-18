package com.example.demo;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import com.example.demo.ExcelData;
import com.example.demo.ExcelDataRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "http://13.232.220.117:3001
") // adjust for frontend port
public class ExcelUploadController {

    @Autowired
    private ExcelDataRepository excelDataRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String linkedinId = getCellValue(row.getCell(0));
                String personName = getCellValue(row.getCell(1));
                String mobileNumber = getCellValue(row.getCell(2));
                String mobileNumber2 = getCellValue(row.getCell(3));
                String personLocation = getCellValue(row.getCell(4));
                String linkedinUrl = getCellValue(row.getCell(5));

                if (!excelDataRepository.existsByLinkedinId(linkedinId)) {
                    ExcelData data = new ExcelData();
                    data.setLinkedinId(linkedinId);
                    data.setPersonName(personName);
                    data.setMobileNumber(mobileNumber);
                    data.setMobileNumber2(mobileNumber2);
                    data.setPersonLocation(personLocation);
                    data.setLinkedinUrl(linkedinUrl);

                    excelDataRepository.save(data);
                }
            }

            return ResponseEntity.ok().body("{\"message\":\"Excel uploaded and data saved\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"message\":\"Failed to process Excel file\"}");
        }



    }
    @PostMapping("/search")
    public ResponseEntity<?> searchByLinkedInUrl(@RequestParam("linkedin_url") String linkedinUrl) {
        try {
            String cleanedUrl = linkedinUrl.replaceFirst("^https?://", "").replaceAll("/$", "");
            ExcelData data = excelDataRepository.findByLinkedinUrl(cleanedUrl);
            if (data != null) {
                return ResponseEntity.ok().body(Map.of("data", data));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "LinkedIn URL not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
        }
    }


    @PostMapping("/search/{*url}")
    public ResponseEntity<?> searchByLinkedInUrl(HttpServletRequest request) {
        try {
            // Extract the dynamic LinkedIn URL from request path
            String fullPath = request.getRequestURI(); // e.g., /api/excel/search/linkedin.com/in/abrahamimendez
            String basePath = "/api/excel/search/";
            String linkedinUrl = fullPath.substring(fullPath.indexOf(basePath) + basePath.length());

            // Clean URL
            String cleanedUrl = linkedinUrl.replaceFirst("^https?://", "").replaceAll("/$", "");

            ExcelData data = excelDataRepository.findByLinkedinUrl(cleanedUrl);
            if (data != null) {
                return ResponseEntity.ok(Map.of("success", true, "data", data));
            } else {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "LinkedIn URL not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Internal server error"));
        }
    }









    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
