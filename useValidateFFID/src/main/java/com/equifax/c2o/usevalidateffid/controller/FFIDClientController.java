package com.equifax.c2o.usevalidateffid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.equifax.c2o.api.interconnect.model.DecisionResponse;
import com.equifax.c2o.api.interconnect.service.FFIDValidationService;
import com.equifax.c2o.usevalidateffid.service.FFIDBusinessService;
import com.equifax.c2o.usevalidateffid.service.ContractProcessingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/ffid")
public class FFIDClientController {
    private static final Logger logger = LoggerFactory.getLogger(FFIDClientController.class);

    @Autowired
    private FFIDValidationService ffidValidationService;
    
    @Autowired
    private FFIDBusinessService ffidBusinessService;
    
    @Autowired
    private ContractProcessingService contractProcessingService;

    /**
     * Uses the original validateFFID method that returns ResponseEntity
     */
    @GetMapping("/validate")
    public ResponseEntity<DecisionResponse> validateFFID(
        @RequestParam("contractId") Long contractId,
        @RequestParam("fulfillmentId") String fulfillmentId,
        @RequestParam(value = "sfdcEfxId", required = false) String sfdcEfxId,
        @RequestParam(value = "shipToCreationMode", required = false) String shipToCreationMode
    ) {
        logger.info("Received request to validate FFID: contractId={}, fulfillmentId={}", 
            contractId, fulfillmentId);
            
        // Call the FFID validation service from the library - using the ResponseEntity version
        ResponseEntity<DecisionResponse> response = ffidValidationService.validateFFID(
            contractId, fulfillmentId, sfdcEfxId, shipToCreationMode);
            
        logger.info("FFID validation completed with status: {}", 
            response.getStatusCode());
            
        return response;
    }
    
    /**
     * Uses the new validateFFIDDirect method that returns just the DecisionResponse
     */
    @GetMapping("/validate-direct")
    public DecisionResponse validateFFIDDirect(
        @RequestParam("contractId") Long contractId,
        @RequestParam("fulfillmentId") String fulfillmentId,
        @RequestParam(value = "sfdcEfxId", required = false) String sfdcEfxId,
        @RequestParam(value = "shipToCreationMode", required = false) String shipToCreationMode
    ) {
        logger.info("Received request to directly validate FFID: contractId={}, fulfillmentId={}", 
            contractId, fulfillmentId);
            
        // Call the FFID validation service from the library - using the direct version
        DecisionResponse response = ffidValidationService.validateFFIDDirect(
            contractId, fulfillmentId, sfdcEfxId, shipToCreationMode);
            
        logger.info("Direct FFID validation completed");
        
        return response;
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("FFID Client is healthy");
    }
    
    /**
     * Endpoint that uses the business service which internally uses FFIDValidationService
     */
    @GetMapping("/process-contract")
    public ResponseEntity<String> processContract(
        @RequestParam("contractId") Long contractId,
        @RequestParam("fulfillmentId") String fulfillmentId
    ) {
        logger.info("Received request to process contract: contractId={}, fulfillmentId={}", 
            contractId, fulfillmentId);
            
        String result = ffidBusinessService.processContract(contractId, fulfillmentId);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Advanced contract processing with the optimized direct method and caching
     */
    @GetMapping("/advanced-process")
    public ResponseEntity<Map<String, Object>> advancedProcessContract(
        @RequestParam("contractId") Long contractId,
        @RequestParam("fulfillmentId") String fulfillmentId
    ) {
        logger.info("Received request for advanced contract processing: contractId={}, fulfillmentId={}", 
            contractId, fulfillmentId);
            
        Map<String, Object> result = contractProcessingService.processContractWithValidation(
            contractId, fulfillmentId);
            
        logger.info("Advanced processing completed with status: {}", result.get("validationStatus"));
        
        return ResponseEntity.ok(result);
    }
}
