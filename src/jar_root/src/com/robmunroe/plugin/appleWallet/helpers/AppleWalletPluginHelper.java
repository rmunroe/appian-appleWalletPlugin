package com.robmunroe.plugin.appleWallet.helpers;

import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentOutputStream;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;

/**
 * Example helper file with some useful functions. Rename this class or delete it.
 */
public class AppleWalletPluginHelper {
    public static final String PASS_EXTENSION = "pkpass";

    public static final String NAMESPACE = "urn:robmunroe:plugin:appleWallet:types";

    public static final String SMARTSERVICE_PALLETCATEGORY = "Custom Services";
    public static final String SMARTSERVICE_PALLET = "Apple Wallet";


    /**
     * Creates a Document object that can then be stored in Appian's ContentService
     *
     * @param name        the Document name, without the extension
     * @param description the Document description
     * @param folderId    the Folder ID (Long) where the document should be stored
     * @param extension   the Document extension
     * @return
     * @throws Exception
     */
    public static Document createNewDocument(String name, String description,
                                             Long folderId, String extension) throws Exception {
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
     * Returns the OS filesystem path of the physical file of a Document stored in Appian, useful for file IO operations
     *
     * @param contentService the Appian injected ContentService instance
     * @param documentId     the Long ID of a valid Document
     * @return String path to the file
     * @throws Exception
     */
    public static String getPhysicalFilePath(ContentService contentService, Long documentId) throws Exception {
        return contentService.getInternalFilename(documentId);
    }


    /**
     * Takes a Document's ID (a Long) and retrieves the Document object from Appian's ContentService. The
     * ContentService is usually obtained "automagically" via dependency injection by requiring an instance
     * of it as a parameter to a method, such as an expression function.
     *
     * @param contentService the Appian injected ContentService instance
     * @param documentId     the Long ID of a valid Document
     * @return Document instance
     * @throws Exception
     */
    public static Document getDocument(ContentService contentService, Long documentId) throws Exception {
        return (Document) contentService.getVersion(documentId, ContentConstants.VERSION_CURRENT);
    }


    /**
     * Takes a Document instance (such as created by createNewDocument) and uploads it to Appian's ContentService,
     * returning a ContentOutputStream (extension of java.io.FileOutputStream) suitable for writing data to using
     * methods found in java.io.* and other libraries that use this standard.
     *
     * @param contentService the Appian injected ContentService instance
     * @param documentId     the Long ID of a valid Document
     * @return
     * @throws Exception
     */
    public static ContentOutputStream uploadDocumentForWriting(ContentService contentService, Document document) throws Exception {
        return contentService.upload(document, ContentConstants.UNIQUE_NONE);
    }


    /**
     * Will lowercase the first letter of a String, as long as the second letter is not also uppercase
     *
     * @param string Any string
     * @return
     */
    private String decapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        char[] c = string.toCharArray();

        // Check if the second character is also uppercase
        if (c.length > 1) {
            if (Character.isUpperCase(c[1]))
                return string;
        }

        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
