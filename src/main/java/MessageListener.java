import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        System.out.println("Message Received From: "+event.getAuthor() +"\nMessage Was " + event.getMessage().getContentRaw());
        String message = event.getMessage().getContentRaw();

        if(message.equalsIgnoreCase("hello")) {
            event.getChannel().sendMessage("Hello!").queue();
            event.getMessage().reply("Hello! again!").queue();
        }
    }
}
