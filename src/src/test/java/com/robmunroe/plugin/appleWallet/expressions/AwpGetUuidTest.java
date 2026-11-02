package com.robmunroe.plugin.appleWallet.expressions;

import com.appiancorp.suiteapi.common.exceptions.PrivilegeException;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AwpGetUuidTest {
    private final ContentService contentService = mock(ContentService.class);

    @Test
    void returnsTheUuidOfTheDocument() throws Exception {
        Document document = new Document();
        document.setUuid("_a-0000e1b3-1234");
        when(contentService.getVersion(5L, ContentConstants.VERSION_CURRENT)).thenReturn(document);

        assertEquals("_a-0000e1b3-1234", new AwpGetUuid().awpGetUuid(contentService, 5L));
    }

    @Test
    void nullDocumentGivesNull() {
        assertNull(new AwpGetUuid().awpGetUuid(contentService, null));
    }

    @Test
    void documentTheUserCannotReadGivesNull() throws Exception {
        when(contentService.getVersion(5L, ContentConstants.VERSION_CURRENT)).thenThrow(PrivilegeException.class);

        assertNull(new AwpGetUuid().awpGetUuid(contentService, 5L));
    }
}
