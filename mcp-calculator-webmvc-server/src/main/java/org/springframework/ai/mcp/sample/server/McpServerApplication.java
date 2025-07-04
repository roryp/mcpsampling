package org.springframework.ai.mcp.sample.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}
	@Bean
	public ToolCallbackProvider calculatorTools(SimpleCalculatorService simpleCalculatorService) {
		return MethodToolCallbackProvider.builder().toolObjects(simpleCalculatorService).build();
	}
}
