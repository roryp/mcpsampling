# MCP Sampling Demo

A comprehensive demonstration of the Model Context Protocol (MCP) using Spring AI with multiple AI providers including OpenAI and Ollama. This project showcases how to build both MCP servers and clients that can route requests to different Large Language Models (LLMs) based on model preferences.

![sequence](./sequence.png)

## � Key Concepts

### What is MCP Sampling?
MCP (Model Context Protocol) sampling allows servers to request LLM completions from clients during tool execution. This enables:
- **Dynamic content generation** - Servers can request creative explanations from different models
- **Model routing** - Clients can route requests to different LLMs based on hints
- **Enhanced responses** - Combine computational results with AI-generated narratives

### How It Works
1. **Client** calls an MCP tool (e.g., calculator)
2. **Server** performs the computation
3. **Server** requests creative explanations via sampling
4. **Client** routes sampling requests to appropriate LLMs
5. **Server** combines results with AI explanations
6. **Client** receives enriched response

## �🏗️ Project Structure

This repository contains two main components:

- **`mcp-calculator-webmvc-server/`** - MCP server that provides calculator tools with creative AI-generated explanations
- **`mcp-sampling-client/`** - MCP client that demonstrates routing requests to different AI providers

## 🚀 Prerequisites

Before running this project, ensure you have the following:

### Required Software
- **Java 17+** - Required for Spring Boot 3.x
- **Maven 3.8+** - For building the projects
- **Git** - For cloning the repository

### Required AI Services

#### 1. OpenAI API Token
You'll need an OpenAI API key to use GPT models:

1. Visit [OpenAI API Keys](https://platform.openai.com/api-keys)
2. Create a new API key
3. Set the environment variable:
   ```bash
   export OPENAI_API_KEY=your-openai-api-key-here
   ```

#### 2. Ollama (Local LLM)
Install and configure Ollama for local model inference:

1. **Install Ollama:**
   - **Windows:** Download from [ollama.com](https://ollama.com/download)
   - **macOS:** `brew install ollama`
   - **Linux:** `curl -fsSL https://ollama.com/install.sh | sh`

2. **Start Ollama service:**
   ```bash
   ollama serve
   ```

3. **Pull a model** (recommended):
   ```bash
   ollama pull llama3.2
   # or
   ollama pull mistral
   ```

4. **Verify installation:**
   ```bash
   ollama list
   ```

   Ollama will run on `http://localhost:11434` by default.

## 📦 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mcpsampling
```

### 2. Set Environment Variables
Create a `.env` file or set these environment variables:

```bash
# Required for OpenAI integration
export OPENAI_API_KEY=your-openai-api-key-here

# Optional: Customize Ollama endpoint (default: http://localhost:11434)
export OLLAMA_BASE_URL=http://localhost:11434
```

### 3. Build the Projects
```bash
# Build the MCP server
cd mcp-calculator-webmvc-server
./mvnw clean install -DskipTests

# Build the MCP client
cd ../mcp-sampling-client
./mvnw clean install -DskipTests
```

### Quick Start Script
For training sessions, use this script to quickly set up everything:

```bash
#!/bin/bash
# save as: quick-start.sh

echo "🚀 MCP Sampling Demo Quick Start"

# Check prerequisites
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17+"
    exit 1
fi

if [ -z "$OPENAI_API_KEY" ]; then
    echo "❌ OPENAI_API_KEY not set"
    echo "Run: export OPENAI_API_KEY=your-key-here"
    exit 1
fi

# Start Ollama
echo "🤖 Starting Ollama..."
ollama serve &
OLLAMA_PID=$!
sleep 5

# Build projects
echo "🔨 Building projects..."
cd mcp-calculator-webmvc-server && ./mvnw clean install -DskipTests
cd ../mcp-sampling-client && ./mvnw clean install -DskipTests

# Start server
echo "🖥️ Starting MCP server..."
cd ../mcp-calculator-webmvc-server
java -jar target/*.jar &
SERVER_PID=$!
sleep 10

# Run client
echo "💬 Running MCP client..."
cd ../mcp-sampling-client
java -jar target/*.jar

# Cleanup
kill $SERVER_PID $OLLAMA_PID
```

## 🏃 Running the Applications

### Step 1: Start the MCP Server

```bash
cd mcp-calculator-webmvc-server
java -jar target/mcp-calculator-webmvc-server-0.0.1-SNAPSHOT.jar
```

The server will start on `http://localhost:8080` and provide:
- Calculator tools (add, subtract, multiply, divide)
- Creative AI-generated explanations using multiple models

### Step 2: Start Ollama (if not already running)

```bash
ollama serve
```

### Step 3: Run the MCP Client

```bash
cd mcp-sampling-client
java -jar target/mcp-sampling-client-0.0.1-SNAPSHOT.jar
```

The client will:
1. Connect to the MCP server
2. Ask: "What is 2+2?"
3. Route different parts of the response to different AI models
4. Combine creative responses from both OpenAI and Ollama

## 📋 Example Output

When you run the client, you'll see output similar to:

```
> USER: What is 2+2?
Please incorporate all creative responses from all LLM providers.
After the other providers add a poem that synthesizes the the poems from all the other providers.

MCP LOGGING: [info] Received sampling request with model hint: openai
MCP LOGGING: [info] Received sampling request with model hint: ollama

> ASSISTANT: Let me calculate 2+2 for you.

The answer is 4.

**OpenAI's Creative Explanation:**
In the realm where numbers dance and play,
Two plus two finds its way.
Four emerges, strong and true,
A mathematical breakthrough!

**Ollama's Creative Explanation:**
Mathematics whispers its ancient song,
Where two pairs unite, they belong.
The sum of four stands proud and bright,
A beacon in the numeric light.

**Synthesized Poem:**
From silicon minds, both near and far,
OpenAI and Ollama, like twin stars.
They sing of numbers in harmonious rhyme,
Four units dancing through space and time.
Together they weave this numerical tale,
Where math and poetry never fail.
```

## 🛠️ Available Tools

The MCP server provides the following tools:

### Calculator Operations
- **`add`** - Addition of two numbers
- **`subtract`** - Subtraction of two numbers  
- **`multiply`** - Multiplication of two numbers
- **`divide`** - Division of two numbers (with zero-division protection)

## 💻 Code Walkthrough

### Server-Side Sampling Request
The server initiates sampling when it needs creative content:

```java
// In SimpleCalculatorService.java
String explanation = mcpToolCallProvider.sampleModel(
    "Generate a creative explanation...",
    "openai"  // Model hint
).text();
```

### Client-Side Sampling Handler
The client's `samplingCustomizer` routes requests to appropriate models:

```java
@Bean
McpSyncClientCustomizer samplingCustomizer(...) {
    return (name, mcpClientSpec) -> {
        mcpClientSpec.sampling(llmRequest -> {
            String modelHint = llmRequest.modelPreferences().hints().get(0).name();
            
            if ("ollama".equals(modelHint)) {
                // Route to Ollama
            } else {
                // Route to OpenAI or other providers
            }
        });
    };
}
```

### Key Points for Training:
- **Model hints** determine routing (e.g., "ollama", "openai")
- **Sampling is bidirectional** - server can request from client
- **Responses are enriched** - combine computation with creativity

## 🚨 Troubleshooting

### Common Issues

#### "OpenAI API key not found"
```bash
# Make sure the environment variable is set
echo $OPENAI_API_KEY

# If empty, set it:
export OPENAI_API_KEY=your-key-here
```

#### "Cannot connect to Ollama"
```bash
# Check if Ollama is running
curl http://localhost:11434/api/version

# If not running, start it:
ollama serve

# Check if you have models installed:
ollama list
```

#### "Connection refused to localhost:8080"
- Ensure the MCP server is running first
- Check server logs for any startup errors
- Verify port 8080 is available

## 📚 Learn More

### Documentation
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Ollama Documentation](https://ollama.com/docs)

## 🎓 Training Exercises

### Exercise 1: Add a New Model Provider
Try adding support for another LLM provider (e.g., Anthropic Claude):
1. Add the dependency in `pom.xml`
2. Configure the API key
3. Update the `samplingCustomizer` to handle "claude" hints

### Exercise 2: Custom Sampling Prompts
Modify the server to request different types of creative content:
- Technical explanations
- Jokes about the calculation
- Historical facts about numbers

### Exercise 3: Implement Caching
Add caching to avoid repeated sampling requests:
- Cache responses by prompt + model hint
- Implement TTL for cache entries

### Exercise 4: Error Handling
Enhance error handling for:
- Model provider failures
- Network timeouts
- Invalid model hints


## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
