import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {
    public static void main(String[] args) throws InterruptedException {
        final String token = System.getenv("DISCORD_TOKEN");

        if (token == null) {
            throw new RuntimeException("DISCORD_TOKEN is not set!");
        }

        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        JDA jda = jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new ReadyEventListener(), new MessageListener(), new SlashCommandListener())
                .build();

        OptionData visibility = new OptionData(
                OptionType.STRING,
                "visibility",
                "choose visibility",
                true
        );

        OptionData concepts = new OptionData(
                OptionType.STRING,
                "concepts",
                "chose a concept",
                true
        );

        visibility.addChoice("Public", "public");
        visibility.addChoice("Private", "private");
        visibility.addChoice("Protected", "protected");

        concepts.addChoice("Strings", "strings");
        concepts.addChoice("Integers", "integers");
        concepts.addChoice("Booleans", "booleans");
        concepts.addChoice("Enums", "enums");
        concepts.addChoice("OOP", "oop");
        concepts.addChoice("Recursion", "recursion");
        concepts.addChoice("Iteration", "interation");

        jda.awaitReady();

        // JDA 6.5.0 Global Slash Command Registration
        jda.updateCommands()
                .addCommands(
                        Commands.slash("java", "Intro")
                                .addOption(OptionType.STRING, "username", "simple intro name"   )
                                .setContexts(InteractionContextType.ALL)
                                .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL),

                        Commands.slash("createclass", "Creates A Class")
                                .addOption(OptionType.STRING, "name", " Name of Class", true)
                                .addOptions(visibility)
                                .addOption(OptionType.STRING, "mainmethod", "Want a main method?Y/N", true)
                                .setContexts(InteractionContextType.ALL)
                                .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL),

                        Commands.slash("learn", "learning topics")
                                .addOptions(concepts)
                                .setContexts(InteractionContextType.ALL)
                                .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL),
                        Commands.slash("query", "custom query")
                                .addOption(OptionType.STRING, "query", "query describe", true)
                                .setContexts(InteractionContextType.ALL)
                                .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
                )
                .queue();
    }
}
