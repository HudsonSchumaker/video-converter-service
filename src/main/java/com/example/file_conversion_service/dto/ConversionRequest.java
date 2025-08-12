package com.example.file_conversion_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ConversionRequest {
    
    @NotBlank(message = "Target format is required")
    @Pattern(regexp = "^(mp4|avi|mov|mkv|mp3|wav|flac|aac|jpg|png|gif|webp)$", 
             message = "Unsupported target format")
    private String targetFormat;
    
    private String quality = "medium"; // low, medium, high
    
    private Integer width;
    private Integer height;
    private Integer bitrate;
    
    // Constructors
    public ConversionRequest() {}
    
    public ConversionRequest(String targetFormat) {
        this.targetFormat = targetFormat;
    }
    
    // Getters and Setters
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
}
