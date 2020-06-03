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

import java.io.*;
import java.net.URLConnection;
import java.util.concurrent.Callable;

@Command(name = "list", mixinStandardHelpOptions = true,
        description = "List Pull Requests available for the repository where jreview is executed")
public class ListCommand implements Callable<Integer> {

    private final ConnectionManager connectionManager = new ConnectionManager();

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

    @Spec
    CommandSpec spec;

    public static void main(String[] args) {
        int exitCode;
        try (AnsiConsole console = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new ListCommand()).execute(args);
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
                value.forEach(v -> out.println(formatOutput(v)));
            }
        }
        return 0;
    }

    private String formatOutput(JsonElement v) {
        return ("#" + v.getAsJsonObject().getAsJsonPrimitive("pullRequestId") +
                "\t" + deleteQuotes(v.getAsJsonObject().getAsJsonPrimitive("title").toString()) +
                "\t" + v.getAsJsonObject().getAsJsonPrimitive("status")
                .toString().replace("\"", "") +
                "\t" + deleteQuotes(v.getAsJsonObject().getAsJsonObject("createdBy")
                .getAsJsonPrimitive("displayName").toString()));
    }

    private String deleteQuotes(String sentence) {
        return sentence.substring(1, sentence.length() - 1);
    }
}

