package com.galiglobal.jreview;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import picocli.jansi.graalvm.AnsiConsole;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.concurrent.Callable;

@Command(name = "checkout", mixinStandardHelpOptions = true,
        description = "Change to the git brach for the specified Pull Request")
public class CheckoutCommand implements Callable<Integer> {

    private final CommandExecutor commandExecutor = new CommandExecutor();
    @CommandLine.Option(
            names = {"-u", "--username"},
            required = true,
            description = "Git service username")
    String username;

    @CommandLine.Option(
            names = {"-t", "--token"},
            required = true,
            description = "Git service password/token")
    String token;

    @CommandLine.Option(
            names = {"-r", "--remote"},
            defaultValue = "origin",
            description = "Git remote with the Pull Request")
    String remote;

    @CommandLine.Option(
            names = {"-p", "--pullrequest"},
            required = true,
            description = "Pull Request ID")
    String pullRequest;

    @Spec
    CommandSpec spec;

    private final ConnectionManager connectionManager = new ConnectionManager();

    public static void main(String[] args) {
        int exitCode;
        try (AnsiConsole console = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new CheckoutCommand()).execute(args);
        }
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        PrintWriter out = spec.commandLine().getOut();

        URLConnection request = connectionManager.openConnection(username, token, remote);

        try (InputStream in = (InputStream) request.getContent();
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            JsonElement jsonTree = JsonParser.parseReader(br);
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();
                JsonArray value = jsonObject.getAsJsonArray("value");
                String branch = "";
                for (JsonElement v : value) {
                    if (v.getAsJsonObject().getAsJsonPrimitive("pullRequestId").getAsInt() == Integer.valueOf(pullRequest)) {
                        branch = v.getAsJsonObject()
                                .getAsJsonPrimitive("sourceRefName").getAsString()
                                .replace("refs/heads/", "");
                    }
                }
                if (branch.isEmpty()) {
                    out.println("Pull Request not found");
                    return 1;
                }

                return commandExecutor.execute(out, "checkout " + branch);
            }
        }
        return 1;
    }
}

