package org.icanthink.minigameManager.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class ChatGPTClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Gson gson = new Gson();

    private final HttpClient client;
    private final String apiKey;
    private final String model;

    /**
     * Creates a new ChatGPT client with custom configuration.
     *
     * @param apiKey The OpenAI API key
     * @param model The model to use (e.g. "gpt-3.5-turbo")
     * @param timeoutSeconds Timeout in seconds for API calls
     */
    public ChatGPTClient(String apiKey, String model, int timeoutSeconds) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }

    /**
     * Creates a new ChatGPT client with default configuration.
     *
     * @param config The plugin configuration containing the API key
     */
    public ChatGPTClient(FileConfiguration config) {
        this(
            config.getString("openai.api_key", ""),
            config.getString("openai.model", "gpt-4o-mini"),
            config.getInt("openai.timeout_seconds", 30)
        );
    }

    /**
     * Sends a message to ChatGPT and returns the response asynchronously.
     *
     * @param prompt The message to send
     * @return A CompletableFuture that will contain the response
     */
    public CompletableFuture<String> sendMessage(String prompt) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    String errorBody = response.body();
                    Bukkit.getLogger().log(Level.SEVERE, "ChatGPT API Error:");
                    Bukkit.getLogger().log(Level.SEVERE, "Status Code: " + response.statusCode());
                    Bukkit.getLogger().log(Level.SEVERE, "Response Body: " + errorBody);
                    throw new RuntimeException("ChatGPT API Error: " + response.statusCode() + " - See console for details");
                }

                String responseBody = response.body();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                return jsonResponse
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString()
                    .trim();
            });
    }

    /**
     * Sends a message to ChatGPT and returns the response synchronously.
     *
     * @param prompt The message to send
     * @return The response from ChatGPT
     * @throws RuntimeException if the API call fails
     */
    public String sendMessageSync(String prompt) {
        try {
            return sendMessage(prompt).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get response from ChatGPT", e);
            throw new RuntimeException("Failed to get response from ChatGPT", e);
        }
    }
}