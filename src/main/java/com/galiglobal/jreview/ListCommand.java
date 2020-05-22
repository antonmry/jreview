package com.galiglobal.jreview;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.jansi.graalvm.AnsiConsole;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

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

    private static final String DEFAULT_URL = "https://dev.azure.com/antonmry/antonmry/_apis/git/repositories/antonmry/pullrequests?api-version=5.1";

    @Parameters(description = "The URL to download", defaultValue = DEFAULT_URL)
    String sUrl;
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

        URL url = new URL(sUrl);
        URLConnection request = url.openConnection();
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
        request.setRequestProperty("Authorization", basicAuth);
        request.connect();

        out.println("****** Content of the URL ********");

        try (InputStream in = (InputStream) request.getContent();
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            JsonElement jsonObject = JsonParser.parseReader(br);
            out.println(jsonObject.toString());

        }
        return 0;
    }
}

