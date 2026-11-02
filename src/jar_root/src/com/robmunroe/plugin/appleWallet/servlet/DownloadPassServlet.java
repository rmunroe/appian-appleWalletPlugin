package com.robmunroe.plugin.appleWallet.servlet;


import com.appiancorp.services.ServiceContext;
import com.appiancorp.services.WebServiceContextFactory;
import com.appiancorp.suiteapi.common.ServiceLocator;
import com.appiancorp.suiteapi.servlet.AppianServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DownloadPassServlet extends AppianServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServiceContext context = WebServiceContextFactory.getServiceContext(req);
        DownloadPassHandler.servePass(ServiceLocator.getContentService(context), req, resp);
    }
}
