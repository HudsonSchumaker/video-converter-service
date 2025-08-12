package com.example.file_conversion_service.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.file_conversion_service.dto.ConversionRequest;
import com.example.file_conversion_service.dto.ConversionResponse;
import com.example.file_conversion_service.model.ConversionJob;

@Service
public class FileConversionService {
    private static final Logger logger = LoggerFactory.getLogger(FileConversionService.class);
    
    private final FFmpegService ffmpegService;
    private final AsyncConversionService asyncConversionService;
    
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.output.dir:./output}")
    private String outputDir;
    
    @Value("${app.max.file.size:100MB}")
    private String maxFileSize;
    
    public FileConversionService(FFmpegService ffmpegService, AsyncConversionService asyncConversionService) {
        this.ffmpegService = ffmpegService;
        this.asyncConversionService = asyncConversionService;
    }
    
    /**
     * Initiates a file conversion job
     */
    public ConversionResponse startConversion(MultipartFile file, ConversionRequest request) throws IOException {
        // Validate file
        validateFile(file);
        
        // Generate job ID
        String jobId = UUID.randomUUID().toString();
        
        // Create conversion job
        ConversionJob job = new ConversionJob(jobId);
        job.setOriginalFileName(file.getOriginalFilename());
        job.setOriginalFormat(FilenameUtils.getExtension(file.getOriginalFilename()));
        job.setTargetFormat(request.getTargetFormat());
        job.setQuality(request.getQuality());
        job.setWidth(request.getWidth());
        job.setHeight(request.getHeight());
        job.setBitrate(request.getBitrate());
        job.setOriginalFileSize(file.getSize());
        
        // Save uploaded file
        String originalFilePath = saveUploadedFile(file, jobId);
        job.setOriginalFilePath(originalFilePath);
        
        // Set converted file path
        String convertedFileName = generateConvertedFileName(file.getOriginalFilename(), request.getTargetFormat());
        String convertedFilePath = Paths.get(outputDir, convertedFileName).toString();
        job.setConvertedFileName(convertedFileName);
        job.setConvertedFilePath(convertedFilePath);
        
        // Store job in async service
        asyncConversionService.storeJob(job);
        
        // Start async conversion
        asyncConversionService.processConversionAsync(job);
        
        // Return response
        ConversionResponse response = new ConversionResponse(jobId, "PENDING");
        response.setOriginalFileName(job.getOriginalFileName());
        response.setOriginalFormat(job.getOriginalFormat());
        response.setTargetFormat(job.getTargetFormat());
        response.setOriginalFileSize(job.getOriginalFileSize());
        
        return response;
    }
    
    /**
     * Gets the status of a conversion job
     */
    public ConversionResponse getConversionStatus(String jobId) {
        ConversionJob job = asyncConversionService.getJob(jobId);
        if (job == null) {
            ConversionResponse response = new ConversionResponse();
            response.setJobId(jobId);
            response.setStatus("NOT_FOUND");
            response.setErrorMessage("Job not found");
            return response;
        }
        
        ConversionResponse response = new ConversionResponse();
        response.setJobId(job.getJobId());
        response.setStatus(job.getStatus());
        response.setOriginalFileName(job.getOriginalFileName());
        response.setConvertedFileName(job.getConvertedFileName());
        response.setOriginalFormat(job.getOriginalFormat());
        response.setTargetFormat(job.getTargetFormat());
        response.setCreatedAt(job.getCreatedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setErrorMessage(job.getErrorMessage());
        response.setOriginalFileSize(job.getOriginalFileSize());
        response.setConvertedFileSize(job.getConvertedFileSize());
        
        if ("COMPLETED".equals(job.getStatus())) {
            response.setDownloadUrl("/api/files/download/" + jobId);
        }
        
        return response;
    }
    
    /**
     * Gets the converted file for download
     */
    public File getConvertedFile(String jobId) {
        ConversionJob job = asyncConversionService.getJob(jobId);
        if (job == null || !"COMPLETED".equals(job.getStatus())) {
            return null;
        }
        
        File file = new File(job.getConvertedFilePath());
        return file.exists() ? file : null;
    }
    
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        // Check file size
        long maxSizeBytes = parseFileSize(maxFileSize);
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize);
        }
    }
    
    private String saveUploadedFile(MultipartFile file, String jobId) throws IOException {
        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String filename = jobId + "_original." + extension;
        
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }
    
    private String generateConvertedFileName(String originalFilename, String targetFormat) {
        String baseName = FilenameUtils.getBaseName(originalFilename);
        return baseName + "_converted." + targetFormat;
    }
    
    private long parseFileSize(String size) {
        if (size == null || size.trim().isEmpty()) {
            return 100 * 1024 * 1024; // Default 100MB
        }
        
        size = size.trim().toUpperCase();
        long multiplier = 1;
        
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        }
        
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 100 * 1024 * 1024; // Default 100MB
        }
    }
    
    /**
     * Checks if the service is ready (FFmpeg available)
     */
    public boolean isServiceReady() {
        return ffmpegService.isFFmpegAvailable();
    }
    
    /**
     * Gets GPU acceleration status
     */
    public String getGpuStatus() {
        return ffmpegService.getGpuStatus();
    }
}
