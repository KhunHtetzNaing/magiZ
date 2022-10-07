package com.htetznaing.magiz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    public static boolean writeInputStream(InputStream input, OutputStream output) throws IOException {
        if (input==null || output==null)
            return false;
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        output.flush();
        output.close();
        input.close();
        return true;
    }
}
