package br.schumaker.fcs.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.schumaker.fcs.model.ConversionJob;

@Service
public class AsyncConversionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncConversionService.class);
    
    private final Map<String, ConversionJob> jobStorage = new ConcurrentHashMap<>();
    
    @Autowired
    private FFmpegService ffmpegService;
    
    public void storeJob(ConversionJob job) {
        jobStorage.put(job.getJobId(), job);
    }
    
    public ConversionJob getJob(String jobId) {
        return jobStorage.get(jobId);
    }
    
    /**
     * Processes file conversion asynchronously
     */
    @Async("conversionTaskExecutor")
    public void processConversionAsync(ConversionJob job) {
        try {
            logger.info("Starting async conversion for job: {}", job.getJobId());
            job.setStatus("PROCESSING");
            
            boolean success = ffmpegService.convertFile(job);
            
            if (success) {
                job.setStatus("COMPLETED");
                job.setCompletedAt(LocalDateTime.now());
                logger.info("Conversion completed successfully for job: {}", job.getJobId());
            } else {
                job.setStatus("FAILED");
                job.setCompletedAt(LocalDateTime.now());
                logger.error("Conversion failed for job: {}", job.getJobId());
            }
            
        } catch (Exception e) {
            logger.error("Error processing conversion for job: {}", job.getJobId(), e);
            job.setStatus("FAILED");
            job.setErrorMessage("Processing error: " + e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
        }
    }
}
