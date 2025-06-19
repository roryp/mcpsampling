package org.springframework.ai.mcp.samples.client;

import java.util.Arrays;
import java.util.List;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring AI MCP Client Application that demonstrates integration with GitHub
 * Models.
 * 
 * <p>
 * This application uses the Azure AI Inference library to connect to GitHub
 * Models
 * when the "github" model hint is specified in MCP sampling requests.
 * 
 * <p>
 * <strong>Setup Requirements:</strong>
 * <ul>
 * <li>Set the GITHUB_TOKEN environment variable with your GitHub Personal
 * Access Token</li>
 * <li>Set the OPENAI_API_KEY environment variable for OpenAI integration</li>
 * <li>Ensure the calculator MCP server is running on localhost:8080</li>
 * </ul>
 * 
 * <p>
 * The application will automatically route requests with "github" model hints
 * to
 * GitHub Models (GPT-4o-mini) and other requests to the configured OpenAI
 * models.
 */

@SpringBootApplication
public class McpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpClientApplication.class, args).close();
		;
	}

	@Bean
	public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
			List<McpSyncClient> mcpClients) {

		return args -> {

			var mcpToolProvider = new SyncMcpToolCallbackProvider(mcpClients);

			ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultToolCallbacks(mcpToolProvider).build();

			String userQuestion = """
					Perform 2+2 and convert to EUR and then generate creative explanations using multiple AI providers
					""";

			System.out.println("> USER: " + userQuestion);
			System.out.println("> ASSISTANT: " + chatClient.prompt(userQuestion).call().content());
		};
	}

	@Bean
	McpSyncClientCustomizer samplingCustomizer(OpenAiChatModel openAiChatModel) {

		return (name, mcpClientSpec) -> {

			mcpClientSpec = mcpClientSpec.loggingConsumer(logingMessage -> {
				System.out.println("MCP LOGGING: [" + logingMessage.level() + "] " + logingMessage.data());
			});

			mcpClientSpec.sampling(llmRequest -> {
				var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
				String modelHint = llmRequest.modelPreferences().hints().get(0).name();

				if ("github".equals(modelHint)) {
					// Use actual GitHub Models with Azure AI Inference
					String githubToken = Configuration.getGlobalConfiguration().get("GITHUB_TOKEN");
					String endpoint = "https://models.github.ai/inference";
					String model = "openai/gpt-4o-mini";

					try {
						ChatCompletionsClient client = new ChatCompletionsClientBuilder()
								.credential(new AzureKeyCredential(githubToken))
								.endpoint(endpoint)
								.buildClient();

						List<ChatRequestMessage> chatMessages = Arrays.asList(
								new ChatRequestSystemMessage(llmRequest.systemPrompt()),
								new ChatRequestUserMessage(userPrompt));

						ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
						chatCompletionsOptions.setModel(model);
						ChatCompletions completions = client.complete(chatCompletionsOptions);
						String response = completions.getChoices().get(0).getMessage().getContent();

						return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
					} catch (Exception e) {
						// Fallback in case of error
						String fallbackResponse = "**GitHub Models (GPT-4o-mini) Error:**\n\n" +
								"Unable to connect to GitHub Models: " + e.getMessage() +
								"\n\nPlease ensure GITHUB_TOKEN environment variable is set correctly.";
						return CreateMessageResult.builder().content(new McpSchema.TextContent(fallbackResponse))
								.build();
					}
				} else {
					// Use OpenAI for all other model hints
					ChatClient openAiChatClient = ChatClient.builder(openAiChatModel).build();

					String response = openAiChatClient.prompt()
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
}