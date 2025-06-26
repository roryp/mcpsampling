# MCP Sampling Demo - Spring AI Model Context Protocol

A comprehensive demonstration of the Model Context Protocol (MCP) using Spring AI with multiple AI providers including OpenAI and Ollama. This project showcases how to build both MCP servers and clients that can route requests to different Large Language Models (LLMs) based on model preferences.

![sequence](./sequence.png)

## 🏗️ Project Structure

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

## 🏃 Running the Applications

### Step 1: Start the MCP Server

```bash
cd mcp-calculator-webmvc-server
java -jar target/mcp-calculator-webmvc-server-0.0.1-SNAPSHOT.jar
```

The server will start on `http://localhost:8080` and provide:
- Calculator tools (add, subtract, multiply, divide)
- Currency conversion with real-time exchange rates
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
2. Ask: "What is 2+2 and give me the result in EUR?"
3. Route different parts of the response to different AI models
4. Combine creative responses from both OpenAI and Ollama

## 🛠️ Available Tools

The MCP server provides the following tools:

### Calculator Operations
- **`add`** - Addition of two numbers
- **`subtract`** - Subtraction of two numbers  
- **`multiply`** - Multiplication of two numbers
- **`divide`** - Division of two numbers (with zero-division protection)

### Currency Conversion
- **`convertCurrency`** - Convert amounts between currencies using live exchange rates

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


## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.