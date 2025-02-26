package com.example.tdg.service.pdf;

import com.example.tdg.model.dto.ColumnDefinitionDto;
import com.example.tdg.model.dto.TemplateDto;
import com.example.tdg.model.entity.Template;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for analyzing redline PDFs and extracting variables.
 */
@Service
public class PDFAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFAnalysisService.class);
    
    // Regular expressions for identifying variables in text
    private static final Pattern VAR_PATTERN = Pattern.compile("\\[\\[(.*?)\\]\\]|\\{\\{(.*?)\\}\\}|<(.*?)>|__(.*?)__");
    
    // Variable type mapping based on common naming patterns
    private static final Map<String, String> TYPE_PATTERNS = new HashMap<>();
    
    static {
        // Initialize common variable name patterns and their corresponding types
        TYPE_PATTERNS.put("(?i).*name.*", "firstName");
        TYPE_PATTERNS.put("(?i).*address.*", "streetAddress");
        TYPE_PATTERNS.put("(?i).*city.*", "city");
        TYPE_PATTERNS.put("(?i).*state.*", "state");
        TYPE_PATTERNS.put("(?i).*zip.*|.*postal.*", "zipCode");
        TYPE_PATTERNS.put("(?i).*email.*", "email");
        TYPE_PATTERNS.put("(?i).*phone.*", "phoneNumber");
        TYPE_PATTERNS.put("(?i).*date.*|.*dob.*", "date");
        TYPE_PATTERNS.put("(?i).*amount.*|.*balance.*|.*payment.*", "currency");
        TYPE_PATTERNS.put("(?i).*account.*|.*loan.*", "accountNumber");
        TYPE_PATTERNS.put("(?i).*ssn.*|.*social.*security.*", "ssn");
        TYPE_PATTERNS.put("(?i).*id.*number.*", "idNumber");
    }
    
    /**
     * Analyze a redline PDF and extract variables.
     * 
     * @param file The PDF file
     * @return TemplateDto with extracted variables as column definitions
     * @throws IOException If PDF processing fails
     */
    public TemplateDto analyzePDF(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is empty");
        }
        
        logger.info("Analyzing PDF: {}", file.getOriginalFilename());
        
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            // Extract variables from PDF
            Set<String> variables = extractVariablesFromPDF(document);
            
            // Create a template with the extracted variables
            TemplateDto template = new TemplateDto();
            template.setName(getTemplateName(file.getOriginalFilename()));
            template.setDescription("Generated from PDF: " + file.getOriginalFilename());
            template.setDefaultOutputFormat(Template.OutputFormat.CSV);
            template.setDefaultRowCount(100);
            
            // Convert variables to column definitions
            List<ColumnDefinitionDto> columns = new ArrayList<>();
            int sequence = 1;
            
            for (String variable : variables) {
                ColumnDefinitionDto column = createColumnDefinition(variable, sequence++);
                columns.add(column);
            }
            
            template.setColumnDefinitions(columns);
            
            logger.info("Extracted {} variables from PDF", variables.size());
            
            return template;
        }
    }
    
    /**
     * Extract variables from a PDF document.
     * 
     * @param document The PDDocument
     * @return Set of variable names
     * @throws IOException If text extraction fails
     */
    private Set<String> extractVariablesFromPDF(PDDocument document) throws IOException {
        Set<String> variables = new HashSet<>();
        
        // Extract from annotations (underlined text)
        variables.addAll(extractVariablesFromAnnotations(document));
        
        // Extract from text (looking for patterns like [[variableName]])
        variables.addAll(extractVariablesFromText(document));
        
        return variables;
    }
    
    /**
     * Extract variables from PDF annotations (e.g., underlines).
     * 
     * @param document The PDDocument
     * @return Set of variable names
     * @throws IOException If annotation processing fails
     */
    private Set<String> extractVariablesFromAnnotations(PDDocument document) throws IOException {
        Set<String> variables = new HashSet<>();
        
        // Process each page
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            PDPage page = document.getPage(i);
            
            // Get annotations
            List<PDAnnotation> annotations = page.getAnnotations();
            
            for (PDAnnotation annotation : annotations) {
                // Check if annotation is underline or highlight (common for redlines)
                if ("Underline".equals(annotation.getSubtype()) || 
                        "Highlight".equals(annotation.getSubtype())) {
                    
                    // Get annotation rectangle
                    PDRectangle rect = annotation.getRectangle();
                    Rectangle rectangle = new Rectangle(
                            (int) rect.getLowerLeftX(),
                            (int) rect.getLowerLeftY(),
                            (int) rect.getWidth(),
                            (int) rect.getHeight());
                    
                    // Extract text from the annotation area
                    PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                    stripper.addRegion("annotation", rectangle);
                    stripper.extractRegions(page);
                    
                    String text = stripper.getTextForRegion("annotation");
                    
                    // Check if text contains variable patterns
                    Matcher matcher = VAR_PATTERN.matcher(text);
                    while (matcher.find()) {
                        // Extract variable name from any of the capture groups
                        for (int g = 1; g <= matcher.groupCount(); g++) {
                            if (matcher.group(g) != null) {
                                variables.add(cleanVariableName(matcher.group(g)));
                                break;
                            }
                        }
                    }
                    
                    // If no pattern match, use the whole underlined text as a potential variable
                    if (!matcher.find()) {
                        String trimmed = text.trim();
                        if (!trimmed.isEmpty() && trimmed.length() < 50) {
                            variables.add(cleanVariableName(trimmed));
                        }
                    }
                }
            }
        }
        
        return variables;
    }
    
    /**
     * Extract variables from PDF text using pattern matching.
     * 
     * @param document The PDDocument
     * @return Set of variable names
     * @throws IOException If text extraction fails
     */
    private Set<String> extractVariablesFromText(PDDocument document) throws IOException {
        Set<String> variables = new HashSet<>();
        
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        
        // Find variable patterns like [[variableName]] or {{variableName}}
        Matcher matcher = VAR_PATTERN.matcher(text);
        
        while (matcher.find()) {
            // Extract variable name from any of the capture groups
            for (int g = 1; g <= matcher.groupCount(); g++) {
                if (matcher.group(g) != null) {
                    variables.add(cleanVariableName(matcher.group(g)));
                    break;
                }
            }
        }
        
        return variables;
    }
    
    /**
     * Clean variable name by removing special characters and standardizing format.
     * 
     * @param name The raw variable name
     * @return Cleaned variable name
     */
    private String cleanVariableName(String name) {
        return name.trim()
                .replaceAll("[\\[\\]{}()<>]", "")
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "");
    }
    
    /**
     * Generate template name from PDF filename.
     * 
     * @param filename The PDF filename
     * @return Template name
     */
    private String getTemplateName(String filename) {
        if (filename == null) {
            return "Template_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        // Remove extension and clean up
        String name = filename.replaceAll("\\.pdf$", "")
                .replaceAll("[^a-zA-Z0-9_\\s]", "")
                .trim();
        
        // If empty after cleanup, generate a random name
        if (name.isEmpty()) {
            return "Template_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        return name + "_Template";
    }
    
    /**
     * Create a column definition from a variable name.
     * 
     * @param variableName The variable name
     * @param sequence The sequence number
     * @return ColumnDefinitionDto
     */
    private ColumnDefinitionDto createColumnDefinition(String variableName, int sequence) {
        ColumnDefinitionDto column = new ColumnDefinitionDto();
        column.setName(variableName);
        column.setSequenceNumber(sequence);
        column.setIsNullable(false);
        column.setNullProbability(0.0);
        
        // Determine best data type based on variable name
        String dataType = inferDataType(variableName);
        column.setType(dataType);
        
        // Add type-specific constraints
        addTypeConstraints(column, dataType);
        
        return column;
    }
    
    /**
     * Infer data type from variable name using pattern matching.
     * 
     * @param variableName The variable name
     * @return Inferred data type
     */
    private String inferDataType(String variableName) {
        for (Map.Entry<String, String> entry : TYPE_PATTERNS.entrySet()) {
            if (variableName.matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Default to string if no pattern matches
        return "string";
    }
    
    /**
     * Add type-specific constraints based on data type.
     * 
     * @param column The column definition
     * @param dataType The data type
     */
    private void addTypeConstraints(ColumnDefinitionDto column, String dataType) {
        Map<String, Object> constraints = new HashMap<>();
        
        switch (dataType) {
            case "string":
                constraints.put("minLength", 5);
                constraints.put("maxLength", 30);
                break;
            
            case "date":
                constraints.put("minDate", "2000-01-01");
                constraints.put("maxDate", "2030-12-31");
                constraints.put("format", "yyyy-MM-dd");
                break;
            
            case "zipCode":
                constraints.put("pattern", "\\d{5}");
                break;
            
            case "phoneNumber":
                constraints.put("pattern", "\\d{3}-\\d{3}-\\d{4}");
                break;
            
            case "ssn":
                constraints.put("pattern", "\\d{3}-\\d{2}-\\d{4}");
                break;
            
            case "currency":
                constraints.put("minValue", 10);
                constraints.put("maxValue", 10000);
                constraints.put("precision", 2);
                break;
            
            case "accountNumber":
                constraints.put("pattern", "ACCT-\\d{10}");
                break;
            
            // Add more type-specific constraints as needed
        }
        
        column.setConstraints(constraints);
    }
}
