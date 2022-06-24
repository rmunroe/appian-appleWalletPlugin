package com.robmunroe.plugin.appleWallet.servlet;


import com.appiancorp.services.ServiceContext;
import com.appiancorp.services.WebServiceContextFactory;
import com.appiancorp.suiteapi.common.ServiceLocator;
import com.appiancorp.suiteapi.common.exceptions.AppianException;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.servlet.AppianServlet;
import com.robmunroe.plugin.appleWallet.helpers.ContentHelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class DownloadPassServlet extends AppianServlet {
//    private static final Logger LOG = Logger.getLogger(DownloadPassServlet.class);

    private static final String PASS_CONTENT_TYPE = "application/vnd.apple.pkpass";
    private static final String DOC_ID_PARAM = "id";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServiceContext context = WebServiceContextFactory.getServiceContext(req);
        ContentService contentService = ServiceLocator.getContentService(context);

        Long documentId = contentService.getIdByUuid(req.getParameter(DOC_ID_PARAM));

        // Get File from Long documentId
        File file;
        try {
            file = new File(ContentHelper.getPhysicalFilePath(contentService, documentId));
        } catch (AppianException e) {
            return;
        }

        // Set the content type
        resp.setContentType(PASS_CONTENT_TYPE);

        try (
                ServletOutputStream output = resp.getOutputStream();
                InputStream input = Files.newInputStream(file.toPath())
        ) {
            // transfer input stream to output stream, via a buffer
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }
}