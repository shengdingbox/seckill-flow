package com.seckillflow.utils;


import com.seckillflow.core.executor.JobExecution;

import java.io.*;
import java.nio.charset.Charset;

public class Shells {

    public static JobExecution exec(String command) throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        Process p = doExec(command);
        stringBuilder.append(copyToString(p.getInputStream(), Charset.defaultCharset()));
        int exitVal = p.waitFor();
        if(exitVal != 0) {
            stringBuilder.append(copyToString(p.getErrorStream(), Charset.defaultCharset()));
        }
        stringBuilder.append("\n");
        stringBuilder.append("exitVal:").append(exitVal);
        return new JobExecution(exitVal, stringBuilder.toString());
    }

    private static Process doExec(String command) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if(isWindows) {
            return Runtime.getRuntime().exec(new String[] {
                "cmd.exe", "/c", command
            });
        } else {
            return Runtime.getRuntime().exec(new String[] {
                "bash", "-c", command
            });
        }
    }

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        if(in == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char buffer[] = new char[4096];
        for(int bytesRead = -1; (bytesRead = reader.read(buffer)) != -1;) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }
}
