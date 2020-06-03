package com.galiglobal.jreview;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListCommandTest {

    private String username = System.getenv("JREVIEW_USERNAME");
    private String token = System.getenv("JREVIEW_TOKEN");

    @Test
    public void testListSuccessful() throws IOException, InterruptedException {

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new ListCommand()).execute("-u ", username, "-t ", token,
                    "-r", "origin");

            String expected = String.format("" +
                    "#6	Test 2	active	Antón María Rodriguez Yuste" + "\n" +
                    "#5	Test 1	active	Antón María Rodriguez Yuste" + "\n"
            );

            assertEquals(expected.trim(), baos.toString().trim());
            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }
}
