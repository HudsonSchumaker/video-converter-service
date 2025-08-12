package com.example.file_conversion_service.dto;

import java.time.LocalDateTime;

public class ConversionResponse {
    
    private String jobId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String originalFileName;
    private String convertedFileName;
    private String originalFormat;
    private String targetFormat;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String downloadUrl;
    private String errorMessage;
    private Long originalFileSize;
    private Long convertedFileSize;
    
    // Constructors
    public ConversionResponse() {}
    
    public ConversionResponse(String jobId, String status) {
        this.jobId = jobId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getConvertedFileName() {
        return convertedFileName;
    }
    
    public void setConvertedFileName(String convertedFileName) {
        this.convertedFileName = convertedFileName;
    }
    
    public String getOriginalFormat() {
        return originalFormat;
    }
    
    public void setOriginalFormat(String originalFormat) {
        this.originalFormat = originalFormat;
    }
    
    public String getTargetFormat() {
        return targetFormat;
    }
    
    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getOriginalFileSize() {
        return originalFileSize;
    }
    
    public void setOriginalFileSize(Long originalFileSize) {
        this.originalFileSize = originalFileSize;
    }
    
    public Long getConvertedFileSize() {
        return convertedFileSize;
    }
    
    public void setConvertedFileSize(Long convertedFileSize) {
        this.convertedFileSize = convertedFileSize;
    }
}
