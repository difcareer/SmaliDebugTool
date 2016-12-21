package com.andr0day;

public class ProcessUtil {

    public static Process execute(String cmd) throws Exception {
        return Runtime.getRuntime().exec(cmd);
    }

    public static Process bakSmaliDex(String dexPath, String outputPath) throws Exception {
        return execute("java -jar baksmali-2.0.3.jar " + dexPath + " -o " + outputPath);
    }
}
