package com.example.file_conversion_service.model;

import java.time.LocalDateTime;

public class ConversionJob {
    private String jobId;
    private String status;
    private String originalFileName;
    private String originalFilePath;
    private String convertedFileName;
    private String convertedFilePath;
    private String originalFormat;
    private String targetFormat;
    private String quality;
    private Integer width;
    private Integer height;
    private Integer bitrate;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private Long originalFileSize;
    private Long convertedFileSize;

    public ConversionJob() {}
    public ConversionJob(String jobId) {
        this.jobId = jobId;
        this.status = "PENDING";
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
    
    public String getOriginalFilePath() {
        return originalFilePath;
    }
    
    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }
    
    public String getConvertedFileName() {
        return convertedFileName;
    }
    
    public void setConvertedFileName(String convertedFileName) {
        this.convertedFileName = convertedFileName;
    }
    
    public String getConvertedFilePath() {
        return convertedFilePath;
    }
    
    public void setConvertedFilePath(String convertedFilePath) {
        this.convertedFilePath = convertedFilePath;
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
    
    public String getQuality() {
        return quality;
    }
    
    public void setQuality(String quality) {
        this.quality = quality;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public void setWidth(Integer width) {
        this.width = width;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public Integer getBitrate() {
        return bitrate;
    }
    
    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
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
