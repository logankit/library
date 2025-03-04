package com.equifax.c2o.usevalidateffid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.equifax.c2o.api.interconnect.model.DecisionResponse;
import com.equifax.c2o.api.interconnect.service.FFIDValidationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service that demonstrates using the direct FFID validation method
 * in a real-world business scenario for contract processing.
 */
@Service
public class ContractProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(ContractProcessingService.class);
    
    // Cache to store validation results to avoid redundant calls
    private final Map<String, ValidationResult> validationCache = new ConcurrentHashMap<>();
    
    @Autowired
    private FFIDValidationService ffidValidationService;
    
    /**
     * Process a contract with validation
     */
    public Map<String, Object> processContractWithValidation(Long contractId, String fulfillmentId) {
        Map<String, Object> result = new HashMap<>();
        
        // Generate cache key
        String cacheKey = contractId + ":" + fulfillmentId;
        
        // Check cache first
        ValidationResult cachedResult = validationCache.get(cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            logger.info("Using cached validation result for contract: {}", contractId);
            return generateResponse(contractId, fulfillmentId, cachedResult.isValid());
        }
        
        // Validate using the direct method - no ResponseEntity wrapper
        DecisionResponse response = ffidValidationService.validateFFIDDirect(
                contractId, fulfillmentId, null, null);
        
        // Process validation result
        boolean isValid = isValidResponse(response);
        
        // Store in cache
        validationCache.put(cacheKey, new ValidationResult(isValid));
        
        // Generate and return the business response
        return generateResponse(contractId, fulfillmentId, isValid);
    }
    
    /**
     * Check if the FFID validation response indicates a valid FFID
     */
    private boolean isValidResponse(DecisionResponse response) {
        if (response == null || response.getOutcome() == null) {
            logger.warn("Received null or incomplete response from FFID validation");
            return false;
        }
        
        String status = response.getOutcome().getStatus();
        boolean isValid = "Approved".equalsIgnoreCase(status);
        
        logger.info("FFID validation status: {}", status);
        return isValid;
    }
    
    /**
     * Generate a business response based on validation result
     */
    private Map<String, Object> generateResponse(Long contractId, String fulfillmentId, boolean isValid) {
        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contractId);
        result.put("fulfillmentId", fulfillmentId);
        result.put("validationStatus", isValid ? "VALID" : "INVALID");
        result.put("timestamp", System.currentTimeMillis());
        
        if (isValid) {
            result.put("message", "Contract is valid and ready for processing");
            result.put("nextSteps", "Contract will be processed automatically");
            // Add more business-specific data here
        } else {
            result.put("message", "Contract failed validation");
            result.put("nextSteps", "Please review contract details and resubmit");
            // Add error details here
        }
        
        return result;
    }
    
    /**
     * Inner class to store validation results with expiration
     */
    private static class ValidationResult {
        private final boolean valid;
        private final long timestamp;
        private static final long EXPIRATION_MS = 300000; // 5 minutes
        
        public ValidationResult(boolean valid) {
            this.valid = valid;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > EXPIRATION_MS;
        }
    }
}
