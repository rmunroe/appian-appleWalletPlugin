package com.robmunroe.plugin.appleWallet.testsupport;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Reads a .pkpass archive into memory: file name to file bytes. */
public final class PkPassArchive {
    private PkPassArchive() {
    }

    public static Map<String, byte[]> read(byte[] archive) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                files.put(entry.getName(), IOUtils.toByteArray(zip));
            }
        }
        return files;
    }
}
