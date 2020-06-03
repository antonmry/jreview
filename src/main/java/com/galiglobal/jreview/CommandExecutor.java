package com.galiglobal.jreview;

import java.io.*;

public class CommandExecutor {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    Integer execute(PrintWriter out, String command) {
        ProcessBuilder builder = new ProcessBuilder();
        if (IS_WINDOWS) {
            builder.command("cmd.exe", "/c", "git.exe " + command);
        } else {
            builder.command("sh", "-c", "git " + command);
        }

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
            out.println("git " + command + "failed");
            return 1;
        } catch (InterruptedException e) {
            out.println("git " + command + "has been cancelled");
            return 1;
        }
    }

    public String execute(String command) {
        ProcessBuilder builder = new ProcessBuilder();
        if (IS_WINDOWS) {
            builder.command("cmd.exe", "/c", "git.exe " + command);
        } else {
            builder.command("sh", "-c", "git " + command);
        }

        try {
            Process process = builder.start();

            // Block is fine, user is waiting
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuffer output = new StringBuffer();
            for (String line; (line = reader.readLine()) != null; ) {
                output.append(line);
            }
            for (String line; (line = errorReader.readLine()) != null; ) {
                output.append(line);
            }
            return output.toString();
        } catch (IOException e) {
            return "git " + command + "failed";
        }
    }
}