package com.robmunroe.plugin.appleWallet.testsupport;

import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.ContentUploadOutputStream;
import com.appiancorp.suiteapi.content.DocumentInputStream;
import com.appiancorp.suiteapi.content.Approval;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.when;

/** Stand-ins for Appian document storage, which only exists inside a running Appian instance. */
public final class FakeDocuments {
    private FakeDocuments() {
    }

    /**
     * Registers a document whose current version id differs from its document id, as it does once a document has been
     * replaced. Reading the document id returns the stale content, so code that skips the version lookup is caught.
     */
    public static void addVersionedDocument(ContentService contentService, long documentId, byte[] currentContent) throws Exception {
        long currentVersionId = documentId + 1000;
        when(contentService.getVersionId(documentId, ContentConstants.VERSION_CURRENT)).thenReturn(currentVersionId);
        when(contentService.getDocumentInputStream(currentVersionId)).thenAnswer(call -> stream(currentContent));
        when(contentService.getDocumentInputStream(documentId)).thenAnswer(call -> stream("stale-content".getBytes()));
    }

    public static DocumentInputStream stream(byte[] content) {
        InputStream source = new ByteArrayInputStream(content);
        return new DocumentInputStream() {
            @Override
            public int read() throws IOException {
                return source.read();
            }
        };
    }

    /** Records what the plugin writes to a new document, and whether the upload was completed by closing the stream. */
    public static final class RecordingUpload extends ContentUploadOutputStream {
        public final ByteArrayOutputStream written = new ByteArrayOutputStream();
        public boolean closed;
        private final Long contentId;
        private final boolean failOnClose;

        public RecordingUpload(Long contentId, boolean failOnClose) {
            this.contentId = contentId;
            this.failOnClose = failOnClose;
        }

        @Override
        public void write(int b) {
            written.write(b);
        }

        @Override
        public Long getContentId() {
            return contentId;
        }

        @Override
        public Approval getApproval() {
            return null;
        }

        @Override
        public void close() throws IOException {
            if (failOnClose) throw new IOException("close failed");
            closed = true;
        }
    }
}
