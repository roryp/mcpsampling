/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.mcp.samples.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring AI MCP Client Application that demonstrates integration with multiple AI providers.
 * 
 * <p>This application uses both OpenAI and Ollama models for MCP sampling requests.
 * When the "ollama" model hint is specified, it routes to the local Ollama server.
 * All other model hints route to OpenAI or other configured providers.
 * 
 * <p><strong>Setup Requirements:</strong>
 * <ul>
 *   <li>Set the OPENAI_API_KEY environment variable for OpenAI integration</li>
 *   <li>Install and run Ollama locally on http://localhost:11434</li>
 *   <li>Pull a model in Ollama (e.g., `ollama pull llama3.2`)</li>
 *   <li>Ensure the calculator MCP server is running on localhost:8080</li>
 * </ul>
 * 
 * <p>The application will automatically route requests with "ollama" model hints to 
 * the local Ollama server and other requests to the configured OpenAI models.
 */
@SpringBootApplication
public class McpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpClientApplication.class, args).close();;
	}

	@Bean
	public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
			List<McpSyncClient> mcpClients) {

		return args -> {

			var mcpToolProvider = new SyncMcpToolCallbackProvider(mcpClients);

			ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultToolCallbacks(mcpToolProvider).build();

			String userQuestion = """
					What is 2+2 and give me the result in EUR?
					Please incorporate all creative responses from all LLM providers.
					After the other providers add a poem that synthesizes the the poems from all the other providers.
					""";

			System.out.println("> USER: " + userQuestion);
			System.out.println("> ASSISTANT: " + chatClient.prompt(userQuestion).call().content());
		};
	}
	@Bean
	McpSyncClientCustomizer samplingCustomizer(Map<String, ChatClient> chatClients, OllamaChatModel ollamaChatModel) {

		return (name, mcpClientSpec) -> {
			
			mcpClientSpec = mcpClientSpec.loggingConsumer(logingMessage -> {			
				System.out.println("MCP LOGGING: [" + logingMessage.level() + "] " + logingMessage.data());			
			});

			mcpClientSpec.sampling(llmRequest -> {
				var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
				String modelHint = llmRequest.modelPreferences().hints().get(0).name();

				if ("ollama".equals(modelHint)) {
					// Use Ollama for this model hint
					ChatClient ollamaChatClient = ChatClient.builder(ollamaChatModel).build();
					
					String response = ollamaChatClient.prompt()
							.system(llmRequest.systemPrompt())
							.user(userPrompt)
							.call()
							.content();

					return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
				} else {
					// Use other chat clients for different model hints (like OpenAI)
					ChatClient hintedChatClient = chatClients.entrySet().stream()
							.filter(e -> e.getKey().contains(modelHint)).findFirst()
							.orElseThrow().getValue();

					String response = hintedChatClient.prompt()
							.system(llmRequest.systemPrompt())
							.user(userPrompt)
							.call()
							.content();

					return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
				}
			});
			System.out.println("Customizing " + name);
		};
	}

	@Bean
	public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {

		return chatModels.stream().collect(Collectors.toMap(model -> model.getClass().getSimpleName().toLowerCase(),
				model -> ChatClient.builder(model).build()));

	}
}