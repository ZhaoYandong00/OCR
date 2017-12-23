package com.zyd.ocr;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * OCR工具包
 * Created by ZYD on 2017/12/23.
 */
class OCRUtils {
    /**
     * 文字识别
     *
     * @param bitmap   要识别的图
     * @param dataPath 训练库所在文件夹
     * @param dataName 训练库名字
     */
    static void ocr(final Bitmap bitmap, final String dataPath, final String dataName, final MyCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //使用TessBaseAPI
                TessBaseAPI baseApi = new TessBaseAPI();
                //初始化OCR的训练数据路径与语言
                baseApi.init(dataPath, dataName);
                //设置识别模式
                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_OSD_ONLY);
                //设置要识别的图片
                baseApi.setImage(bitmap);
                //识别结果
                String result = baseApi.getUTF8Text();

                callBack.response(result);
                baseApi.clear();
                baseApi.end();
            }
        }).start();
    }
}
