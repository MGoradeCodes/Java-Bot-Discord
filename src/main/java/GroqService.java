import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class GroqService {

    private final OpenAIClient client;
    private int[] Tokens = new int[3];

    public GroqService() {
        client = OpenAIOkHttpClient.builder()
                .apiKey(System.getenv("GROQ_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
    }

    public String ask(String prompt) {

        ChatCompletionCreateParams params =
                ChatCompletionCreateParams.builder()
                        .model("openai/gpt-oss-120b")
                        .addUserMessage(prompt)
                        .build();

        ChatCompletion response =
                client.chat().completions().create(params);

        var usage = response.usage();

        Tokens[0] = usage.map(u -> (int) u.promptTokens()).orElse(0);
        Tokens[1] = usage.map(u -> (int) u.completionTokens()).orElse(0);
        Tokens[2] = usage.map(u -> (int) u.totalTokens()).orElse(0);

        return response.choices().get(0)
                .message()
                .content()
                .orElse("");
    }

    public int[] returnTokens() {
        return Tokens;
    }
}