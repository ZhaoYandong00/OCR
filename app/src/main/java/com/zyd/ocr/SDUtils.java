package com.zyd.ocr;

import android.content.Context;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;

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
        if (file.exists()) {
            Assert.assertTrue(file.delete());
        } else {
            File fileParent = new File(file.getParent());
            if (!fileParent.exists())
                Assert.assertTrue(fileParent.mkdirs());
        }
        try {
            Assert.assertTrue(file.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
