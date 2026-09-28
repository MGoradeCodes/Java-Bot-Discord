import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GroqService {

    private static final String MODEL = "openai/gpt-oss-120b";
    private static final String BASE_URL = "https://api.groq.com/openai/v1";

    private final OpenAIClient client;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final int[] tokens = new int[3]; // [prompt, completion, total]

    public GroqService() {
        client = OpenAIOkHttpClient.builder()
                .apiKey(getApiKey())
                .baseUrl(BASE_URL)
                .build();

        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }


    public String ask(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addUserMessage(prompt)
                .build();

        ChatCompletion response = client.chat().completions().create(params);

        updateTokens(
                response.usage().map(u -> (int) u.promptTokens()).orElse(0),
                response.usage().map(u -> (int) u.completionTokens()).orElse(0),
                response.usage().map(u -> (int) u.totalTokens()).orElse(0)
        );

        return response.choices().get(0).message().content().orElse("");
    }

    public String askWithSearch(String prompt) {
        try {
            String responseBody = sendSearchRequest(prompt);
            return parseSearchResponse(responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform Groq browser search request.", e);
        }
    }



    private String sendSearchRequest(String prompt) throws Exception {
        SearchRequest body = new SearchRequest(
                MODEL,
                new Message[]{ new Message("user", prompt) },
                "required",
                new Tool[]{ new Tool("browser_search") }
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/chat/completions"))
                .header("Authorization", "Bearer " + getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Groq API error (" + response.statusCode() + "): " + response.body()
            );
        }

        return response.body();
    }

    private String parseSearchResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        JsonNode usage = root.path("usage");
        updateTokens(
                usage.path("prompt_tokens").asInt(0),
                usage.path("completion_tokens").asInt(0),
                usage.path("total_tokens").asInt(0)
        );

        return root.path("choices").path(0).path("message").path("content").asText();
    }



    private static String getApiKey() {
        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY environment variable is not set.");
        }
        return apiKey;
    }

    private void updateTokens(int prompt, int completion, int total) {
        tokens[0] = prompt;
        tokens[1] = completion;
        tokens[2] = total;
    }

    public int[] returnTokens() {
        return tokens.clone();
    }


    private record SearchRequest(
            String model,
            Message[] messages,
            String tool_choice,
            Tool[] tools
    ) {}

    private record Message(String role, String content) {}

    private record Tool(String type) {}
}