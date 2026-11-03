package com.robmunroe.plugin.appleWallet.servlet;

import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.exceptions.InvalidContentException;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DownloadPassServletTest {
    private static final String DOCUMENT_UUID = "_a-0000e1b3-1234";

    /**
     * Sets the field the way AppianServlet.init() does, by finding it through its @Inject annotation. This can't show
     * that Appian supplies the service, or which user it runs as: that has to be checked on a running site.
     */
    private static void inject(DownloadPassServlet servlet, ContentService contentService) throws Exception {
        Field[] injected = Arrays.stream(DownloadPassServlet.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class) && field.getType() == ContentService.class)
                .toArray(Field[]::new);
        assertTrue(injected.length == 1, "the servlet should have one @Inject ContentService field");

        injected[0].setAccessible(true);
        injected[0].set(servlet, contentService);
    }

    @Test
    void requestsAreServedWithTheInjectedContentService() throws Exception {
        ContentService contentService = mock(ContentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("id")).thenReturn(DOCUMENT_UUID);
        when(contentService.getVersion(DOCUMENT_UUID, ContentConstants.VERSION_CURRENT)).thenThrow(InvalidContentException.class);

        DownloadPassServlet servlet = new DownloadPassServlet();
        inject(servlet, contentService);
        servlet.doGet(request, response);

        verify(contentService).getVersion(DOCUMENT_UUID, ContentConstants.VERSION_CURRENT);
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }
}
