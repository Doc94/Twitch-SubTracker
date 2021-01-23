package me.mrdoc.twitch.subtracker;

import me.mrdoc.twitch.subtracker.classes.TwitchInstance;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Core {

    public static Logger LOGGER = LoggerFactory.getLogger(Core.class);
    private static TwitchInstance twitchInstance;

    public static void main(String[] args) {
        CommandLine commandLine = parseInCommandLine(args);

        twitchInstance = new TwitchInstance(commandLine.getOptionValue("twitch-token"),commandLine.getOptionValue("twitch-channelId"));

        //Set de variables si aplica
        if(commandLine.hasOption("subsformId")) {
            twitchInstance.setSubs_formId(commandLine.getOptionValue("subsformId"));
        }
        if(commandLine.hasOption("bitsformId")) {
            twitchInstance.setSubs_formId(commandLine.getOptionValue("bitsformId"));
        }
        if(commandLine.hasOption("rewardsformId") && commandLine.hasOption("rewardId")) {
            twitchInstance.setSubs_formId(commandLine.getOptionValue("rewardsformId"));
            twitchInstance.setReward_id(commandLine.getOptionValue("rewardId"));
        }

        twitchInstance.build();
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

        Option option_subs_formId = new Option("sfi", "subsformId", true, "input google form id (the /asdadasdad/) for subs");
        options.addOption(option_subs_formId);

        Option option_bits_formId = new Option("bfi", "bitsformId", true, "input google form id (the /asdadasdad/) for bits");
        options.addOption(option_bits_formId);

        Option option_rewards_formId = new Option("rfi", "rewardsformId", true, "input google form id (the /asdadasdad/) for rewards");
        options.addOption(option_rewards_formId);

        Option option_reward_Id = new Option("ri", "rewardId", true, "input ID for the reward to track");
        options.addOption(option_reward_Id);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
            return null;
        }

        return cmd;
    }

}
