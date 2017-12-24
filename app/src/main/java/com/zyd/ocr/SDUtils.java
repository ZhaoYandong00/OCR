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
    static void assetsToSD (Context context, String targetPathName, String sourceName) {
        File file = new File(targetPathName);
        //如果存在，删除
        if (file.exists())
            Assert.assertTrue(file.delete());
        //如果不存在，创建文件
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
        //准备写入文件
        file = new File(targetPathName);
        //复制文件
        try (InputStream is = context.getAssets()
                .open(sourceName); OutputStream os = new FileOutputStream(file)) {
            //缓存2M
            byte[] bytes = new byte[2048];
            int len;
            //复制开始
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            //写入结果写入到文件
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
