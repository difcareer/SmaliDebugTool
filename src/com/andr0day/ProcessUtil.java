package com.andr0day;

public class ProcessUtil {
    private static final String BAK_SMALI = "baksmali.jar";
    private static final String APK_TOOL = "apktool.jar";


    public static Process execute(String cmd) throws Exception {
        return Runtime.getRuntime().exec(cmd);
    }

    public static Process bakSmaliDex(String dexPath, String outputPath) throws Exception {
        return execute("java -jar " + BAK_SMALI + " disassemble " + dexPath + " -o " + outputPath);
    }

    public static Process apkToolApk(String apkPath, String outputPath) throws Exception {
        return execute("java -jar " + APK_TOOL + " d " + apkPath + " -o " + outputPath);
    }
}
