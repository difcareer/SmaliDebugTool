package com.andr0day;

public class ProcessUtil {
    private static final String BAK_SMALI = "baksmali-2.0.3.jar";
    private static final String APK_TOOL = "apktool_2.1.1.jar";


    public static Process execute(String cmd) throws Exception {
        return Runtime.getRuntime().exec(cmd);
    }

    public static Process bakSmaliDex(String dexPath, String outputPath) throws Exception {
        return execute("java -jar " + BAK_SMALI + " " + dexPath + " -o " + outputPath);
    }

    public static Process apkToolApk(String apkPath, String outputPath) throws Exception {
        return execute("java -jar " + APK_TOOL + " d " + apkPath + " -o " + outputPath);
    }
}
