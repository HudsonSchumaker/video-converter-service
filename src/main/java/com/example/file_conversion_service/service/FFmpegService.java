package com.example.file_conversion_service.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.file_conversion_service.model.ConversionJob;

@Service
public class FFmpegService {
    
    private static final Logger logger = LoggerFactory.getLogger(FFmpegService.class);
    
    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;
    
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.output.dir:./output}")
    private String outputDir;
    
    @Value("${app.ffmpeg.gpu.enabled:true}")
    private boolean gpuEnabled;
    
    @Value("${app.ffmpeg.gpu.auto-detect:true}")
    private boolean gpuAutoDetect;
    
    @Value("${app.ffmpeg.gpu.preferred:auto}")
    private String preferredGpu;
    
    // GPU acceleration cache
    private String detectedGpuEncoder = null;
    private boolean gpuDetectionComplete = false;
    
    /**
     * Converts a file using FFmpeg
     */
    public boolean convertFile(ConversionJob job) {
        try {
            // Ensure output directory exists
            ensureDirectoryExists(outputDir);
            
            // Detect GPU acceleration if enabled
            if (gpuEnabled && !gpuDetectionComplete) {
                detectGpuAcceleration();
            }
            
            // Build FFmpeg command
            List<String> command = buildFFmpegCommand(job);
            
            logger.info("Executing FFmpeg command: {}", String.join(" ", command));
            
            // Execute the command
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    logger.debug("FFmpeg output: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("FFmpeg conversion completed successfully for job: {}", job.getJobId());
                
                // Set a converted file size
                File convertedFile = new File(job.getConvertedFilePath());
                if (convertedFile.exists()) {
                    job.setConvertedFileSize(convertedFile.length());
                }
                
                return true;
            } else {
                logger.error("FFmpeg conversion failed for job: {} with exit code: {}", job.getJobId(), exitCode);
                logger.error("FFmpeg output: {}", output.toString());
                job.setErrorMessage("FFmpeg conversion failed with exit code: " + exitCode);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error during file conversion for job: {}", job.getJobId(), e);
            job.setErrorMessage("Conversion error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Builds the FFmpeg command based on the conversion job
     */
    private List<String> buildFFmpegCommand(ConversionJob job) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(job.getOriginalFilePath());
        
        // Add quality settings
        addQualitySettings(command, job);
        
        // Add resolution settings for video
        if (isVideoFormat(job.getTargetFormat())) {
            addVideoSettings(command, job);
        }
        
        // Add audio settings
        if (isAudioFormat(job.getTargetFormat())) {
            addAudioSettings(command, job);
        }
        
        // Add image settings
        if (isImageFormat(job.getTargetFormat())) {
            addImageSettings(command, job);
        }
        
        // Overwrite output file
        command.add("-y");
        
        // Output file
        command.add(job.getConvertedFilePath());
        
        return command;
    }
    
    private void addQualitySettings(List<String> command, ConversionJob job) {
        String quality = job.getQuality();
        if (quality == null) quality = "medium";
        
        switch (quality.toLowerCase()) {
            case "low":
                if (isVideoFormat(job.getTargetFormat())) {
                    command.addAll(Arrays.asList("-crf", "28", "-preset", "fast"));
                }
                break;
            case "high":
                if (isVideoFormat(job.getTargetFormat())) {
                    command.addAll(Arrays.asList("-crf", "18", "-preset", "slow"));
                }
                break;
            default: // medium
                if (isVideoFormat(job.getTargetFormat())) {
                    command.addAll(Arrays.asList("-crf", "23", "-preset", "medium"));
                }
                break;
        }
    }
    
    private void addVideoSettings(List<String> command, ConversionJob job) {
        String targetFormat = job.getTargetFormat().toLowerCase();
        
        // Use GPU acceleration if available
        if (gpuEnabled && detectedGpuEncoder != null && isVideoFormat(targetFormat)) {
            addGpuVideoSettings(command, job);
        } else {
            addCpuVideoSettings(command, job);
        }
        
        // Resolution
        if (job.getWidth() != null && job.getHeight() != null) {
            command.addAll(Arrays.asList("-s", job.getWidth() + "x" + job.getHeight()));
        }
        
        // Bitrate
        if (job.getBitrate() != null) {
            command.addAll(Arrays.asList("-b:v", job.getBitrate() + "k"));
        }
    }
    
    private void addGpuVideoSettings(List<String> command, ConversionJob job) {
        String targetFormat = job.getTargetFormat().toLowerCase();
        logger.info("Using GPU acceleration: {}", detectedGpuEncoder);
        
        switch (detectedGpuEncoder) {
            case "h264_nvenc": // NVIDIA
                command.addAll(Arrays.asList("-c:v", "h264_nvenc"));
                addNvidiaSettings(command, job);
                break;
            case "h264_amf": // AMD
                command.addAll(Arrays.asList("-c:v", "h264_amf"));
                addAmdSettings(command, job);
                break;
            case "h264_qsv": // Intel Quick Sync
                command.addAll(Arrays.asList("-c:v", "h264_qsv"));
                addIntelSettings(command, job);
                break;
            case "h264_videotoolbox": // Apple VideoToolbox
                command.addAll(Arrays.asList("-c:v", "h264_videotoolbox"));
                addAppleSettings(command, job);
                break;
            default:
                addCpuVideoSettings(command, job);
                break;
        }
        
        // Add audio codec
        addAudioCodecForVideo(command, targetFormat);
    }
    
    private void addCpuVideoSettings(List<String> command, ConversionJob job) {
        String targetFormat = job.getTargetFormat().toLowerCase();
        
        switch (targetFormat) {
            case "mp4", "mov", "mkv":
                command.addAll(Arrays.asList("-c:v", "libx264", "-c:a", "aac"));
                break;
            case "avi":
                command.addAll(Arrays.asList("-c:v", "libx264", "-c:a", "mp3"));
                break;
        }
    }
    
    private void addNvidiaSettings(List<String> command, ConversionJob job) {
        String quality = job.getQuality();
        if (quality == null) quality = "medium";
        
        switch (quality.toLowerCase()) {
            case "low":
                command.addAll(Arrays.asList("-preset", "fast", "-cq", "28"));
                break;
            case "high":
                command.addAll(Arrays.asList("-preset", "slow", "-cq", "18"));
                break;
            default: // medium
                command.addAll(Arrays.asList("-preset", "medium", "-cq", "23"));
                break;
        }
    }
    
    private void addAmdSettings(List<String> command, ConversionJob job) {
        String quality = job.getQuality();
        if (quality == null) quality = "medium";
        
        switch (quality.toLowerCase()) {
            case "low":
                command.addAll(Arrays.asList("-quality", "speed", "-rc", "cqp", "-qp_i", "28", "-qp_p", "30"));
                break;
            case "high":
                command.addAll(Arrays.asList("-quality", "quality", "-rc", "cqp", "-qp_i", "18", "-qp_p", "20"));
                break;
            default: // medium
                command.addAll(Arrays.asList("-quality", "balanced", "-rc", "cqp", "-qp_i", "23", "-qp_p", "25"));
                break;
        }
    }
    
    private void addIntelSettings(List<String> command, ConversionJob job) {
        String quality = job.getQuality();
        if (quality == null) quality = "medium";
        
        switch (quality.toLowerCase()) {
            case "low":
                command.addAll(Arrays.asList("-preset", "fast", "-global_quality", "28"));
                break;
            case "high":
                command.addAll(Arrays.asList("-preset", "slow", "-global_quality", "18"));
                break;
            default: // medium
                command.addAll(Arrays.asList("-preset", "medium", "-global_quality", "23"));
                break;
        }
    }
    
    private void addAppleSettings(List<String> command, ConversionJob job) {
        String quality = job.getQuality();
        if (quality == null) quality = "medium";
        
        switch (quality.toLowerCase()) {
            case "low":
                command.addAll(Arrays.asList("-q:v", "70"));
                break;
            case "high":
                command.addAll(Arrays.asList("-q:v", "90"));
                break;
            default: // medium
                command.addAll(Arrays.asList("-q:v", "80"));
                break;
        }
    }
    
    private void addAudioCodecForVideo(List<String> command, String targetFormat) {
        switch (targetFormat) {
            case "mp4", "mov", "mkv":
                command.addAll(Arrays.asList("-c:a", "aac"));
                break;
            case "avi":
                command.addAll(Arrays.asList("-c:a", "mp3"));
                break;
        }
    }
    
    private void addAudioSettings(List<String> command, ConversionJob job) {
        switch (job.getTargetFormat().toLowerCase()) {
            case "mp3":
                command.addAll(Arrays.asList("-c:a", "libmp3lame"));
                break;
            case "wav":
                command.addAll(Arrays.asList("-c:a", "pcm_s16le"));
                break;
            case "flac":
                command.addAll(Arrays.asList("-c:a", "flac"));
                break;
            case "aac":
                command.addAll(Arrays.asList("-c:a", "aac"));
                break;
        }
        
        // Audio bitrate
        if (job.getBitrate() != null) {
            command.addAll(Arrays.asList("-b:a", job.getBitrate() + "k"));
        }
    }
    
    private void addImageSettings(List<String> command, ConversionJob job) {
        // Resolution for images
        if (job.getWidth() != null && job.getHeight() != null) {
            command.addAll(Arrays.asList("-s", job.getWidth() + "x" + job.getHeight()));
        }
        
        // Quality for JPEG
        if ("jpg".equals(job.getTargetFormat().toLowerCase()) || "jpeg".equals(job.getTargetFormat().toLowerCase())) {
            String quality = job.getQuality();
            int qValue = 2; // Default medium quality
            if ("high".equals(quality)) qValue = 1;
            else if ("low".equals(quality)) qValue = 5;
            command.addAll(Arrays.asList("-q:v", String.valueOf(qValue)));
        }
    }
    
    private boolean isVideoFormat(String format) {
        return Arrays.asList("mp4", "avi", "mov", "mkv").contains(format.toLowerCase());
    }
    
    private boolean isAudioFormat(String format) {
        return Arrays.asList("mp3", "wav", "flac", "aac").contains(format.toLowerCase());
    }
    
    private boolean isImageFormat(String format) {
        return Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(format.toLowerCase());
    }
    
    private void ensureDirectoryExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
    
    /**
     * Checks if FFmpeg is available on the system
     */
    public boolean isFFmpegAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegPath, "-version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.error("FFmpeg not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Detects available GPU acceleration
     */
    private void detectGpuAcceleration() {
        if (!gpuAutoDetect) {
            return;
        }
        
        logger.info("Detecting GPU acceleration capabilities...");
        
        // List of GPU encoders to test (in order of preference)
        String[] gpuEncoders = {
            "h264_videotoolbox", // Apple VideoToolbox (macOS)
            "h264_nvenc",        // NVIDIA NVENC
            "h264_amf",          // AMD AMF
            "h264_qsv"           // Intel Quick Sync
        };
        
        for (String encoder : gpuEncoders) {
            if (testGpuEncoder(encoder)) {
                this.detectedGpuEncoder = encoder;
                logger.info("GPU acceleration available: {}", encoder);
                break;
            }
        }
        
        if (detectedGpuEncoder == null) {
            logger.info("No GPU acceleration available, falling back to CPU encoding");
        }
        
        this.gpuDetectionComplete = true;
    }
    
    /**
     * Tests if a specific GPU encoder is available
     */
    private boolean testGpuEncoder(String encoder) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath, "-f", "lavfi", "-i", "testsrc=duration=1:size=32x32:rate=1", 
                "-c:v", encoder, "-f", "null", "-"
            );
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Consume output to prevent blocking
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Optionally log the output for debugging
                    logger.debug("GPU encoder test output: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (Exception e) {
            logger.debug("GPU encoder {} not available: {}", encoder, e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets information about GPU acceleration status
     */
    public String getGpuStatus() {
        if (!gpuEnabled) {
            return "GPU acceleration disabled";
        }
        
        if (!gpuDetectionComplete) {
            detectGpuAcceleration();
        }
        
        if (detectedGpuEncoder != null) {
            return "GPU acceleration enabled: " + detectedGpuEncoder;
        } else {
            return "GPU acceleration not available";
        }
    }
}
