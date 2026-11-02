package com.robmunroe.plugin.appleWallet.servlet;

import com.appiancorp.suiteapi.common.exceptions.AppianStorageException;
import com.appiancorp.suiteapi.common.exceptions.PrivilegeException;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.exceptions.InvalidContentException;
import com.appiancorp.suiteapi.knowledge.Document;
import com.robmunroe.plugin.appleWallet.testsupport.FakeDocuments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DownloadPassHandlerTest {
    private static final String UUID = "_a-0000e1b3-1234";
    private static final byte[] CURRENT_CONTENT = "current-pass".getBytes(StandardCharsets.UTF_8);
    private static final long DOCUMENT_ID = 50;
    private static final long CURRENT_VERSION_ID = 51;

    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private ContentService contentService;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        contentService = mock(ContentService.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) {
                body.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
            }
        });
    }

    private void givenDocument(String extension) throws Exception {
        Document document = new Document();
        document.setId(CURRENT_VERSION_ID);
        document.setExtension(extension);
        when(request.getParameter("id")).thenReturn(UUID);
        when(contentService.getIdByUuid(UUID)).thenReturn(DOCUMENT_ID);
        when(contentService.getVersion(UUID, ContentConstants.VERSION_CURRENT)).thenReturn(document);
        when(contentService.getVersionId(UUID, ContentConstants.VERSION_CURRENT)).thenReturn(CURRENT_VERSION_ID);
        when(contentService.getDocumentInputStream(CURRENT_VERSION_ID)).thenAnswer(call -> FakeDocuments.stream(CURRENT_CONTENT));
        when(contentService.getDocumentInputStream(DOCUMENT_ID)).thenAnswer(call -> FakeDocuments.stream("stale".getBytes()));
    }

    private void assertRejectedWith(int status) throws Exception {
        verify(response).sendError(eq(status), anyString());
        assertEquals(0, body.size(), "a rejected request must not return document content");
    }

    @Test
    void passIsServedWithThePassContentTypeFromTheCurrentVersion() throws Exception {
        givenDocument("pkpass");

        DownloadPassHandler.servePass(contentService, request, response);

        verify(response).setContentType("application/vnd.apple.pkpass");
        verify(response, never()).sendError(anyInt(), anyString());
        assertArrayEquals(CURRENT_CONTENT, body.toByteArray());
    }

    @Test
    void passExtensionMatchIgnoresCase() throws Exception {
        givenDocument("PKPASS");

        DownloadPassHandler.servePass(contentService, request, response);

        assertArrayEquals(CURRENT_CONTENT, body.toByteArray());
    }

    @Test
    void missingIdIsABadRequest() throws Exception {
        DownloadPassHandler.servePass(contentService, request, response);

        assertRejectedWith(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void emptyIdIsABadRequest() throws Exception {
        when(request.getParameter("id")).thenReturn("");

        DownloadPassHandler.servePass(contentService, request, response);

        assertRejectedWith(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void unknownDocumentIsNotFound() throws Exception {
        when(request.getParameter("id")).thenReturn(UUID);
        when(contentService.getVersion(UUID, ContentConstants.VERSION_CURRENT)).thenThrow(InvalidContentException.class);

        DownloadPassHandler.servePass(contentService, request, response);

        assertRejectedWith(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void userWithoutRightsToTheDocumentIsForbidden() throws Exception {
        givenDocument("pkpass");
        when(contentService.getVersion(UUID, ContentConstants.VERSION_CURRENT)).thenThrow(PrivilegeException.class);

        DownloadPassHandler.servePass(contentService, request, response);

        assertRejectedWith(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void signingKeyIsNotServedEvenToAUserWithRightsToIt() throws Exception {
        givenDocument("p12");

        DownloadPassHandler.servePass(contentService, request, response);

        assertRejectedWith(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void storageFailureIsAServerErrorNotAnEmptySuccess() throws Exception {
        givenDocument("pkpass");
        when(contentService.getDocumentInputStream(CURRENT_VERSION_ID)).thenThrow(AppianStorageException.class);

        DownloadPassHandler.servePass(contentService, request, response);

        assertRejectedWith(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
