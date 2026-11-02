package com.robmunroe.plugin.appleWallet.testsupport;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Throwaway certificate material generated at test time, so no key or password is ever stored in the repo.
 * Stands in for the real Apple WWDR certificate and Pass Type ID .p12.
 */
public final class TestSigningMaterial {
    public static final String PASSWORD = "test-only-password";

    public final byte[] pkcs12;
    public final byte[] wwdrDer;
    public final X509Certificate signingCertificate;

    private TestSigningMaterial(byte[] pkcs12, byte[] wwdrDer, X509Certificate signingCertificate) {
        this.pkcs12 = pkcs12;
        this.wwdrDer = wwdrDer;
        this.signingCertificate = signingCertificate;
    }

    public static TestSigningMaterial generate() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair caKeys = generator.generateKeyPair();
        KeyPair signingKeys = generator.generateKeyPair();

        X509Certificate ca = certificate("CN=Test WWDR", "CN=Test WWDR", caKeys, caKeys);
        X509Certificate signing = certificate("CN=Test WWDR", "CN=Pass Type ID: pass.test", signingKeys, caKeys);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("signing", signingKeys.getPrivate(), PASSWORD.toCharArray(), new Certificate[]{signing, ca});
        ByteArrayOutputStream p12 = new ByteArrayOutputStream();
        keyStore.store(p12, PASSWORD.toCharArray());

        return new TestSigningMaterial(p12.toByteArray(), ca.getEncoded(), signing);
    }

    private static X509Certificate certificate(String issuer, String subject, KeyPair subjectKeys, KeyPair issuerKeys) throws Exception {
        Instant now = Instant.now();
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(new BouncyCastleProvider())
                .build(issuerKeys.getPrivate());
        X509CertificateHolder holder = new JcaX509v3CertificateBuilder(
                new X500Name(issuer),
                BigInteger.valueOf(now.toEpochMilli()),
                Date.from(now.minus(Duration.ofDays(1))),
                Date.from(now.plus(Duration.ofDays(1))),
                new X500Name(subject),
                subjectKeys.getPublic()
        ).build(signer);
        return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(holder);
    }
}
