package com.galiglobal.jreview;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionManager {

    URLConnection openConnection(String username, String password) throws Exception {
        // TODO. We should add retries
        URL url = new URL(buildUrlFromGitConfig());
        URLConnection request = url.openConnection();
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
        request.setRequestProperty("Authorization", basicAuth);
        request.connect();
        return request;
    }

    String buildUrlFromGitConfig() throws Exception {
        // TODO: we should use git command
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