package com.example.tdg.controller;

import com.example.tdg.model.dto.TemplateDto;
import com.example.tdg.service.pdf.PDFAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST API controller for PDF analysis.
 */
@RestController
@RequestMapping("/api/pdf")
public class PDFAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFAnalysisController.class);
    
    @Autowired
    private PDFAnalysisService pdfAnalysisService;
    
    /**
     * Analyze a redline PDF and extract variables.
     * 
     * @param file The PDF file
     * @return Template with extracted variables
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzePDF(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a PDF file");
        }
        
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only PDF files are supported");
        }
        
        try {
            logger.info("Received PDF for analysis: {}", file.getOriginalFilename());
            TemplateDto template = pdfAnalysisService.analyzePDF(file);
            return ResponseEntity.ok(template);
        } catch (IOException e) {
            logger.error("Error analyzing PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error analyzing PDF: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during PDF analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
}
