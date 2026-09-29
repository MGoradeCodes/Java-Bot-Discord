import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.print.attribute.HashDocAttributeSet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SlashCommandListener extends ListenerAdapter {

    private final HashMap<String, String> cachedAnswers = new HashMap<>();
    private final GroqService groq = new GroqService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File cacheFile = new File("data/localcache.json");

    String prompt;
    String answer;

    public SlashCommandListener() {

        cacheFile.getParentFile().mkdirs();

        if (cacheFile.exists()) {
            try {
                cachedAnswers.putAll(
                        objectMapper.readValue(
                                cacheFile,
                                new TypeReference<HashMap<String, String>>() {}
                        )
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        switch (event.getName()) {

            case "createclass":
                ClassHandler(event);
                break;

            case "java":
                HandleJavaCommand(event);
                break;

            case "learn":

                event.deferReply().queue();

                var conceptOption = event.getOption("concepts");


                if (conceptOption == null) {
                    event.getHook()
                            .editOriginal("Error: No concept selected.")
                            .queue();
                    return;
                }

                String ownerID = "1245995882680156199";
                prompt = conceptOption.getAsString();

                answer = HandleAI(prompt, event, false);

                event.getHook().editOriginal(answer).queue();

                break;

            case "query":

                event.deferReply().queue();

                var queryOption = event.getOption("query");

                if (queryOption == null) {
                    event.getHook()
                            .editOriginal("⚠️ Please provide a query.")
                            .queue();
                    return;
                }

                String customized = queryOption.getAsString();

                prompt = ConstPrompt.Prompt + customized;

                answer = HandleAI(prompt, event, false);

                event.getHook().editOriginal(answer).queue();


                break;
            case "viewtokens":
                getTokens(event, groq);
                break;

            case "askwithsearch":
                event.deferReply().queue();
                String raw = event.getOption("query").getAsString();
                prompt = ConstPrompt.Prompt + raw;

                answer = HandleAI(prompt, event, true);

                event.getHook().editOriginal(answer).queue();

                break;

            default:
                break;
        }
    }

    public static void ClassHandler(SlashCommandInteractionEvent event) {

        var nameOpt = event.getOption("name");
        var visOpt = event.getOption("visibility");
        var mainOpt = event.getOption("mainmethod");

        if (nameOpt == null || visOpt == null || mainOpt == null) {
            event.reply("Error: Missing required fields.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String className = nameOpt.getAsString();
        String classVisibility = visOpt.getAsString();
        String main = mainOpt.getAsString();

        String blueprint;

        String locationText =
                event.isFromGuild()
                        ? "Server Context"
                        : "User Install/DM Context";

        if (main.equalsIgnoreCase("Y")) {

            blueprint =
                    "Generated via " + locationText + ":\n```java\n" +
                            classVisibility + " class " + className + " {\n" +
                            "    public static void main(String[] args) {\n" +
                            "        \n" +
                            "    }\n" +
                            "}\n```";

        } else {

            blueprint =
                    "Generated via " + locationText + ":\n```java\n" +
                            classVisibility + " class " + className + " {\n\n" +
                            "}\n```";
        }

        event.reply(blueprint).queue();
    }

    public static void HandleJavaCommand(@NotNull SlashCommandInteractionEvent event) {

        var userOpt = event.getOption("username");

        if (userOpt == null) {
            event.reply("Error: Please provide a username.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String message = userOpt.getAsString();

        event.reply(
                "Hello this is java bot nice to meet you, " + message
        ).queue();
    }

    public static void getTokens(
            SlashCommandInteractionEvent event,
            GroqService groq
    ) {

        event.deferReply().queue();

        String test = groq.ask("Test Message");

        System.out.println(test);

        int[] tokens = groq.returnTokens();

        String message =
                "Current User Input Tokens: " + tokens[0] +
                        "\nUser Output Tokens: " + tokens[1] +
                        "\nTotal Tokens: " + tokens[2];

        System.out.println(message);

        event.getHook()
                .editOriginal(message)
                .queue();
    }


    public String HandleAI(String question, SlashCommandInteractionEvent event, boolean search) {
        event.getHook()
                .editOriginal("☕ Brewing my Java knowledge...(Java is the best.....)")
                .queue();


        if (!cachedAnswers.containsKey(question)) {

            answer = groq.ask(ConstPrompt.Prompt + question);

            cachedAnswers.put(question, answer);

            try {
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValue(cacheFile, cachedAnswers);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            answer = cachedAnswers.get(question);
        }

        if (answer.length() > 1990) {
            answer = answer.substring(0, 1990) + "...";
        }



        if(search){
            try {

                answer = groq.askWithSearch(ConstPrompt.Prompt + question);

                if (answer.length() > 1999) {
                    answer = answer.substring(0, 1990) + "...";
                }

                event.getHook()
                        .editOriginal(answer)
                        .queue();

            } catch (Exception e) {

                e.printStackTrace();

                event.getHook()
                        .editOriginal(
                                "⚠️ AI service is temporarily unavailable. Please try again."
                        )
                        .queue();
            }
        }

        return answer;
    }


}

