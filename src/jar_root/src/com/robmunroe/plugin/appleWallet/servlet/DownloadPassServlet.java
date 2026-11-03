package com.robmunroe.plugin.appleWallet.servlet;


import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.servlet.AppianServlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DownloadPassServlet extends AppianServlet {

    @Inject
    private ContentService contentService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DownloadPassHandler.servePass(contentService, req, resp);
    }
}
