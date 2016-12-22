package com.andr0day;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    public static final String TARGET_SUB = "src";
    public static final String DEX = ".dex";
    public static final String TMP_SUB = "smalidebugtool";

    public static String getAbsPath(String path) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(getCurPath(), path);
        }
        return file.getAbsolutePath();
    }

    public static String getCurPath() {
        return System.getProperty("user.dir");
    }

    public static void unZipDexToTmpDir(String zipPath, String tmpDir) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipPath));
        ZipInputStream zis = new ZipInputStream(bis);
        BufferedOutputStream bos;
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (entryName.endsWith(DEX)) {
                bos = new BufferedOutputStream(new FileOutputStream(new File(tmpDir, entryName)));
                copyStream(zis, bos);
                bos.flush();
                bos.close();
            }
        }
        zis.close();
        bis.close();
    }

    private static void copyStream(InputStream is, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
    }

    public static void delete(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] sub = file.listFiles();
            if (sub != null) {
                for (File it : sub) {
                    delete(it.getAbsolutePath());
                }
            }
        }
        file.delete();
    }

    public static String getTmpDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File file = new File(tmpDir, TMP_SUB);
        return file.getAbsolutePath();
    }

    public static void compareAndCopy(File src, File dst, String dex) throws Exception {
        if (src.isDirectory()) {
            File[] sub = src.listFiles();
            if (sub == null) {
                return;
            }
            for (File it : sub) {
                File d = new File(dst, it.getName());
                if (it.isDirectory()) {
                    if (!d.exists()) {
                        d.mkdirs();
                    }
                }
                compareAndCopy(it, d, dex);
            }
        } else {
            //如果存在同一个文件来自不同的dex，内容会被合并在同一个文件中
            if (dst.exists()) {
                System.out.println("\n same name file from diff dex," + dst.getAbsolutePath());
            }
            copyFile(src, dst, dex);
        }
    }

    private static void copyFile(File src, File dst, String dex) throws Exception {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dst, true);
        copyStream(fis, fos);
        if (dex != null) {
            String tail = "\n# from : " + dex + "\n\n\n";
            fos.write(tail.getBytes());
        }
        fis.close();
        fos.close();
    }

}
