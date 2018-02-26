package com.scurrilous.circe.crc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * provides utility to load libraries at runtime.
 *
 */
public class LibraryUtils {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);

    /**
     * loads given library from the this jar. ie: this jar contains:
     * /lib/libcirce-crc32c-sse42.jnilib
     * 
     * @param path
     *            : absolute path of the library in the jar <br/>
     *            if this jar contains: /lib/libcirce-crc32c-sse42.jnilib then
     *            provide the same absolute path as input
     * @throws Exception
     */
    public static void loadLibraryFromJar(String path) throws Exception {

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("absolute path must start with  /");
        }

        String[] parts = path.split("/");
        String filename = (parts.length > 0) ? parts[parts.length - 1] : null;

        File dir = File.createTempFile("native", "");
        dir.delete();
        if (!(dir.mkdir())) {
            throw new IOException("Failed to create temp directory " + dir.getAbsolutePath());
        }
        dir.deleteOnExit();
        File temp = new File(dir, filename);
        temp.deleteOnExit();

        byte[] buffer = new byte[1024];
        int read;

        InputStream input = LibraryUtils.class.getResourceAsStream(path);
        if (input == null) {
            throw new FileNotFoundException("Couldn't find file into jar " + path);
        }

        OutputStream out = new FileOutputStream(temp);
        try {
            while ((read = input.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            out.close();
            input.close();
        }

        if (!temp.exists()) {
            throw new FileNotFoundException("Failed to copy file from jar at " + temp.getAbsolutePath());
        }

        System.load(temp.getAbsolutePath());
    }

    /**
     * Returns jni library extension based on OS specification. Maven-nar
     * generates jni library based on different OS :
     * http://mark.donszelmann.org/maven-nar-plugin/aol.html (jni.extension)
     * 
     * @return library type based on operating system
     */
    public static String libType() {

        if (OS_NAME.indexOf("mac") >= 0) {
            return "jnilib";
        } else if (OS_NAME.indexOf("nix") >= 0 || OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("aix") > 0) {
            return "so";
        } else if (OS_NAME.indexOf("win") >= 0) {
            return "dll";
        }
        throw new TypeNotPresentException(OS_NAME + " not supported", null);
    }
}
