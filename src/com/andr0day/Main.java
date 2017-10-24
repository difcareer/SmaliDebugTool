package com.andr0day;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static ConcurrentHashMap<Runnable, Boolean> apkToolRunnables = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Runnable, Boolean> runnables = new ConcurrentHashMap<>();
    private static Map<String, String> dexMap = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("params format: path/of/apk path/of/output");
            System.exit(0);
        }
        final String absApk = FileUtil.getAbsPath(args[0]);
        String absOutput = FileUtil.getAbsPath(args[1]);
        if (!new File(absApk).exists()) {
            System.out.println("apk not exist,path:" + absApk);
            System.exit(0);
        }
        String tmpDexDir = FileUtil.getTmpDexDir();
        System.out.println("clean tmp dex dir: " + tmpDexDir);
        FileUtil.delete(tmpDexDir);

        final String tmpApkDir = FileUtil.getTmpApkDir();
        System.out.println("clean tmp apk dir: " + tmpApkDir);
        FileUtil.delete(tmpApkDir);

        System.out.println("clean output dir: " + absOutput);
        FileUtil.delete(absOutput);

        new File(tmpDexDir).mkdir();
//        new File(tmpApkDir).mkdir();
        new File(absOutput).mkdir();

        Runnable apkToolRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("apktool start:" + absApk);
                try {
                    Process process = ProcessUtil.apkToolApk(absApk, tmpApkDir);
                    InputStream is = process.getInputStream();
                    InputStream es = process.getErrorStream();
                    String iMsg = readToStr(is);
                    String eMsg = readToStr(es);
                    if (!iMsg.isEmpty()) {
                        System.out.println(iMsg);
                    }
                    if (!eMsg.isEmpty()) {
                        System.out.println("apktool err :\n" + eMsg);
                    }
                    System.out.println("\napktool done:" + tmpApkDir);
                    is.close();
                    es.close();
                    process.destroy();
                } catch (Exception e) {
                    System.out
                            .println("apktool error,path:" + tmpApkDir + "," + e.getMessage());
                    System.exit(0);
                }
                apkToolRunnables.remove(this);
            }
        };

        apkToolRunnables.put(apkToolRunnable, true);
        new Thread(apkToolRunnable).start();

        System.out.println("Unzip apk to dir:" + tmpDexDir);
        try {
            FileUtil.unZipDexToTmpDir(absApk, tmpDexDir);
        } catch (Exception e) {
            System.out.println("unzip apk to tmp error," + e.getMessage());
            System.exit(0);
        }

        File[] tmpFiles = new File(tmpDexDir).listFiles();
        if (tmpFiles == null) {
            System.out.println("dex not found");
            System.exit(0);
        }
        for (final File it : tmpFiles) {
            final String filePath = it.getAbsolutePath();
            String fileName = it.getName();
            if (fileName.endsWith(FileUtil.DEX)) {
                System.out.println("find dex:" + it.getAbsolutePath());
                String prefix = fileName.replace(FileUtil.DEX, "");
                final String output = absOutput + File.separator + prefix;
                dexMap.put(output, fileName);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("baksmali dex start:" + filePath);
                        try {
                            Process process = ProcessUtil.bakSmaliDex(filePath, output);
                            InputStream is = process.getInputStream();
                            InputStream es = process.getErrorStream();
                            String iMsg = readToStr(is);
                            String eMsg = readToStr(es);
                            if (!iMsg.isEmpty()) {
                                System.out.println(iMsg);
                            }
                            if (!eMsg.isEmpty()) {
                                System.out.println("baksmali err :\n" + eMsg);
                            }
                            System.out.println("\nbaksmali dex done:" + filePath);
                            is.close();
                            es.close();
                            process.destroy();
                        } catch (Exception e) {
                            System.out
                                    .println("baksmali dex error,path:" + filePath + "," + e.getMessage());
                            System.exit(0);
                        }
                        runnables.remove(this);
                    }
                };

                runnables.put(runnable, true);
                Thread sub = new Thread(runnable);
                sub.start();
            }
        }

        loop(runnables);

        final File targetOut = new File(absOutput, FileUtil.TARGET_SUB);
        if (targetOut.exists()) {
            targetOut.delete();
        }
        targetOut.mkdir();

        runnables.clear();

        System.out.print("merge dir ...");
        File[] subOut = new File(absOutput).listFiles();
        if (subOut != null) {
            for (final File it : subOut) {
                if (it.getName().equals(FileUtil.TARGET_SUB)) {
                    continue;
                }
                Runnable mergeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileUtil.compareAndCopy(it, targetOut, dexMap.get(it.getAbsolutePath()));
                            FileUtil.delete(it.getAbsolutePath());
                        } catch (Exception e) {
                            System.out.println("merge file error" + e.getMessage());
                            System.exit(0);
                        }
                        runnables.remove(this);
                    }
                };
                runnables.put(mergeRunnable, true);
                new Thread(mergeRunnable).start();
            }
        }
        loop(runnables);
        System.out.println("\nmerge dir done");
        FileUtil.delete(tmpDexDir);

        loop(apkToolRunnables);

        System.out.println("merge apktool start");
        try {
            FileUtil.copyDir(new File(tmpApkDir), new File(absOutput));
        } catch (Exception e) {
            System.out.println("merge apktool res err," + e.getMessage());
        }
        System.out.println("merge apktool done");
        FileUtil.delete(tmpApkDir);

        System.out.println("success! path:" + absOutput);
    }

    private static void loop(Map map) {
        do {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //ignore
            }
            if (map.size() == 0) {
                break;
            } else {
                System.out.print(".");
            }
        } while (true);
    }

    private static String readToStr(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        try {
            FileUtil.copyStream(is, baos);
        } catch (Exception e) {
            System.out.println("copy steam err");
        }
        return new String(baos.toByteArray());
    }
}
