package com.robmunroe.plugin.appleWallet.dataTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robmunroe.plugin.appleWallet.helpers.PassHelper;
import com.robmunroe.plugin.appleWallet.testsupport.PkPassArchive;
import com.robmunroe.plugin.appleWallet.testsupport.TestSigningMaterial;
import de.brendamour.jpasskit.signing.PKPassTemplateInMemory;
import de.brendamour.jpasskit.signing.PKSigningInformation;
import de.brendamour.jpasskit.signing.PKSigningInformationUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins what the plugin produces end to end through the real jpasskit and BouncyCastle: the pass.json content
 * Apple Wallet reads, and a signature that verifies. Catches an upgrade changing the output of an existing pass.
 */
class GenericPassSigningTest {
    private static final byte[] ICON = "fake-icon-bytes".getBytes(StandardCharsets.UTF_8);

    private static TestSigningMaterial material;
    private static Map<String, byte[]> archive;

    @BeforeAll
    static void signPass() throws Exception {
        material = TestSigningMaterial.generate();

        PKSigningInformation signingInformation = new PKSigningInformationUtil()
                .loadSigningInformationFromPKCS12AndIntermediateCertificate(
                        new ByteArrayInputStream(material.pkcs12),
                        TestSigningMaterial.PASSWORD,
                        new ByteArrayInputStream(material.wwdrDer));

        PKPassTemplateInMemory template = new PKPassTemplateInMemory();
        template.addFile(PKPassTemplateInMemory.PK_ICON, new ByteArrayInputStream(ICON));

        byte[] signed = PassHelper.newSigningUtil().createSignedAndZippedPkPassArchive(
                fullyPopulatedPass().getPassBuilder().build(), template, signingInformation);
        archive = PkPassArchive.read(signed);

        Path actual = Paths.get("build", "actual-pass.json");
        Files.createDirectories(actual.getParent());
        Files.write(actual, archive.get("pass.json"));
    }

    @Test
    void passJsonMatchesTheContentCapturedFromRelease106Code() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expected;
        try (InputStream golden = getClass().getResourceAsStream("/golden/pass.json")) {
            assertNotNull(golden, "golden/pass.json is missing");
            expected = mapper.readTree(golden);
        }

        assertEquals(expected, mapper.readTree(archive.get("pass.json")));
    }

    @Test
    void manifestListsTheCorrectHashOfEveryFile() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(archive.get("manifest.json"));

        assertEquals(2, manifest.size(), "manifest should cover pass.json and icon.png");
        assertEquals(DigestUtils.sha1Hex(archive.get("pass.json")), manifest.get("pass.json").asText());
        assertEquals(DigestUtils.sha1Hex(ICON), manifest.get("icon.png").asText());
    }

    @Test
    void signatureVerifiesOverTheManifestWithTheSigningCertificate() throws Exception {
        CMSSignedData signedData = new CMSSignedData(
                new CMSProcessableByteArray(archive.get("manifest.json")), archive.get("signature"));

        assertEquals(1, signedData.getSignerInfos().size());
        Iterator<SignerInformation> signers = signedData.getSignerInfos().getSigners().iterator();
        assertTrue(signers.next().verify(new JcaSimpleSignerInfoVerifierBuilder()
                .setProvider(new BouncyCastleProvider())
                .build(material.signingCertificate)));

        assertEquals(2, signedData.getCertificates().getMatches(null).size(), "signature must embed WWDR and signing certs");
    }

    private static GenericPass fullyPopulatedPass() {
        PassLocation location = new PassLocation(10.5, 38.9, -77.2, "Nearby");
        PassBarcode barcode = new PassBarcode("PKBarcodeFormatQR", "ISO-8859-1", "BARCODE-123");

        return new GenericPass(
                "pass.test.example",
                "Example Org",
                "TEAM123456",
                "auth-token-0123456789",
                "SERIAL-001",
                "Example description",
                "Example logo text",
                "#112233",
                "#445566",
                "#778899",
                50L,
                Date.from(Instant.parse("2030-01-02T03:04:05Z")),
                Date.from(Instant.parse("2031-06-07T08:09:10Z")),
                false,
                true,
                "[{\"key\":\"h\",\"label\":\"Header\",\"value\":\"Header value\"}]",
                "[{\"key\":\"p\",\"label\":\"Primary\",\"value\":\"Primary value\"}]",
                "[{\"key\":\"s\",\"label\":\"Secondary\",\"value\":{\"id\":\"from-id\"}}]",
                "[{\"key\":\"a\",\"label\":\"Aux\",\"value\":\"Aux value\"}]",
                "[{\"key\":\"b\",\"label\":\"Back\",\"value\":\"Back value\"}]",
                Collections.singletonList(location),
                Arrays.asList(barcode)
        );
    }
}
