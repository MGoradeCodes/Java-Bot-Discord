import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class PrefixListener extends ListenerAdapter {

    private final String prefix = "q";

    GroqService groq = new GroqService();

    String answer = "";

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
            answer = groq.ask(ConstPrompt.Prompt + question);
            if (answer.length() > 1990) {
                answer = answer.substring(0, 1990) + "...";
            }
            placeholderMessage.editMessage(answer).queue();

            if (search) {
                try {
                    answer = groq.askWithSearch(ConstPrompt.Prompt + question +  "\n RMEMINDER STRICT < 2000 CHARACTER DO NOT VIOLATE");

                    if (answer.length() > 1999) {
                        answer = answer.substring(0, 1990) + "...";
                    }

                    placeholderMessage.editMessage(answer).queue();

                    DiscordBot.Debug(answer);

                } catch (Exception e) {
                    e.printStackTrace();

                    placeholderMessage.editMessage("⚠️ AI service is temporarily unavailable. Please try again.").queue();
                }
            }
        }).start();
    }
}
