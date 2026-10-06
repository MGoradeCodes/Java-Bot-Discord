import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.HashMap;

public class PrefixListener extends ListenerAdapter {

    private final String prefix = "q";

    GroqService groq = new GroqService();

    String answer;
    String prev;

    private final HashMap<String, String> cachedAnswers = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();

        if(!message.startsWith(prefix)) return;
        String content = message.substring(prefix.length()).trim();

        if (content.isEmpty()) return;

        if(content.contains("/s")){
            event.getChannel().sendMessage("Thinking... 🤖").queue(placeholderMessage -> {
                handleAI(content, event, placeholderMessage, true);
            });
        }

        event.getChannel().sendMessage("Thinking... 🤖").queue(placeholderMessage -> {
            handleAI(content, event, placeholderMessage, false);
        });



    }

    public void handleAI(String question, MessageReceivedEvent event, Message placeholderMessage, boolean search) {
        new Thread(() -> {
            if (search) {
                try {
                    answer = groq.askWithSearch(ConstPrompt.Prompt + question + "\n REMINDER STRICT < 2000 CHARACTER DO NOT VIOLATE");

                    if (answer.length() > 1990) {
                        answer = answer.substring(0, 1990) + "...";
                    }

                    placeholderMessage.editMessage(answer).queue();
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                    placeholderMessage.editMessage("⚠️ AI service is temporarily unavailable. Please try again.").queue();
                    return;
                }
            }

            if (!cachedAnswers.containsKey(question)) {

                answer = groq.ask("Context till now: (ignore if empty) { : \n" + prev + " } \n Now once context is done read this prompt and answer: " + ConstPrompt.Prompt + question);

                cachedAnswers.put(question, answer);

                prev = "Previous Context: \n This was question asked previously: " + question + "\n and this is what you had answered: " + answer;

            } else {
                answer = cachedAnswers.get(question);
            }

            if (answer.length() > 1990) {
                answer = answer.substring(0, 1990) + "...";
            }

            placeholderMessage.editMessage(answer).queue();

        }).start();
    }
}
