package com.robmunroe.plugin.appleWallet.helpers;

import com.appiancorp.suiteapi.knowledge.Document;

/**
 * Constants and document setup shared across the plugin.
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
}
