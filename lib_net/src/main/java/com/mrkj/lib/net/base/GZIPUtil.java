package com.mrkj.lib.net.base;


import com.xx.lib.db.exception.ReturnJsonCodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author DingDong
 * @date 2011-11-30
 */
public class GZIPUtil {
    public static void gzip(InputStream inputStream) {
        try {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gop = new GZIPOutputStream(arrayOutputStream);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                gop.write(buffer, 0, len);
            }
            gop.finish(); // 这个在写入arrayOutputStream时一定要有，否则不能完全写入
            gop.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] unzip(InputStream in) throws ReturnJsonCodeException {
        // Open the compressed stream
        GZIPInputStream gin = null;
        ByteArrayOutputStream out = null;
        try {
            gin = new GZIPInputStream(in);
            out = new ByteArrayOutputStream();
            // Transfer bytes from the compressed stream to the output stream
            byte[] buf = new byte[1024 * 4];
            int len;
            while ((len = gin.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Close the file and stream
            gin.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new ReturnJsonCodeException("数据异常,请您稍后再试,或联系客服");
        } finally {
            try {
                if (gin != null) {
                    gin.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }
}