package com.robmunroe.plugin.appleWallet.helpers;

import com.appiancorp.suiteapi.common.exceptions.AppianException;
import com.appiancorp.suiteapi.common.exceptions.InvalidVersionException;
import com.appiancorp.suiteapi.common.exceptions.PrivilegeException;
import com.appiancorp.suiteapi.content.Content;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.ContentUploadOutputStream;
import com.appiancorp.suiteapi.content.DocumentInputStream;
import com.appiancorp.suiteapi.content.exceptions.InvalidContentException;
import com.appiancorp.suiteapi.knowledge.Document;
import org.apache.log4j.Logger;

/**
 * The plugin's access to Appian documents.
 */
public class ContentHelper {
    private static final Logger LOG = Logger.getLogger(ContentHelper.class);


    /**
     * Returns the Appian Document's UUID
     * @param contentService the Appian injected ContentService instance
     * @param documentId the Long ID of the Document
     * @return the String UUID from Appian's content management system, or null if there is no such readable Document
     */
    public static String getDocumentUuid(ContentService contentService, Long documentId) {
        if (documentId == null) return null;

        try {
            Content content = contentService.getVersion(documentId, ContentConstants.VERSION_CURRENT);
            return content.getUuid();
        } catch (InvalidContentException | InvalidVersionException | PrivilegeException e) {
            LOG.error("Could not read the UUID of document " + documentId, e);
        }
        return null;
    }


    /**
     * Opens the current version of a Document for reading. A Document ID alone isn't enough, because the read API
     * takes a version ID, and a Document that has been replaced has moved on to a new one. The caller must close the
     * returned stream.
     * @param contentService the Appian injected ContentService instance
     * @param documentId the Long ID of a valid Document
     * @return an open stream of the content of the Document's current version
     * @throws AppianException if the Document can't be found or read
     */
    public static DocumentInputStream openCurrentVersion(ContentService contentService, Long documentId) throws AppianException {
        return contentService.getDocumentInputStream(contentService.getVersionId(documentId, ContentConstants.VERSION_CURRENT));
    }


    /**
     * Creates a Document in Appian's ContentService and returns a stream for writing its content. The upload only
     * completes when the stream is closed.
     * @param contentService the Appian injected ContentService instance
     * @param document the Document
     * @return the open stream to write the Document's content to
     * @throws AppianException if the Document can't be created
     */
    public static ContentUploadOutputStream uploadDocumentForWriting(ContentService contentService, Document document) throws AppianException {
        return contentService.uploadDocument(document, ContentConstants.UNIQUE_NONE);
    }
}
