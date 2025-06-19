package org.springframework.ai.mcp.sample.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

/**
 * Calculator service providing basic arithmetic operations and currency
 * conversion.
 * Integrates with ExchangeRate-API for live currency exchange rates.
 * 
 * @author Spring AI MCP Example
 */
@Service
public class CalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ExchangeRate-API free tier endpoint (1,500 requests/month)
    private static final String EXCHANGE_API_BASE_URL = "https://open.er-api.com/v6/latest";

    public CalculatorService() {
        // Configure RestTemplate with timeouts to prevent hanging
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(20)); // 20 seconds connection timeout
        factory.setReadTimeout(Duration.ofSeconds(25)); // 25 seconds read timeout

        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Add two numbers together.
     * 
     * @param a First number
     * @param b Second number
     * @return Sum of a and b
     */
    @Tool(description = "Add two numbers together")
    public double add(@ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        double result = a + b;
        logger.info("Addition: {} + {} = {}", a, b, result);
        return result;
    }

    /**
     * Subtract the second number from the first.
     * 
     * @param a First number (minuend)
     * @param b Second number (subtrahend)
     * @return Difference of a minus b
     */
    @Tool(description = "Subtract second number from first number")
    public double subtract(@ToolParam(description = "First number (minuend)") double a,
            @ToolParam(description = "Second number (subtrahend)") double b) {
        double result = a - b;
        logger.info("Subtraction: {} - {} = {}", a, b, result);
        return result;
    }

    /**
     * Multiply two numbers.
     * 
     * @param a First number
     * @param b Second number
     * @return Product of a and b
     */
    @Tool(description = "Multiply two numbers")
    public double multiply(@ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        double result = a * b;
        logger.info("Multiplication: {} * {} = {}", a, b, result);
        return result;
    }

    /**
     * Divide the first number by the second.
     * 
     * @param a First number (dividend)
     * @param b Second number (divisor)
     * @return Quotient of a divided by b
     * @throws ArithmeticException if b is zero
     */
    @Tool(description = "Divide first number by second number")
    public double divide(@ToolParam(description = "First number (dividend)") double a,
            @ToolParam(description = "Second number (divisor)") double b) {
        if (b == 0) {
            logger.error("Division by zero attempted: {} / {}", a, b);
            throw new ArithmeticException("Division by zero is not allowed");
        }
        double result = a / b;
        logger.info("Division: {} / {} = {}", a, b, result);
        return result;
    }

    /**
     * Convert currency from one denomination to another using live exchange rates.
     * Uses ExchangeRate-API free tier for currency data.
     * 
     * @param amount       Amount to convert
     * @param fromCurrency Source currency code (ISO 4217, e.g., "USD")
     * @param toCurrency   Target currency code (ISO 4217, e.g., "EUR")
     * @return Converted amount rounded to 4 decimal places
     * @throws IllegalArgumentException if currency codes are invalid
     * @throws RuntimeException         if exchange rate API is unavailable
     */
    @Tool(description = "Convert amount from one currency to another using live exchange rates")
    public double convertCurrency(@ToolParam(description = "Amount to convert") double amount,
            @ToolParam(description = "Source currency code (e.g., USD)") String fromCurrency,
            @ToolParam(description = "Target currency code (e.g., EUR)") String toCurrency) {

        // Validate input parameters
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (fromCurrency == null || fromCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Source currency code cannot be null or empty");
        }
        if (toCurrency == null || toCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Target currency code cannot be null or empty");
        }

        // Normalize currency codes to uppercase
        String from = fromCurrency.trim().toUpperCase();
        String to = toCurrency.trim().toUpperCase();

        // Handle same currency conversion
        if (from.equals(to)) {
            logger.info("Currency conversion: {} {} to {} {} = {} (same currency)",
                    amount, from, to, to, amount);
            return amount;
        }
        try {
            // Build API URL with source currency as base
            String apiUrl = EXCHANGE_API_BASE_URL + "/" + from;
            logger.debug("Fetching exchange rates from: {}", apiUrl);

            // Call ExchangeRate-API with configured timeouts
            String response = restTemplate.getForObject(apiUrl, String.class);

            if (response == null) {
                throw new RuntimeException("Received null response from exchange rate API");
            }

            // Parse JSON response
            JsonNode jsonResponse = objectMapper.readTree(response);

            // Check if the API call was successful
            if (!jsonResponse.path("result").asText("").equals("success")) {
                String errorType = jsonResponse.path("error-type").asText("Unknown error");
                logger.error("Exchange rate API returned error: {}", errorType);
                throw new RuntimeException("Exchange rate API error: " + errorType);
            }

            // Extract exchange rates
            JsonNode ratesNode = jsonResponse.path("rates");
            if (!ratesNode.has(to)) {
                logger.error("Unsupported target currency code: {}", to);
                throw new IllegalArgumentException("Unsupported target currency code: " + to);
            }

            double exchangeRate = ratesNode.get(to).asDouble();
            if (exchangeRate <= 0) {
                logger.error("Invalid exchange rate received: {}", exchangeRate);
                throw new RuntimeException("Invalid exchange rate received from API");
            }

            double convertedAmount = amount * exchangeRate;

            // Round to 4 decimal places for precision
            BigDecimal rounded = BigDecimal.valueOf(convertedAmount)
                    .setScale(4, RoundingMode.HALF_UP);
            double result = rounded.doubleValue();

            logger.info("Currency conversion: {} {} to {} {} = {} (rate: {})",
                    amount, from, to, to, result, exchangeRate);

            return result;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Handle timeout and connection issues specifically
            logger.error("Network error during currency conversion from {} to {}: {}", from, to, e.getMessage());
            throw new RuntimeException(
                    "Currency conversion failed due to network timeout or connection error. Please try again later.",
                    e);
        } catch (org.springframework.web.client.HttpClientErrorException
                | org.springframework.web.client.HttpServerErrorException e) {
            // Handle HTTP errors from the API
            logger.error("HTTP error during currency conversion from {} to {}: {} - {}", from, to, e.getStatusCode(),
                    e.getMessage());
            throw new RuntimeException("Currency conversion failed due to API error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("Failed to convert currency from {} to {}: {}", from, to, e.getMessage());
            throw new RuntimeException("Currency conversion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get exchange rate between two currencies.
     * 
     * @param fromCurrency Source currency code
     * @param toCurrency   Target currency code
     * @return Exchange rate from source to target currency
     */
    @Tool(description = "Get the current exchange rate between two currencies")
    public double getExchangeRate(@ToolParam(description = "Source currency code (e.g., USD)") String fromCurrency,
            @ToolParam(description = "Target currency code (e.g., EUR)") String toCurrency) {

        // Use convertCurrency with amount = 1 to get the rate
        return convertCurrency(1.0, fromCurrency, toCurrency);
    }

    /**
     * Perform a calculation and generate creative explanations using multiple LLM
     * providers.
     * This tool demonstrates MCP Sampling by delegating creative content generation
     * to different AI models.
     * 
     * @param operation   The operation to perform (add, subtract, multiply, divide)
     * @param a           First number
     * @param b           Second number
     * @param toolContext ToolContext for MCP sampling
     * @return Calculation result with creative explanations from multiple LLM
     *         providers
     */
    @Tool(description = "Perform calculation and generate creative explanations using multiple AI providers")
    public String calculateWithCreativeExplanation(
            @ToolParam(description = "Operation to perform (add, subtract, multiply, divide)") String operation,
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b,
            ToolContext toolContext) {

        // Perform the calculation
        double result;
        String operationSymbol;

        switch (operation.toLowerCase().trim()) {
            case "add":
            case "addition":
                result = add(a, b);
                operationSymbol = "+";
                break;
            case "subtract":
            case "subtraction":
                result = subtract(a, b);
                operationSymbol = "-";
                break;
            case "multiply":
            case "multiplication":
                result = multiply(a, b);
                operationSymbol = "×";
                break;
            case "divide":
            case "division":
                result = divide(a, b);
                operationSymbol = "÷";
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

        // Create a calculation summary object for the LLMs
        var calculationSummary = new CalculationResult(a, b, operationSymbol, result, operation);

        // Generate creative responses using MCP Sampling
        String responseWithExplanations = callMcpSampling(toolContext, calculationSummary);

        return responseWithExplanations;
    }

    /**
     * Calls MCP Sampling to generate creative explanations from multiple LLM
     * providers.
     * 
     * @param toolContext       The tool context containing MCP exchange information
     * @param calculationResult The calculation result to explain creatively
     * @return Combined responses from different LLM providers
     */    public String callMcpSampling(ToolContext toolContext, CalculationResult calculationResult) {

        StringBuilder openAiExplanation = new StringBuilder();
        StringBuilder githubModelsExplanation = new StringBuilder();

        McpToolUtils.getMcpExchange(toolContext)
                .ifPresent(exchange -> {

                    exchange.loggingNotification(LoggingMessageNotification.builder()
                            .level(LoggingLevel.INFO)
                            .data("Start sampling for calculation explanation")
                            .build());

                    if (exchange.getClientCapabilities().sampling() != null) {
                        try {
                            var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
                                    .systemPrompt(
                                            "You are a creative mathematics teacher who explains calculations in an engaging and imaginative way!")
                                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                                            new McpSchema.TextContent(
                                                    "Please create a creative and educational explanation for this calculation. Use metaphors, storytelling, or real-world examples to make it interesting:\n\n"
                                                            + "Calculation: " + calculationResult.a() + " "
                                                            + calculationResult.operationSymbol() + " "
                                                            + calculationResult.b() + " = " + calculationResult.result()
                                                            + "\n"
                                                            + "Operation: " + calculationResult.operation() + "\n\n"
                                                            + "Make it engaging and educational. Use markdown formatting for better presentation."))));

                            // Get explanation from OpenAI model
                            try {
                                var openAiLlmMessageRequest = messageRequestBuilder
                                        .modelPreferences(ModelPreferences.builder().addHint("openai").build())
                                        .build();
                                CreateMessageResult openAiLlmResponse = exchange.createMessage(openAiLlmMessageRequest);
                                openAiExplanation.append(((McpSchema.TextContent) openAiLlmResponse.content()).text());
                            } catch (Exception e) {
                                logger.warn("Failed to get OpenAI explanation: " + e.getMessage());
                                openAiExplanation
                                        .append("OpenAI explanation unavailable due to error: " + e.getMessage());
                            }                            // Get explanation from GitHub Models (GPT-4o-mini)
                            try {
                                var githubModelsLlmMessageRequest = messageRequestBuilder
                                        .modelPreferences(ModelPreferences.builder().addHint("github").build())
                                        .build();
                                CreateMessageResult githubModelsLlmResponse = exchange
                                        .createMessage(githubModelsLlmMessageRequest);
                                githubModelsExplanation
                                        .append(((McpSchema.TextContent) githubModelsLlmResponse.content()).text());
                            } catch (Exception e) {
                                logger.warn("Failed to get GitHub Models explanation: " + e.getMessage());
                                githubModelsExplanation
                                        .append("GitHub Models explanation unavailable due to error: " + e.getMessage());
                            }                        } catch (Exception e) {
                            logger.error("Error during sampling setup: " + e.getMessage());
                            openAiExplanation.append("Sampling unavailable due to configuration error");
                            githubModelsExplanation.append("Sampling unavailable due to configuration error");
                        }
                    } else {
                        logger.warn("Client does not support sampling capability");
                        openAiExplanation.append("Sampling capability not available on client");
                        githubModelsExplanation.append("Sampling capability not available on client");
                    }

                    exchange.loggingNotification(LoggingMessageNotification.builder()
                            .level(LoggingLevel.INFO)
                            .data("Finish sampling for calculation explanation")
                            .build());
                });        // Combine all responses
        String combinedResponse = "# Calculation Result\n\n" +
                "**" + calculationResult.a() + " " + calculationResult.operationSymbol() + " " + calculationResult.b()
                + " = " + calculationResult.result() + "**\n\n" +
                "---\n\n" +
                "## OpenAI's Creative Explanation\n\n" + openAiExplanation.toString() + "\n\n" +
                "---\n\n" +
                "## GitHub Models (GPT-4o-mini) Creative Explanation\n\n" + githubModelsExplanation.toString() + "\n\n" +
                "---\n\n" +
                "*This response was generated using MCP Sampling with multiple AI providers.*";

        logger.info("Generated creative explanation for calculation: {} {} {} = {}",
                calculationResult.a(), calculationResult.operationSymbol(), calculationResult.b(),
                calculationResult.result());

        return combinedResponse;
    }

    /**
     * Record to hold calculation results for sampling.
     */
    public record CalculationResult(double a, double b, String operationSymbol, double result, String operation) {
    }
}
