# Spring AI MCP Sampling Examples

This project demonstrates the Model Context Protocol (MCP) Sampling capability in Spring AI, showcasing how an MCP server can delegate requests to multiple LLM providers for creative content generation.

## 🚀 Quick Start

### Prerequisites

- Java 17 or later
- Maven 3.6+
- OpenAI API key
- GitHub Models API key (for GPT-4o-mini access)

### Environment Setup

```bash
export OPENAI_API_KEY=your-openai-key-here
export GITHUB_TOKEN=your-github-token-here  # For GitHub Models access
```

### Running the Application

1. **Start the MCP Calculator Server**
   ```bash
   cd mcp-calculator-webmvc-server
   ./mvnw spring-boot:run
   ```

2. **Run the MCP Sampling Client** (in another terminal)
   ```bash
   cd mcp-sampling-client
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--ai.user.input='Calculate 15 + 27 and explain it creatively'"
   ```

## 📁 Project Structure

```
├── mcp-calculator-webmvc-server/     # MCP Server with calculator operations
├── mcp-sampling-client/              # MCP Client with sampling capabilities
├── README.md                         # This file
└── .gitignore                       # Git ignore rules
```

## 🛠 Features

- **Calculator Operations**: Add, subtract, multiply, divide
- **Currency Conversion**: Real-time exchange rates via API
- **MCP Sampling**: Delegate creative explanations to multiple LLM providers
- **Multi-Model Support**: OpenAI and GitHub Models (GPT-4o-mini) integration
- **Timeout Protection**: Robust error handling for external API calls

## 🔧 Development

### Building the Projects

```bash
# Build calculator server
cd mcp-calculator-webmvc-server
./mvnw clean install

# Build sampling client
cd ../mcp-sampling-client
./mvnw clean install
```

## 📄 License

This project is part of the Spring AI examples and follows the Spring AI licensing terms.

## 🔗 Links

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
