package com.zyd.ocr;

import android.content.Context;

import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SD卡工具包
 * Created by ZYD on 2017/12/23.
 */
class SDUtils {
    /**
     * 从assets复制到指定位置
     *
     * @param context        上下文
     * @param targetPathName 目标位置
     * @param sourceName     源文件
     */
    static void assetsToSD(Context context, String targetPathName, String sourceName) {
        File file = new File(targetPathName);
        if (file.exists())
            Assert.assertTrue(file.delete());
        if (!file.exists()) {
            File fileParent = new File(file.getParent());
            if (!fileParent.exists())
                Assert.assertTrue(fileParent.mkdirs());
            try {
                Assert.assertTrue(file.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = new File(targetPathName);
        try (InputStream is = context.getAssets().open(sourceName); OutputStream os = new FileOutputStream(file)) {
            byte[] bytes = new byte[2048];
            int len;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
