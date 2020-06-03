package com.galiglobal.jreview;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckoutCommandTest {

    private String username = System.getenv("JREVIEW_USERNAME");
    private String token = System.getenv("JREVIEW_TOKEN");
    private String originalBranch;

    @BeforeEach
    public void setup() {
        CommandExecutor commandExecutor = new CommandExecutor();
        String output = commandExecutor.execute("branch");
        Matcher m = Pattern.compile(".*\\* (\\w+).*").matcher(output);
        m.find();
        originalBranch = m.group(1);
        System.out.println(commandExecutor.execute("stash"));
    }

    @AfterEach
    public void restore() {
        CommandExecutor commandExecutor = new CommandExecutor();
        System.out.println(commandExecutor.execute("checkout " + originalBranch));
        System.out.println(commandExecutor.execute("stash pop"));
    }

    @Test
    public void testCheckoutSuccessful() {

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new CheckoutCommand()).execute("-u ", username, "-t ", token,
                    "-r", "origin", "-p", "6");

            String expected = String.format("" +
                    "Your branch is up to date with 'origin/test/pr2'." + "\n" +
                    "Switched to branch 'test/pr2'"
            );


            assertEquals(expected.trim(), baos.toString().trim());
            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }
}
