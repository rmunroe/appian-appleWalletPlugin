package com.robmunroe.plugin.appleWallet.helpers;

import com.appiancorp.suiteapi.common.Constants;
import com.appiancorp.suiteapi.common.exceptions.AppianException;
import com.appiancorp.suiteapi.common.exceptions.InvalidVersionException;
import com.appiancorp.suiteapi.common.exceptions.PrivilegeException;
import com.appiancorp.suiteapi.common.exceptions.StorageLimitException;
import com.appiancorp.suiteapi.content.*;
import com.appiancorp.suiteapi.content.exceptions.DuplicateUuidException;
import com.appiancorp.suiteapi.content.exceptions.InsufficientNameUniquenessException;
import com.appiancorp.suiteapi.content.exceptions.InvalidContentException;
import com.appiancorp.suiteapi.content.exceptions.InvalidTypeMaskException;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.KnowledgeFolder;
import org.apache.log4j.Logger;

/**
 * Class with helper functions.
 */
public class ContentHelper {

    private static final Logger LOG = Logger.getLogger(ContentHelper.class);


    /**
     * Locates a sub-folder by name (case sensitive) within a parent Folder using the given relative path (eg. "subfolder1/subfolder2") and creates one if not found
     * @param contentService the Appian injected ContentService instance
     * @param relativePath the relative path of the folder to find or create within the parent folder, eg. "subfolder1/subfolder2"
     * @param parentFolder the Long ID of the parent Folder where searching should begin
     * @return The Long ID of the Folder found or the new Folder created
     */
    public static Long findOrCreateFolderWithPath(ContentService contentService, String relativePath, Long parentFolder) {
        relativePath = relativePath.replaceFirst("^/", "");
        Long currentFolder = parentFolder;
        for (String pathSegment : relativePath.split("/")) {
            currentFolder = findOrCreateFolder(contentService, pathSegment, currentFolder);
        }
        return currentFolder;
    }


    /**
     * Locates a sub-folder by name (case sensitive) within a parent Folder and creates one if not found
     * @param contentService the Appian injected ContentService instance
     * @param folderName the name of the folder to search for (case sensitive)
     * @param parentFolder the Long ID of the parent Folder
     * @return The Long ID of the Folder found or the new Folder created
     */
    public static Long findOrCreateFolder(ContentService contentService, String folderName, Long parentFolder) {
        try {
            Long folderId = findFolderInParent(contentService, folderName, parentFolder);
            if (folderId == null) {
                Folder folder = new KnowledgeFolder();
                folder.setParent(parentFolder);
                folder.setName(folderName);
                return contentService.create(folder, ContentConstants.UNIQUE_NONE); // create new and return
            } else {
                return folderId;
            }
        } catch (InvalidContentException | StorageLimitException | PrivilegeException | InsufficientNameUniquenessException | DuplicateUuidException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Locates a sub-folder by name (case sensitive) within a parent Folder
     * @param contentService the Appian injected ContentService instance
     * @param folderName the name of the folder to search for (case sensitive)
     * @param parentFolder the Long ID of the parent Folder
     * @return The Long ID of the Folder found, or null if not found
     */
    public static Long findFolderInParent(ContentService contentService, String folderName, Long parentFolder) {
        try {
            ContentFilter contentFilter = new ContentFilter(ContentConstants.TYPE_FOLDER);
            contentFilter.setName(folderName);

            Content[] childFolders = (Content[])contentService.getChildrenPaging(
                    parentFolder,
                    contentFilter,
                    ContentConstants.GC_MOD_NORMAL,
                    0,
                    Constants.COUNT_ALL,
                    ContentConstants.COLUMN_NAME,
                    Constants.SORT_ORDER_ASCENDING
            ).getResults();

            for (Content child : childFolders)
                if (child.getName().matches(folderName))
                    return child.getId(); // Found an existing folder with this name, return it

        } catch (InvalidContentException | InvalidTypeMaskException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Looks for a Document by name (case sensitive) and extension (case insensitive) within a parent Folder
     *
     * @param contentService the Appian injected ContentService instance
     * @param documentName   the name of the document to search for (case sensitive)
     * @param extension      the extension of the document to search for (case insensitive)
     * @param parentFolder   the Long ID of the parent Folder
     * @return The Long ID of the Document found, or null if not found
     */
    public static Long findDocumentInFolder(ContentService contentService, String documentName, String extension, Long parentFolder) {
        try {
            ContentFilter contentFilter = new ContentFilter(ContentConstants.TYPE_DOCUMENT);
            contentFilter.setName(documentName);

            Content[] childDocuments = (Content[]) contentService.getChildrenPaging(
                    parentFolder,
                    contentFilter,
                    ContentConstants.GC_MOD_NORMAL,
                    0,
                    Constants.COUNT_ALL,
                    ContentConstants.COLUMN_NAME,
                    Constants.SORT_ORDER_ASCENDING
            ).getResults();

            for (Content child : childDocuments) {
                if (child.getName().matches(documentName)) {
                    if (((Document) child).getExtension().toUpperCase().equals(extension.toUpperCase())) {
                        return child.getId(); // Found an existing document with this name, return it
                    }
                }
            }

        } catch (InvalidContentException | InvalidTypeMaskException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Creates a Document object that can then be stored in Appian's ContentService
     * @param name the Document name, without the extension
     * @param description the Document description
     * @param folderId the Folder ID (Long) where the document should be stored
     * @param extension the Document extension
     * @return the new Document
     */
    public static Document createNewDocument(String name, String description, Long folderId, String extension) {
        Document document = new Document();
        document.setName(name);
        document.setDescription(description);
        document.setSize(0);
        document.setParent(folderId);
        document.setExtension(extension);
        document.setId(null);
        return document;
    }


    /**
     * Returns the Appian Document's UUID
     * @param contentService the Appian injected ContentService instance
     * @param documentId the Long ID of the Document
     * @return the String UUID from Appian's content management system
     */
    public static String getDocumentUuid(ContentService contentService, Long documentId) {
        try {
            Content content = contentService.getVersion(documentId, ContentConstants.VERSION_CURRENT);
            return content.getUuid();
        } catch (InvalidContentException | InvalidVersionException | PrivilegeException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    /**
     * Returns the OS filesystem path of the physical file of a Document stored in Appian, useful for file IO operations
     * @param contentService the Appian injected ContentService instance
     * @param documentId the Long ID of a valid Document
     * @return String path to the file
     * @throws AppianException Any exception
     */
    public static String getPhysicalFilePath(ContentService contentService, Long documentId) throws AppianException {
        return contentService.getInternalFilename(documentId);
    }


    /**
     * Takes a Document's ID (a Long) and retrieves the Document object from Appian's ContentService. The
     * ContentService is usually obtained "automagically" via dependency injection by requiring an instance
     * of it as a parameter to a method, such as an expression function.
     * @param contentService the Appian injected ContentService instance
     * @param documentId the Long ID of a valid Document
     * @return Document instance
     * @throws AppianException Any exception
     */
    public static Document getDocument(ContentService contentService, Long documentId) throws AppianException {
        return (Document) contentService.getVersion(documentId, ContentConstants.VERSION_CURRENT);
    }


    /**
     * Takes a Document instance (such as created by createNewDocument) and uploads it to Appian's ContentService,
     * returning a ContentOutputStream (extension of java.io.FileOutputStream) suitable for writing data to using
     * methods found in java.io.* and other libraries that use this standard.
     * @param contentService the Appian injected ContentService instance
     * @param document the Document
     * @return The open ContentOutputStream to the physical file, ready for writing
     * @throws AppianException Any exception
     */
    public static ContentOutputStream uploadDocumentForWriting(ContentService contentService, Document document) throws AppianException {
        return contentService.upload(document, ContentConstants.UNIQUE_NONE);
    }
}
