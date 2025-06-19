package org.springframework.ai.mcp.sample.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.sample.server.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    @Autowired
    private CalculatorService calculatorService;

    /**
     * Perform arithmetic calculations via HTTP POST.
     * 
     * @param request CalculationRequest containing operands and operation
     * @return CalculationResponse with the computed result
     */
    @PostMapping("/calculate")
    public ResponseEntity<CalculationResponse> calculate(@RequestBody CalculationRequest request) {
        try {
            logger.info("Received calculation request: {} {} {}", 
                       request.getA(), request.getOperation(), request.getB());
            
            double result;
            String operation = request.getOperation().toLowerCase().trim();
            
            // Route to appropriate calculator service method
            switch (operation) {
                case "add":
                case "addition":
                case "+":
                    result = calculatorService.add(request.getA(), request.getB());
                    break;
                case "subtract":
                case "subtraction":
                case "-":
                    result = calculatorService.subtract(request.getA(), request.getB());
                    break;
                case "multiply":
                case "multiplication":
                case "*":
                    result = calculatorService.multiply(request.getA(), request.getB());
                    break;
                case "divide":
                case "division":
                case "/":
                    result = calculatorService.divide(request.getA(), request.getB());
                    break;
                default:
                    logger.error("Unsupported operation: {}", request.getOperation());
                    return ResponseEntity.badRequest()
                            .body(new CalculationResponse(Double.NaN));
            }
            
            CalculationResponse response = new CalculationResponse(result);
            logger.info("Calculation completed successfully: result = {}", result);
            return ResponseEntity.ok(response);
            
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error in calculation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new CalculationResponse(Double.NaN));
        } catch (Exception e) {
            logger.error("Unexpected error in calculation: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new CalculationResponse(Double.NaN));
        }
    }

    /**
     * Convert currency amounts via HTTP POST.
     * 
     * @param request CurrencyConversionRequest containing amount and currency codes
     * @return CurrencyConversionResponse with conversion details and result
     */
    @PostMapping("/convert-currency")
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(
            @RequestBody CurrencyConversionRequest request) {
        try {
            logger.info("Received currency conversion request: {} {} to {}", 
                       request.getAmount(), request.getFrom(), request.getTo());
            
            // Get exchange rate for response details
            double exchangeRate = calculatorService.getExchangeRate(request.getFrom(), request.getTo());
            
            // Perform conversion
            double convertedAmount = calculatorService.convertCurrency(
                    request.getAmount(), request.getFrom(), request.getTo());
            
            CurrencyConversionResponse response = new CurrencyConversionResponse(
                    request.getAmount(),
                    request.getFrom().toUpperCase(),
                    request.getTo().toUpperCase(),
                    convertedAmount,
                    exchangeRate
            );
            
            logger.info("Currency conversion completed successfully: {} {} = {} {}", 
                       request.getAmount(), request.getFrom(), convertedAmount, request.getTo());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid currency conversion request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            logger.error("Currency conversion failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("Unexpected error in currency conversion: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint to verify service availability.
     * 
     * @return Simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Calculator & Currency Service is running");
    }

    /**
     * Get information about available operations.
     * 
     * @return List of supported operations and currencies
     */
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        String info = """
                Calculator & Currency Service
                
                Available Operations:
                POST /calculate
                - Supported operations: add, subtract, multiply, divide
                - Example: {"a": 5, "b": 3, "operation": "add"}
                
                POST /convert-currency
                - Supports 150+ currencies (ISO 4217 codes)
                - Example: {"amount": 100, "from": "USD", "to": "EUR"}
                
                GET /health - Service health check
                GET /info - This information
                """;
        return ResponseEntity.ok(info);
    }
}
