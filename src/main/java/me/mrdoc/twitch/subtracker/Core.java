package me.mrdoc.twitch.subtracker;

import me.mrdoc.twitch.subtracker.classes.SubInstance;
import org.apache.commons.cli.*;

public class Core {

    private static SubInstance subInstance;

    public static void main(String[] args) {
        CommandLine commandLine = parseInCommandLine(args);

        subInstance = new SubInstance(commandLine.getOptionValue("twitch-token"),commandLine.getOptionValue("twitch-channelId"),commandLine.getOptionValue("formId"));
        subInstance.build();
    }

    /**
     * Parsea argumentos como si fuera un sistema de comandos.
     * @param args Argumentos
     * @return {@code CommandLine}
     */
    private static CommandLine parseInCommandLine(String[] args) {
        Options options = new Options();

        Option option_twitch_token = new Option("tt", "twitch-token", true, "input token of twitch with channel_subscriptions scope");
        option_twitch_token.setRequired(true);
        options.addOption(option_twitch_token);

        Option option_twitch_channelId = new Option("tc", "twitch-channelId", true, "input channelId of twitch");
        option_twitch_channelId.setRequired(true);
        options.addOption(option_twitch_channelId);

        Option option_formId = new Option("fi", "formId", true, "input google form id (the /asdadasdad/)");
        option_formId.setRequired(true);
        options.addOption(option_formId);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
            return null;
        }

        return cmd;
    }

}
