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
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Command(name = "checkout", mixinStandardHelpOptions = true,
        description = "Change to the git brach for the specified Pull Request")
public class CheckoutCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-u", "--username"},
            required = true,
            description = "Git service username")
    String username;

    @CommandLine.Option(
            names = {"-p", "--password"},
            required = true,
            description = "Git service password/token")
    String password;

    @CommandLine.Option(
            names = {"-r", "--request"},
            required = true,
            description = "Pull Request ID")
    String pullRequest;

    @Spec
    CommandSpec spec;

    private final ConnectionManager connectionManager = new ConnectionManager();

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

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

        URLConnection request = connectionManager.openConnection(username, password);

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
                } else {
                    ProcessBuilder builder = new ProcessBuilder();
                    if (IS_WINDOWS) {
                        builder.command("cmd.exe", "/c", "git.exe checkout " + branch);
                    } else {
                        builder.command("sh", "-c", "git checkout " + branch);
                    }

                    // TODO: we should refactor, extract method
                    try {

                        Process process = builder.start();

                        // Block is fine, user is waiting
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            out.println(line);
                        }
                        while ((line = errorReader.readLine()) != null) {
                            out.println(line);
                        }
                        return process.waitFor();

                    } catch (IOException e) {
                        out.println("Checkout failed");
                        return 1;
                    } catch (InterruptedException e) {
                        out.println("Checkout has been cancelled");
                        return 1;
                    }
                }
            }
        }
        return 1;
    }

}

