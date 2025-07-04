/* 
* Copyright 2025 - 2025 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.server;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import org.slf4j.Logger;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Simple Calculator Service that performs arithmetic operations
 * with creative explanations using MCP Sampling.
 * 
 * @author Spring AI MCP Example
 */
@Service
public class SimpleCalculatorService {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleCalculatorService.class);
	/**
	 * Calculation result record for MCP sampling
	 */
	public record CalculationResult(double a, double b, String operation, double result) {
	}

	@Tool(description = "Perform basic calculation with creative explanations")
	public String calculate(
			@ToolParam(description = "First number") double a,
			@ToolParam(description = "Second number") double b,
			@ToolParam(description = "Operation: add, subtract, multiply, divide") String operation,
			ToolContext toolContext) {

		// Perform the calculation
		double result = performCalculation(a, b, operation);
		
		CalculationResult calculationResult = new CalculationResult(a, b, operation, result);

		String responseWithExplanations = callMcpSampling(toolContext, calculationResult);

		return responseWithExplanations;
	}
	
	/**
	 * Perform basic arithmetic calculation
	 */
	private double performCalculation(double a, double b, String operation) {
		return switch (operation.toLowerCase().trim()) {
			case "add", "addition" -> a + b;
			case "subtract", "subtraction" -> a - b;
			case "multiply", "multiplication" -> a * b;
			case "divide", "division" -> {
				if (b == 0) {
					throw new ArithmeticException("Division by zero is not allowed");
				}
				yield a / b;
			}
			default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
		};
	}
	
	public String callMcpSampling(ToolContext toolContext, CalculationResult calculationResult) {

		StringBuilder openAiExplanation = new StringBuilder();
		StringBuilder ollamaExplanation = new StringBuilder();

		McpToolUtils.getMcpExchange(toolContext)
				.ifPresent(exchange -> {

					exchange.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.INFO)
							.data("Start sampling for calculation")
							.build());

					if (exchange.getClientCapabilities().sampling() != null) {
						var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
								.systemPrompt("You are a creative mathematics teacher who explains calculations in an engaging way!")
								.messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
										new McpSchema.TextContent(
												"Please create a fun and educational explanation for this calculation. Use markdown format:\n"
														+ ModelOptionsUtils.toJsonStringPrettyPrinter(calculationResult)))));

						// Get explanation from OpenAI
						try {
							var openAiLlmMessageRequest = messageRequestBuilder
									.modelPreferences(ModelPreferences.builder().addHint("openai").build())
									.build();
							CreateMessageResult openAiLlmResponse = exchange.createMessage(openAiLlmMessageRequest);
							openAiExplanation.append(((McpSchema.TextContent) openAiLlmResponse.content()).text());
						} catch (Exception e) {
							logger.warn("Failed to get OpenAI explanation: " + e.getMessage());
							openAiExplanation.append("OpenAI explanation unavailable: " + e.getMessage());
						}

						// Get explanation from Ollama
						try {
							var ollamaLlmMessageRequest = messageRequestBuilder
									.modelPreferences(ModelPreferences.builder().addHint("ollama").build())
									.build();
							CreateMessageResult ollamaLlmResponse = exchange.createMessage(ollamaLlmMessageRequest);
							ollamaExplanation.append(((McpSchema.TextContent) ollamaLlmResponse.content()).text());
						} catch (Exception e) {
							logger.warn("Failed to get Ollama explanation: " + e.getMessage());
							ollamaExplanation.append("Ollama explanation unavailable: " + e.getMessage());
						}
					}

					exchange.loggingNotification(LoggingMessageNotification.builder()
							.level(LoggingLevel.INFO)
							.data("Finish sampling for calculation")
							.build());
				});

		String responseWithExplanations = "## Calculation Result\n" +
				String.format("**%.2f %s %.2f = %.2f**\n\n", 
					calculationResult.a(), getOperationSymbol(calculationResult.operation()), 
					calculationResult.b(), calculationResult.result()) +
				"---\n\n" +
				"### OpenAI Creative Explanation\n" + openAiExplanation.toString() + "\n\n" +
				"---\n\n" +
				"### Ollama Creative Explanation\n" + ollamaExplanation.toString() + "\n\n" +
				"---\n\n" +
				"*Generated using MCP Sampling with OpenAI and Ollama*";

		logger.info("Generated explanations for calculation: {} {} {} = {}", 
			calculationResult.a(), calculationResult.operation(), calculationResult.b(), calculationResult.result());

		return responseWithExplanations;
	}
	
	/**
	 * Get operation symbol for display
	 */
	private String getOperationSymbol(String operation) {
		return switch (operation.toLowerCase().trim()) {
			case "add", "addition" -> "+";
			case "subtract", "subtraction" -> "-";
			case "multiply", "multiplication" -> "×";
			case "divide", "division" -> "÷";
			default -> operation;
		};
	}

}