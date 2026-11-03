package com.robmunroe.plugin.appleWallet.smartServices;

import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.security.external.SecureCredentialsStore;
import com.robmunroe.plugin.appleWallet.dataTypes.GenericPass;
import com.robmunroe.plugin.appleWallet.dataTypes.PassImages;
import com.robmunroe.plugin.appleWallet.testsupport.FakeDocuments;
import com.robmunroe.plugin.appleWallet.testsupport.FakeDocuments.RecordingUpload;
import com.robmunroe.plugin.appleWallet.testsupport.PkPassArchive;
import com.robmunroe.plugin.appleWallet.testsupport.TestSigningMaterial;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateGenericPassSmartServiceTest {
    private static final long WWDR_ID = 10;
    private static final long KEY_ID = 20;
    private static final long ICON_ID = 30;
    private static final long FOLDER_ID = 40;
    private static final long NEW_DOCUMENT_ID = 999;
    private static final String SCS_KEY = "wallet-scs-key";
    private static final byte[] ICON_BYTES = "current-icon".getBytes(StandardCharsets.UTF_8);

    private static TestSigningMaterial material;

    private ContentService contentService;
    private SecureCredentialsStore credentials;
    private RecordingUpload upload;
    private CreateGenericPassSmartService service;

    @BeforeAll
    static void generateCertificates() throws Exception {
        material = TestSigningMaterial.generate();
    }

    @BeforeEach
    void setUp() throws Exception {
        contentService = mock(ContentService.class);
        credentials = mock(SecureCredentialsStore.class);
        upload = new RecordingUpload(NEW_DOCUMENT_ID, false);

        FakeDocuments.addVersionedDocument(contentService, WWDR_ID, material.wwdrDer);
        FakeDocuments.addVersionedDocument(contentService, KEY_ID, material.pkcs12);
        FakeDocuments.addVersionedDocument(contentService, ICON_ID, ICON_BYTES);
        when(contentService.uploadDocument(any(), any())).thenReturn(upload);
        when(credentials.getSystemSecuredValues(SCS_KEY))
                .thenReturn(Collections.singletonMap("password", TestSigningMaterial.PASSWORD));

        service = new CreateGenericPassSmartService(contentService, credentials);
        service.setAppleWWDRCA(WWDR_ID);
        service.setPrivateKey(KEY_ID);
        service.setPasswordScsKey(SCS_KEY);
        service.setOutputFolder(FOLDER_ID);
        service.setOutputFilename("my-pass");
        service.setPassImages(imagesWithIconOnly());
        service.setGenericPass(validPass());
    }

    private static PassImages imagesWithIconOnly() {
        PassImages images = new PassImages();
        images.setIcon(ICON_ID);
        return images;
    }

    private static GenericPass validPass() {
        GenericPass pass = new GenericPass();
        pass.setPassTypeIdentifier("pass.test.example");
        pass.setOrganizationName("Example Org");
        pass.setTeamIdentifier("TEAM123456");
        pass.setSerialNumber("SERIAL-001");
        pass.setDescription("Example description");
        return pass;
    }

    private void assertNothingWasSaved() throws Exception {
        verify(contentService, never()).uploadDocument(any(), any());
        verify(contentService, never()).upload(any(), any());
        assertNull(service.getOutputFile());
    }

    @Test
    void validInputsProduceASignedPassDocument() throws Exception {
        service.run();

        assertFalse(service.getErrorOccurred(), service.getErrorTxt());
        assertNull(service.getErrorTxt());
        assertEquals(NEW_DOCUMENT_ID, service.getOutputFile());
        assertTrue(upload.closed, "the upload only completes when its stream is closed");

        Map<String, byte[]> archive = PkPassArchive.read(upload.written.toByteArray());
        assertEquals(new TreeSet<>(Set.of("pass.json", "manifest.json", "signature", "icon.png")), new TreeSet<>(archive.keySet()));
        assertTrue(new String(archive.get("pass.json"), StandardCharsets.UTF_8).contains("\"serialNumber\":\"SERIAL-001\""));
    }

    @Test
    void newDocumentHasTheRequestedNameFolderAndExtension() throws Exception {
        service.run();

        ArgumentCaptor<Document> created = ArgumentCaptor.forClass(Document.class);
        verify(contentService).uploadDocument(created.capture(), any());
        assertEquals("my-pass", created.getValue().getName());
        assertEquals(FOLDER_ID, created.getValue().getParent());
        assertEquals("pkpass", created.getValue().getExtension());
    }

    @Test
    void readsTheCurrentVersionOfADocumentThatHasBeenReplaced() throws Exception {
        service.run();

        Map<String, byte[]> archive = PkPassArchive.read(upload.written.toByteArray());
        assertArrayEquals(ICON_BYTES, archive.get("icon.png"));
    }

    @Test
    void everyImageSlotIsWrittenUnderItsAppleFileName() throws Exception {
        PassImages images = new PassImages(
                101L, 102L, 103L,
                104L, 105L, 106L,
                107L, 108L, 109L,
                110L, 111L, 112L,
                113L, 114L, 115L,
                116L, 117L, 118L,
                119L, 120L, 121L);
        String[] expectedNames = {
                "icon.png", "icon@2x.png", "icon@3x.png",
                "logo.png", "logo@2x.png", "logo@3x.png",
                "thumbnail.png", "thumbnail@2x.png", "thumbnail@3x.png",
                "strip.png", "strip@2x.png", "strip@3x.png",
                "background.png", "background@2x.png", "background@3x.png",
                "footer.png", "footer@2x.png", "footer@3x.png",
                "personalizationLogo.png", "personalizationLogo@2x.png", "personalizationLogo@3x.png"};
        for (int i = 0; i < expectedNames.length; i++) {
            FakeDocuments.addVersionedDocument(contentService, 101L + i, expectedNames[i].getBytes(StandardCharsets.UTF_8));
        }
        service.setPassImages(images);

        service.run();

        assertFalse(service.getErrorOccurred(), service.getErrorTxt());
        Map<String, byte[]> archive = PkPassArchive.read(upload.written.toByteArray());
        for (String name : expectedNames) {
            assertEquals(name, new String(archive.get(name), StandardCharsets.UTF_8), "wrong image in slot " + name);
        }
    }

    @Test
    void invalidPassReportsTheValidationProblemAndSavesNothing() throws Exception {
        GenericPass invalid = validPass();
        invalid.setSerialNumber(null);
        service.setGenericPass(invalid);

        service.run();

        assertTrue(service.getErrorOccurred());
        assertTrue(service.getErrorTxt().startsWith("The provided arguments did not produce a valid Pass: "), service.getErrorTxt());
        assertNothingWasSaved();
    }

    @Test
    void wrongKeyPasswordReportsAnErrorAndSavesNothing() throws Exception {
        when(credentials.getSystemSecuredValues(SCS_KEY)).thenReturn(Collections.singletonMap("password", "wrong"));

        service.run();

        assertTrue(service.getErrorOccurred());
        assertNothingWasSaved();
    }

    @Test
    void missingIconReportsThatItIsRequired() throws Exception {
        service.setPassImages(new PassImages());

        service.run();

        assertTrue(service.getErrorOccurred());
        assertTrue(service.getErrorTxt().contains("Icon image is required"), service.getErrorTxt());
        assertNothingWasSaved();
    }

    @Test
    void missingImagesInputReportsThatTheIconIsRequired() throws Exception {
        service.setPassImages(null);

        service.run();

        assertTrue(service.getErrorOccurred());
        assertTrue(service.getErrorTxt().contains("Icon image is required"), service.getErrorTxt());
        assertNothingWasSaved();
    }

    @Test
    void credentialEntryWithoutAPasswordFieldIsExplainedWithoutSavingAnything() throws Exception {
        when(credentials.getSystemSecuredValues(SCS_KEY)).thenReturn(Collections.emptyMap());

        service.run();

        assertTrue(service.getErrorOccurred());
        assertTrue(service.getErrorTxt().contains(SCS_KEY), service.getErrorTxt());
        assertTrue(service.getErrorTxt().contains("password"), service.getErrorTxt());
        assertNothingWasSaved();
    }

    @Test
    void failureToFinishTheUploadReportsAnErrorAndNoOutputFile() throws Exception {
        when(contentService.uploadDocument(any(), any())).thenReturn(new RecordingUpload(NEW_DOCUMENT_ID, true));

        service.run();

        assertTrue(service.getErrorOccurred());
        assertNull(service.getOutputFile());
    }

    @Test
    void exceptionWithNoMessageStillGivesAUsefulError() throws Exception {
        when(contentService.uploadDocument(any(), any())).thenThrow(new IllegalStateException());

        service.run();

        assertTrue(service.getErrorOccurred());
        assertTrue(service.getErrorTxt().contains("IllegalStateException"), service.getErrorTxt());
    }

    @Test
    void failureUnrelatedToThePassContentIsNotBlamedOnTheArguments() throws Exception {
        when(contentService.uploadDocument(any(), any())).thenThrow(new IllegalStateException("storage unavailable"));

        service.run();

        assertTrue(service.getErrorOccurred());
        assertEquals("Could not create the pass: storage unavailable", service.getErrorTxt());
    }
}
