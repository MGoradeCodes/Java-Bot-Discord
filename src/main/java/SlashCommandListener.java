import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SlashCommandListener extends ListenerAdapter {
    private final HashMap<String, String> cachedAnswers = new HashMap<>();
    private final GeminiService gemini = new GeminiService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File cacheFile = new File("data/localcache.json");

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
                event.getHook()
                        .editOriginal("☕ Brewing my Java knowledge...(Java is the best.....)")
                        .queue();

                var conceptOption = event.getOption("concepts");

                if (conceptOption == null) {
                    event.getHook().editOriginal("Error: No concept selected.").queue();
                    return;
                }

                String ownerID = "1245995882680156199";
                String question = conceptOption.getAsString();


                String answer;

                if (!cachedAnswers.containsKey(question)) {

                    answer = gemini.ask(
                            "You are an experienced Java instructor(Discord Bot AI). Explain the given Java concept with clarity and precision.\n" +
                                    "\n" +
                                    "Rules:\n" +
                                    "- Maximum 400 words (strict).\n" +
                                    "- Keep the explanation concise and compact.\n" +
                                    "- Avoid large gaps, unnecessary line breaks, and verbose formatting.\n" +
                                    "- Explain what it is, why it is used, and how it works.\n" +
                                    "- Include one short Java example if appropriate.\n" +
                                    "- Do not include unrelated information.\n" +
                                    "- Return only the explanation but you can add small funfacts. And keep crediting me only for making the discord bot by adding sentencs like 'Did you know this bot which your're using right now is made by the user @JavaIsCool, just add my reference in funfacts sometimes not all times.\n" +
                                    "- Make sure to respond Java style, this is a java app, so add some coffee emoji but dont use it every time, and the vibe.\n" +
                                    "- Say at end: App Created By <@" + ownerID + ">\n" +
                                    "- Every response shall be respectful. Praise Java a little sometimes, not always (about 25% of the time).\n" +
                                    "\n" +
                                    "Concept: " + question
                    );

                    cachedAnswers.put(question, answer);
                    try {
                        objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValue(cacheFile, cachedAnswers);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                    answer = cachedAnswers.get(question);

                }

                if (answer.length() > 1990) {
                    answer = answer.substring(0, 1990) + "...";
                }

                event.getHook().editOriginal(answer).queue();

                break;

            case "query":
                event.deferReply().queue(); // acknowledge immediately

                String customized = event.getOption("query").getAsString();

                String prompt = "You are a a discord bot named JavaBot, and you were made by user @JavaIsCool. Answer directly and keep it under 250 words.\n\nQuestion: "
                        + customized;

                try {
                    String reply = gemini.ask(prompt);

                    event.getHook().editOriginal(reply).queue();

                } catch (Exception e) {
                    e.printStackTrace();

                    event.getHook().editOriginal(
                            "⚠️ AI service is temporarily unavailable. Please try again."
                    ).queue();
                }

                break;

            default:
                break;
        }
    }


    public static void ClassHandler(SlashCommandInteractionEvent event) {
        // Safe option retrieval
        var nameOpt = event.getOption("name");
        var visOpt = event.getOption("visibility");
        var mainOpt = event.getOption("mainmethod");

        if (nameOpt == null || visOpt == null || mainOpt == null) {
            event.reply("Error: Missing required fields.").setEphemeral(true).queue();
            return;
        }

        String className = nameOpt.getAsString();
        String classVisibility = visOpt.getAsString();
        String main = mainOpt.getAsString();
        String blueprint;

        // Context evaluation (Safe for User Install / Guild Install anywhere)
        String locationText = event.isFromGuild() ? "Server Context" : "User Install/DM Context";

        if (main.equalsIgnoreCase("Y")) {
            blueprint = "Generated via " + locationText + ":\n```java\n" + classVisibility + " class " + className + " {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        \n" +
                    "    }\n" +
                    "}\n```";
        } else {
            blueprint = "Generated via " + locationText + ":\n```java\n" + classVisibility + " class " + className + " {\n\n}\n```";
        }

        event.reply(blueprint).queue();
    }

    public static void HandleJavaCommand(SlashCommandInteractionEvent event) {
        var userOpt = event.getOption("username");
        if (userOpt == null) {
            event.reply("Error: Please provide a username.").setEphemeral(true).queue();
            return;
        }

        String message = userOpt.getAsString();
        event.reply("Hello this is java bot nice to meet you, " + message).queue();
    }
}
