package com.andr0day;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static ConcurrentHashMap<Runnable, Boolean> runnables = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("params format: path/of/apk path/of/output");
            System.exit(0);
        }
        String absApk = FileUtil.getAbsPath(args[0]);
        final String absOutput = FileUtil.getAbsPath(args[1]);
        if (!new File(absApk).exists()) {
            System.out.println("apk not exist,path:" + absApk);
            System.exit(0);
        }
        String tmpDir = FileUtil.getTmpDir();
        System.out.println("clean tmp dir: " + tmpDir);
        FileUtil.delete(tmpDir);
        System.out.println("clean output dir: " + absOutput);
        FileUtil.delete(absOutput);
        new File(tmpDir).mkdir();
        new File(absOutput).mkdir();

        System.out.println("Unzip dex files");
        try {
            FileUtil.unZipDexToTmpDir(absApk, tmpDir);
        } catch (Exception e) {
            System.out.println("unzip apk to tmp error," + e.getMessage());
            System.exit(0);
        }

        File[] tmpFiles = new File(tmpDir).listFiles();
        if (tmpFiles == null) {
            System.out.println("dex not found");
            System.exit(0);
        }
        for (final File it : tmpFiles) {
            String fileName = it.getName();
            if (fileName.endsWith(FileUtil.DEX)) {
                String prefix = fileName.replace(FileUtil.DEX, "");
                final String output = absOutput + "/" + prefix;

                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Process process = ProcessUtil.bakSmaliDex(it.getAbsolutePath(), output);
                            process.waitFor();
                            runnables.remove(this);
                        } catch (Exception e) {
                            System.out
                                    .println("baksmali dex error,path:" + it.getAbsolutePath() + "," + e.getMessage());
                            System.exit(0);
                        }
                    }
                };
                runnables.put(runnable, true);
                new Thread(runnable).start();
            }
        }
        System.out.print("baksmali start ...");
        loop();
        System.out.println("\nbaksmali done");

        final File targetOut = new File(absOutput, FileUtil.TARGET_SUB);

        runnables.clear();
        Runnable mergeRunnable = new Runnable() {
            @Override
            public void run() {
                File[] subOut = new File(absOutput).listFiles();
                if (subOut != null) {
                    for (File it : subOut) {
                        if (it.getName().equals(FileUtil.TARGET_SUB)) {
                            continue;
                        }
                        try {
                            FileUtil.compareAndCopy(it, targetOut);
                            FileUtil.delete(it.getAbsolutePath());
                        } catch (Exception e) {
                            System.out.println("merge file error" + e.getMessage());
                            System.exit(0);
                        }
                    }
                }
                runnables.remove(this);
            }
        };
        runnables.put(mergeRunnable, true);
        new Thread(mergeRunnable).start();

        System.out.print("merge dir ...");
        loop();
        System.out.println("\nmerge dir done");
        FileUtil.delete(tmpDir);
        System.out.println("success! path:" + absOutput);
    }

    private static void loop() {
        do {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //ignore
            }
            if (runnables.size() == 0) {
                break;
            } else {
                System.out.print(".");
            }
        } while (true);
    }
}
