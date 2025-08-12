package com.example.file_conversion_service.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.file_conversion_service.dto.ConversionRequest;
import com.example.file_conversion_service.dto.ConversionResponse;
import com.example.file_conversion_service.service.FileConversionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileConversionController {
    private static final Logger logger = LoggerFactory.getLogger(FileConversionController.class);
    
    @Autowired
    private FileConversionService fileConversionService;
    
    /**
     * Upload file and start conversion
     */
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> convertFile(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute ConversionRequest request
    ) {
        
        try {
            logger.info("Received conversion request for file: {} to format: {}", file.getOriginalFilename(), request.getTargetFormat());
            
            ConversionResponse response = fileConversionService.startConversion(file, request);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("INVALID_REQUEST", e.getMessage()));
                    
        } catch (IOException e) {
            logger.error("IO error during file upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("UPLOAD_ERROR", "Failed to upload file"));
                    
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
        }
    }
    
    /**
     * Get conversion job status
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ConversionResponse> getStatus(@PathVariable String jobId) {
        logger.info("Getting status for job: {}", jobId);
        
        ConversionResponse response = fileConversionService.getConversionStatus(jobId);
        
        if ("NOT_FOUND".equals(response.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Download a converted file
     */
    @GetMapping("/files/download/{jobId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String jobId) {
        logger.info("Download request for job: {}", jobId);
        
        try {
            File file = fileConversionService.getConvertedFile(jobId);
            
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + file.getName() + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Error downloading file for job: {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        
        boolean serviceReady = fileConversionService.isServiceReady();
        status.put("ffmpeg", serviceReady ? "AVAILABLE" : "NOT_AVAILABLE");
        
        if (serviceReady) {
            // Add GPU status information
            String gpuStatus = fileConversionService.getGpuStatus();
            status.put("gpu", gpuStatus);
        }
        
        if (!serviceReady) {
            status.put("status", "DOWN");
            status.put("error", "FFmpeg not available");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get supported formats
     */
    @GetMapping("/formats")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        Map<String, Object> formats = new HashMap<>();
        
        formats.put("video", new String[]{"mp4", "avi", "mov", "mkv"});
        formats.put("audio", new String[]{"mp3", "wav", "flac", "aac"});
        formats.put("image", new String[]{"jpg", "png", "gif", "webp"});
        
        return ResponseEntity.ok(formats);
    }
    
    private Map<String, String> createErrorResponse(String errorCode, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", errorCode);
        error.put("message", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }
}
