package com.galiglobal.jreview;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.jansi.graalvm.AnsiConsole;

import java.util.concurrent.Callable;

@Command(name = "jreview", mixinStandardHelpOptions = true,
        version = "jreview 4.0",
        subcommands = {
                ListCommand.class,
                HelpCommand.class},
        description = "Manage Pull Requests from the command-line")
public class JReview implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode;
        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new JReview()).execute(args);
        }
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // TODO: call help?
        return 0;
    }
}
