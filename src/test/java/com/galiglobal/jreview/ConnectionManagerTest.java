package com.galiglobal.jreview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectionManagerTest {

    @Test
    public void testApiUrlBuild() throws Exception {
        ConnectionManager cm = new ConnectionManager();
        String apiUrl = cm.buildApiUrlFromGit("origin");
        assertEquals(apiUrl, "https://dev.azure.com/antonmry/antonmry/_apis/git/repositories/jreview/pullrequests?api-version=5.1");
    }
}
