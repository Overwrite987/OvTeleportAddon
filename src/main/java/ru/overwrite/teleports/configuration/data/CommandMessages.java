package ru.overwrite.teleports.configuration.data;

public record CommandMessages(
        String incorrectChannel,
        String channelNotSpecified,
        String cancelled,
        String tooMuchTeleporting,
        String reload,
        String unknownArgument,
        String playerNotFound,
        String adminHelp
) {
}
