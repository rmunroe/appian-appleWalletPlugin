package com.robmunroe.plugin.appleWallet.expressions;

import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.robmunroe.plugin.appleWallet.AppleWalletCategory;
import com.robmunroe.plugin.appleWallet.helpers.ContentHelper;
import org.apache.log4j.Logger;

@AppleWalletCategory
public class AwpGetUuid {
    private static final Logger LOG = Logger.getLogger(AwpGetUuid.class);

    @Function
    public String awpGetUuid(ContentService contentService, @Parameter @DocumentDataType Long pkpassDocument) {
        return ContentHelper.getDocumentUuid(contentService, pkpassDocument);
    }
}
