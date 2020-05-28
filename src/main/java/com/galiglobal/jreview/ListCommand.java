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
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(name = "list", mixinStandardHelpOptions = true,
        description = "List Pull Requests available for the repository where jreview is executed")
public class ListCommand implements Callable<Integer> {

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

    @Spec
    CommandSpec spec;

    public static void main(String[] args) {
        int exitCode;
        try (AnsiConsole console = AnsiConsole.windowsInstall()) {
            exitCode = new CommandLine(new ListCommand()).execute(args);
        }
        System.exit(exitCode);
    }

    public Integer call() throws Exception {
        PrintWriter out = spec.commandLine().getOut();

        URL url = new URL(buildUrlFromGitConfig());
        URLConnection request = url.openConnection();
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
        request.setRequestProperty("Authorization", basicAuth);
        request.connect();

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
                " " + v.getAsJsonObject().getAsJsonPrimitive("title") +
                " " + v.getAsJsonObject().getAsJsonPrimitive("status")
                .toString().replace("\"", "") +
                " " + v.getAsJsonObject().getAsJsonObject("createdBy")
                .getAsJsonPrimitive("displayName"));
    }

    private String buildUrlFromGitConfig() throws Exception {
        // TODO: we should search the file recursively
        try (BufferedReader br = new BufferedReader(new FileReader(".git/config"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // TODO: remote should be a parameter
                if (line.equals("[remote \"origin\"]")) {
                    break;
                }
            }
            line = br.readLine();
            // TODO: this should work also for SSH
            Matcher m = Pattern.compile(".*dev.azure.com/(.*)/(.*)/_git/(.*)").matcher(line);
            if (m.find()) {
                String organization = m.group(1);
                String project = m.group(2);
                String repository = m.group(3);
                return "https://dev.azure.com/" + organization + "/" + project + "/_apis/git/repositories/" +
                        repository + "/pullrequests?api-version=5.1";
            } else {
                throw new Exception("Remote URL not supported");
            }
        } catch (Exception e) {
            throw new Exception("Run this command in the root folder of a git project");
        }
    }
}

