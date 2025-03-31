package com.github.mcp.democlient.mcp_demo_client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class McpDemoClientApplication {

    private static Logger log = LoggerFactory.getLogger(McpDemoClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(McpDemoClientApplication.class, args);
    }

    @RestController
    public class DemoClientTestController {

        @Autowired
        private List<McpSyncClient> mcpSyncClients;

        @GetMapping("/test")
        public ResponseEntity<McpSchema.CallToolResult> test() {
            // Use the mcpSyncClients to perform some operations
            // For example, call a method on the first client
            McpSyncClient client = mcpSyncClients.get(0);
            McpSchema.ListToolsResult listToolsResult = client.listTools();
            McpSchema.Tool tool1 = listToolsResult.tools().get(0);
            log.info("Tool: {}", tool1);
            // Call a tool
            McpSchema.CallToolResult result = client.callTool(
                    new McpSchema.CallToolRequest(tool1.name(),
                            Map.of("expression", "1+2")));
            log.info("Tool result {}", result);

            return ResponseEntity.ok(result);
        }

    }

    @Configuration
    @EnableWebMvc
    public static class McpDemoClientConfiguration {

        @Bean
        McpClientTransport httpClientSseClientTransport() {
            return new HttpClientSseClientTransport("http://localhost:8080");
        }

        @Bean
        public List<McpSyncClient> mcpSyncDemoClients(McpClientTransport transport) {

            List<McpSyncClient> mcpSyncClients = new ArrayList<>();

            // Create a sync client with custom configuration
            McpSyncClient client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(20))
                    .capabilities(McpSchema.ClientCapabilities.builder().build()).build();

            // Initialize connection
            client.initialize();

            mcpSyncClients.add(client);

            return mcpSyncClients;
        }

    }

}
