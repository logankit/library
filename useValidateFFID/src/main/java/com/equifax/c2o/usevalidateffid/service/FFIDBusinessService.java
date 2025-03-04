package com.equifax.c2o.usevalidateffid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.equifax.c2o.api.interconnect.model.DecisionResponse;
import com.equifax.c2o.api.interconnect.service.FFIDValidationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example business service that uses the FFIDValidationService directly
 */
@Service
public class FFIDBusinessService {
    private static final Logger logger = LoggerFactory.getLogger(FFIDBusinessService.class);

    @Autowired
    private FFIDValidationService ffidValidationService;
    
    /**
     * Example business method that validates an FFID and performs additional logic
     * based on the validation result
     */
    public boolean isFFIDValid(Long contractId, String fulfillmentId) {
        // Use the direct method to validate the FFID
        DecisionResponse response = ffidValidationService.validateFFIDDirect(
            contractId, fulfillmentId, null, null);
        
        // Check if response is valid
        if (response == null || response.getOutcome() == null) {
            logger.warn("Invalid FFID validation response for contract: {}", contractId);
            return false;
        }
        
        // Check if status is "Approved"
        String status = response.getOutcome().getStatus();
        boolean isValid = "Approved".equalsIgnoreCase(status);
        
        logger.info("FFID validation for contract {} is {}", contractId, isValid ? "valid" : "invalid");
        
        return isValid;
    }
    
    /**
     * Example of more complex business logic that uses FFID validation
     */
    public String processContract(Long contractId, String fulfillmentId) {
        // First validate the FFID
        if (!isFFIDValid(contractId, fulfillmentId)) {
            return "Contract processing failed: Invalid FFID";
        }
        
        // Additional business logic would go here
        logger.info("Processing contract {} with valid FFID", contractId);
        
        // More processing steps...
        
        return "Contract processed successfully";
    }
}
