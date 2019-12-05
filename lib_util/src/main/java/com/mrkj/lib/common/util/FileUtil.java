package com.mrkj.lib.common.util;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author
 * @date 2018/1/10 0010
 */

public class FileUtil {
    /**
     * 检验文件
     *
     * @param file 本地文件地址
     * @param md5  需要检验的MD5码
     * @return
     */
    public static boolean examinnation(String file, String md5) {
        MessageDigest degest;
        try {
            degest = MessageDigest.getInstance("MD5");
            FileInputStream inputStream = new FileInputStream(file);
            DigestInputStream dis = new DigestInputStream(inputStream, degest);
            //对于大文件或者网络文件使用输入流的形式要比字节数组方便很多也节省内存
            ByteArrayOutputStream fileOutput = new ByteArrayOutputStream();
            int size;
            byte[] buffer = new byte[1024];
            while ((size = dis.read(buffer)) != -1) {
                //跟读普通输入流是一样的，原理就是需要将输入流读完后，再调用digest方法才能获取整个文件的MD5
                fileOutput.write(buffer, 0, size);
            }
            fileOutput.flush();
            byte[] sumary = degest.digest();
            StringBuilder strBuffer = new StringBuilder();
            for (byte aSumary : sumary) {
                String tmp = Integer.toHexString(aSumary & 0xff);
                if (tmp.length() == 1) {
                    //如果这个字节的值小于16，那么转换的就只有一个字符，所以需要手动添加一个字符“0”，
                    tmp = "0" + tmp;
                }
                strBuffer.append(tmp);
            }
            return md5.endsWith(strBuffer.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从Url中获取文件名
     *
     * @param url
     * @return
     */
    public static String getNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return url;
        }
        return url.substring(index + 1);
    }

    public static String getPath(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return GetPathFromUri4kitkat.getPath(context, uri);
        } else {
            String path = "";
            if (uri.getScheme().equals("file")) {
                path = uri.getPath();
            } else {
                if (!uri.getScheme().equals("content")) {
                    path = "";
                } else {
                    Cursor exifInterface = context.getContentResolver()
                            .query(uri, new String[]{"_data"}, null, null, null);
                    if (exifInterface != null) {
                        exifInterface.moveToFirst();
                        path = exifInterface.getString(0);
                        exifInterface.close();
                    }

                }
            }
            return path;
        }
    }

    /**
     * 获取音频时常
     *
     * @param uri
     * @return
     */
    public static long getRingDuring(Context c, Uri uri) {
        long duration = 0;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

        try {
            if (uri != null) {
                mmr.setDataSource(c, uri);
            }
            duration = Long.valueOf(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mmr.release();
        }
        return duration / 1000;
    }

    public static String getMediaFileName(Context c, Uri uri) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        String name = "";
        String articl = "";
        try {
            if (uri != null) {
                mmr.setDataSource(c, uri);
            }
            name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            articl = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mmr.release();
        }
        if (!TextUtils.isEmpty(articl)) {
            return name + "-" + articl;
        }
        return name;
    }

    /**
     * 解压缩功能.
     * 将zipFile文件解压到folderPath目录下.
     *
     * @throws Exception
     */
    public static int upZipFile(File zipFile, String folderPath) throws ZipException, IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                Log.d("upZipFile", "ze.getName() = " + ze.getName());
                String dirstr = folderPath + ze.getName();
                //dirstr.trim();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                Log.d("upZipFile", "str = " + dirstr);
                File f = new File(dirstr);
                f.mkdir();
                continue;
            }
            Log.d("upZipFile", "ze.getName() = " + ze.getName());
            File resultFile = new File(folderPath, ze.getName());
            File resultFileDir = resultFile.getParentFile();
            if (!resultFileDir.exists()) {
                resultFileDir.mkdirs();
            }
            if (resultFile.exists()) {
                resultFile.delete();
            }
            resultFile.createNewFile();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(resultFile));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        return 0;
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    //substr.trim();
                    substr = new String(substr.getBytes("8859_1"), "GB2312");

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ret = new File(ret, substr);

            }
            Log.d("upZipFile", "1ret = " + ret);
            if (!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length - 1];
            try {
                //substr.trim();
                substr = new String(substr.getBytes("8859_1"), "GB2312");
                Log.d("upZipFile", "substr = " + substr);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ret = new File(ret, substr);
            Log.d("upZipFile", "2ret = " + ret);
            return ret;
        }
        return ret;
    }

    public static void decryByBase64(String base64, String path) {
        byte[] bitmapArray;
        bitmapArray = Base64.decode(base64, Base64.DEFAULT);
        File file = new File(path);
        try {
            if (!file.exists()) {
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bitmapArray);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            file.delete();
        }
    }




}
