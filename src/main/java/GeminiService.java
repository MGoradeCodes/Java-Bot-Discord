import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiService {

    private final Client client;

    public GeminiService() {
        client = Client.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();
    }

    public String ask(String prompt) {

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3-flash-preview",
                        prompt,
                        null
                );

        return response.text();
    }
}