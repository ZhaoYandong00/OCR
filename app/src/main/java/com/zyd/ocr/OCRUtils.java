package com.zyd.ocr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
     * @param handler  线程通信Handler
     */
    static void ocr (final Bitmap bitmap, final String dataPath, final String dataName,
                     final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run () {
                //使用TessBaseAPI
                TessBaseAPI baseApi = new TessBaseAPI();
                //初始化OCR的训练数据路径与语言
                baseApi.init(dataPath, dataName);
                //设置识别模式
                // baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_OSD_ONLY);
                //设置要识别的图片
                baseApi.setImage(bitmap);
                //识别结果
                String result = baseApi.getUTF8Text();
                //日志
                Log.i(dataName, result);
                //新建message
                Message msg = handler.obtainMessage();
                //新建Bundle
                Bundle bundle = new Bundle();
                //Bundle写键值对
                bundle.putString(dataName, result);
                //bundle存入message
                msg.setData(bundle);
                //把消息发送给主程序
                handler.sendMessage(msg);
                //清除
                baseApi.clear();
                //结束
                baseApi.end();
            }
        }).start();
    }
}
