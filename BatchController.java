package com.example.tdg.controller;

import com.example.tdg.exception.DataGenerationException;
import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.BatchGenerationRequestDto;
import com.example.tdg.model.dto.BatchGenerationResultDto;
import com.example.tdg.model.entity.Template;
import com.example.tdg.service.BatchGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * REST API controller for batch data generation operations.
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {
    
    @Autowired
    private BatchGenerationService batchGenerationService;
    
    /**
     * Generate data for multiple templates in a batch.
     * 
     * @param request The batch generation request
     * @return Batch generation results
     */
    @PostMapping("/generate")
    public ResponseEntity<?> batchGenerate(@Valid @RequestBody BatchGenerationRequestDto request) {
        try {
            List<BatchGenerationResultDto> results = batchGenerationService.generateBatch(
                    request.getTemplateIds(),
                    request.getRowCount(),
                    request.getOutputFormat(),
                    request.isParallel()
            );
            return ResponseEntity.ok(results);
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataGenerationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
