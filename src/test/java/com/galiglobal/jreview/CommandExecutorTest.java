package com.galiglobal.jreview;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandExecutorTest {

    @Test
    public void testExecuteCommand() throws Exception {
        CommandExecutor commandExecutor = new CommandExecutor();
        String output = commandExecutor.execute(" remote get-url origin");
        assertEquals(output, getUrlFromGitConfig());
    }

    String getUrlFromGitConfig() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(".git/config"))) {
            for (String line; (line = br.readLine()) != null; )
                if (line.equals("[remote \"origin\"]")) {
                    break;
                }

            String remote = br.readLine();
            Matcher m = Pattern.compile(".*url = (.*)").matcher(remote);
            m.find();
            return m.group(1);
        }
    }
}
