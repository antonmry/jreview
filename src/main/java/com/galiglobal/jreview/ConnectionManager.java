package com.galiglobal.jreview;

import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionManager {

    URLConnection openConnection(String username, String password, String remote) throws Exception {
        // TODO. We should add retries
        URL url = new URL(buildApiUrlFromGit(remote));
        URLConnection request = url.openConnection();
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
        request.setRequestProperty("Authorization", basicAuth);
        request.connect();
        return request;
    }

    String buildApiUrlFromGit(String remote) throws Exception {
        CommandExecutor commandExecutor = new CommandExecutor();
        String line = commandExecutor.execute("remote get-url " + remote);

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
    }
}
