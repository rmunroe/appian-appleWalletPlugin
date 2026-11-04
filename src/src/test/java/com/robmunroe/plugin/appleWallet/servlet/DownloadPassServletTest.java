package com.robmunroe.plugin.appleWallet.servlet;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DownloadPassServletTest {

    /**
     * Appian has no ContentService to inject into a servlet. An @Inject field for one stops the servlet from starting,
     * and every request then gets a 404. The servlet has to get the service from each request's context instead, so it
     * needs no instance fields. Getting the service that way can only be checked on a running site, because the SDK's
     * ServiceLocator can't be mocked.
     */
    @Test
    void servletHasNoFieldsForAppianToInject() {
        Field[] instanceFields = Arrays.stream(DownloadPassServlet.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toArray(Field[]::new);

        assertEquals(0, instanceFields.length, "unexpected fields: " + Arrays.toString(instanceFields));
    }
}
