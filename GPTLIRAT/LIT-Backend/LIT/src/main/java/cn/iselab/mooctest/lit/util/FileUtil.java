package cn.iselab.mooctest.lit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
    }

    public static void mkdirs(File file) {
        if (!file.exists() && !file.mkdirs()) {
            LOGGER.error("Cannot create directories {}.", file.getPath());
        }
    }

    public static void mkdirs(String path) {
        mkdirs(new File(path));
    }

    public static void mkParentDirs(File file) {
        mkdirs(file.getParentFile());
    }

    public static void mkParentDirs(String path) {
        mkParentDirs(new File(path));
    }

    public static void saveFile(byte[] data, String path) {
        if (data == null) {
            LOGGER.warn("Input byte array is null.");
            return;
        }

        File file = new File(path);
        mkParentDirs(file);

        try (FileOutputStream fOut = new FileOutputStream(file)) {
            fOut.write(data);
            fOut.flush();
        } catch (IOException e) {
            LOGGER.error("Error on saving file to " + path + ".", e);
        }
    }
}
