package com.robmunroe.plugin.appleWallet.servlet;

import com.appiancorp.suiteapi.common.exceptions.AppianException;
import com.appiancorp.suiteapi.common.exceptions.InvalidVersionException;
import com.appiancorp.suiteapi.common.exceptions.PrivilegeException;
import com.appiancorp.suiteapi.content.Content;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.exceptions.InvalidContentException;
import com.appiancorp.suiteapi.knowledge.Document;
import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;
import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * The download logic behind {@link DownloadPassServlet}, kept apart from the servlet so it doesn't depend on the
 * Appian servlet container.
 */
final class DownloadPassHandler {
    private static final Logger LOG = Logger.getLogger(DownloadPassHandler.class);

    private static final String PASS_CONTENT_TYPE = "application/vnd.apple.pkpass";
    private static final String DOC_ID_PARAM = "id";

    private DownloadPassHandler() {
    }

    static void servePass(ContentService contentService, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uuid = req.getParameter(DOC_ID_PARAM);
        if (uuid == null || uuid.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The id parameter is required");
            return;
        }

        try {
            // Reading the document's properties enforces the user's rights to it. Only passes are served: without this,
            // the servlet would also hand out other documents, such as the private key used to sign passes.
            Content content = contentService.getVersion(uuid, ContentConstants.VERSION_CURRENT);
            if (!isPass(content)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Pass not found");
                return;
            }

            // The stream is opened before the response is touched, so that a failure can still send an error status
            try (InputStream input = contentService.getDocumentInputStream(contentService.getVersionId(uuid, ContentConstants.VERSION_CURRENT))) {
                resp.setContentType(PASS_CONTENT_TYPE);
                try (ServletOutputStream output = resp.getOutputStream()) {
                    input.transferTo(output);
                }
            }
        } catch (PrivilegeException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this pass");
        } catch (InvalidContentException | InvalidVersionException | IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Pass not found");
        } catch (AppianException e) {
            LOG.error("Could not read pass " + uuid, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The pass could not be read");
        }
    }

    private static boolean isPass(Content content) {
        return content instanceof Document
                && AppleWalletPluginHelper.PASS_EXTENSION.equalsIgnoreCase(((Document) content).getExtension());
    }
}
