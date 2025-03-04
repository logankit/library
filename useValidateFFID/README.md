# Use Validate FFID

This is a sample Spring Boot application that uses the C2O Interconnect Client library for FFID validation.

## Overview

This application demonstrates how to use the FFID validation functionality from the C2O Interconnect Client library while sharing the same database with the original service.

## Prerequisites

- Java 17+
- Maven 3.8+
- Access to C2O Oracle database
- C2O Interconnect Client library installed in your local Maven repository

## Building and Running

### Build the Library First

Before building this application, you need to build and install the FFID validation library:

```bash
cd ../MVP2
mvn clean install
```

### Building the Application

To build the application and create an executable JAR:

```bash
cd ../useValidateFFID
mvn clean package
```

This will create an executable JAR in the `target` directory.

### Running the Application

You can run the application using:

```bash
java -jar target/useValidateFFID-0.0.1-SNAPSHOT.jar
```

Or using Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

### Environment Variables

The following environment variables should be set before running:

- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `OKTA_USERNAME` - Okta username
- `OKTA_PASSWORD` - Okta password
- `OKTA_CLIENT_ID` - Okta client ID
- `OKTA_CLIENT_SECRET` - Okta client secret

## Configuration

The application's configuration can be modified in `src/main/resources/application.properties`. Make sure to update:

1. Database connection details to point to the C2O database
2. FFID validation URL 
3. Okta authentication details

## Security Notes

In a production environment, sensitive information like passwords and API keys should be stored securely using environment variables or a secrets management service.

## API Endpoints

- `GET /api/ffid/validate` - Validates an FFID using the library service with ResponseEntity
  - Parameters:
    - `contractId` - Contract ID (required)
    - `fulfillmentId` - Fulfillment ID (required)
    - `sfdcEfxId` - Salesforce Equifax ID (optional)
    - `shipToCreationMode` - Ship-to creation mode (optional)

- `GET /api/ffid/validate-direct` - Validates an FFID using the direct method without ResponseEntity
  - Parameters: 
    - `contractId` - Contract ID (required)
    - `fulfillmentId` - Fulfillment ID (required)
    - `sfdcEfxId` - Salesforce Equifax ID (optional)
    - `shipToCreationMode` - Ship-to creation mode (optional)

- `GET /api/ffid/process-contract` - Processes a contract after validating its FFID
  - Parameters:
    - `contractId` - Contract ID (required)
    - `fulfillmentId` - Fulfillment ID (required)

- `GET /api/ffid/advanced-process` - Advanced contract processing with caching and detailed response
  - Parameters:
    - `contractId` - Contract ID (required)
    - `fulfillmentId` - Fulfillment ID (required)
  - Returns:
    - JSON object with validation status, timestamps, and next steps

- `GET /api/ffid/health` - Health check endpoint

## Using the Direct Method

The FFID validation service provides two ways to validate FFIDs:

1. **REST API approach** (`validateFFID`): Returns a `ResponseEntity<DecisionResponse>` with appropriate HTTP status codes
2. **Direct method approach** (`validateFFIDDirect`): Returns just the `DecisionResponse` without HTTP wrapping

For internal service-to-service communication, the direct method is preferred:

```java
@Service
public class MyService {
    @Autowired
    private FFIDValidationService ffidValidationService;
    
    public void processData(Long contractId, String fulfillmentId) {
        // Direct call - cleaner for internal use
        DecisionResponse response = ffidValidationService.validateFFIDDirect(
            contractId, fulfillmentId, null, null);
            
        if (response != null && 
            response.getOutcome() != null && 
            "Approved".equals(response.getOutcome().getStatus())) {
            // Process approved FFID
        }
    }
}
```

## Implementation Patterns

This project demonstrates several patterns for using the FFID validation library:

### 1. Direct vs. ResponseEntity Methods

Choose the appropriate method based on your needs:

- `validateFFID()` - Returns ResponseEntity, good for REST API endpoints
- `validateFFIDDirect()` - Returns plain DecisionResponse, better for internal service calls

### 2. Service Composition

The `ContractProcessingService` demonstrates:
- Using the direct method for cleaner service-to-service communication
- Implementing caching to reduce redundant validation calls
- Building rich business responses based on validation results

### 3. Auto-Configuration

The application leverages Spring Boot's auto-configuration to:
- Automatically detect and initialize the FFID validation components
- No explicit component scanning needed in the main application class

## Troubleshooting

If you encounter issues with database access, ensure that both applications are configured to use the same database connection details and that the required tables are accessible to the user specified in the configuration.
