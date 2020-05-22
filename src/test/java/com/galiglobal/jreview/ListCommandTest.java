package com.galiglobal.jreview;

import jdk.nashorn.internal.ir.annotations.Ignore;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ListCommandTest {
    
    public void testNoCertificates() throws IOException, InterruptedException {

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new ListCommand()).execute("--no-certificates");

            String expected = String.format("" +
                    "****** Content of the URL ********%n" +
                    "security.provider.3=notSunEC%n");

            assertEquals(expected, baos.toString());
            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }

    public void testHeaders() throws IOException, InterruptedException {

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));
            int exitCode = new CommandLine(new ListCommand()).execute("--no-certificates", "--headers");

            String actual = baos.toString();
            assertTrue(actual.contains("null: [HTTP/1.1 200 OK]"));
            assertTrue(actual.contains("Content-Length: [29]"));
            assertTrue(actual.contains("Content-Type: [text/plain; charset=utf-8]"));

            assertEquals(0, exitCode);
        } finally {
            System.setOut(oldOut);
        }
    }

    public void testUnknownOptionGivesExitCode2() {
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(baos));
            int exitCode = new CommandLine(new ListCommand()).execute("--xxx");

            String expected = String.format("" +
                    "Unknown option: '--xxx'%n" +
                    "Usage: https-client [-chHV] [--use-local-keystore] <url>%n" +
                    "Uses https protocol to get a remote resource.%n" +
                    "      <url>                  The URL to download%n" +
                    "  -c, --[no-]certificates    Show server certificates (true by default)%n" +
                    "  -h, --help                 Show this help message and exit.%n" +
                    "  -H, --headers              Print response headers (false by default)%n" +
                    "      --use-local-keystore   Use this when connecting to local SimpleHttpsServer%n" +
                    "  -V, --version              Print version information and exit.%n");

            assertEquals(expected, baos.toString());
            assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        } finally {
            System.setErr(oldErr);
        }
    }

    private PrintWriter devNull() {
        return new PrintWriter(new StringWriter());
    }

}
