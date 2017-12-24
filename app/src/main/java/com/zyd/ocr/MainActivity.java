package com.zyd.ocr;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATA_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String TESS_Data = DATA_PATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * 英文库
     */
    private static String ENGLISH_LANGUAGE = "eng";
    /**
     * assets中的文件名
     */
    private static String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 英语库名字
     */
    private static String ENGLISH_LANGUAGE_NAME = ENGLISH_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static String LANGUAGE_PATH = TESS_Data + File.separator + DEFAULT_LANGUAGE_NAME;
    /**
     * 英语库
     */
    private static String ENGLISH_PATH = TESS_Data + File.separator + ENGLISH_LANGUAGE_NAME;
    private android.widget.ImageView english, chinese;
    private TextView englishText, chineseText;
    /**
     * 线程通信
     */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage (Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString(DEFAULT_LANGUAGE);
            if (string != null && !string.isEmpty())
                chineseText.setText(string);
            string = bundle.getString(ENGLISH_LANGUAGE);
            if (string != null && !string.isEmpty())
                englishText.setText(string);
            return false;
        }
    });

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.english = findViewById(R.id.english);
        this.chinese = findViewById(R.id.chinese);
        this.englishText = findViewById(R.id.english_text);
        this.chineseText = findViewById(R.id.chinese_text);
        /**
         * 检查权限获取
         */
        MainActivityPermissionsDispatcher.getStorageAndCameraWithPermissionCheck(this);
        checkData();

    }

    /**
     * OCR识别
     * @param view 按键
     */
    public void ocr (View view) {
        checkData();
        //设置图片可以缓存
        chinese.setDrawingCacheEnabled(true);
        english.setDrawingCacheEnabled(true);
        //获取缓存的bitmap
        Bitmap bmp = chinese.getDrawingCache();
        OCRUtils.ocr(bmp, DATA_PATH, DEFAULT_LANGUAGE, mHandler);
        bmp = english.getDrawingCache();
        OCRUtils.ocr(bmp, DATA_PATH, ENGLISH_LANGUAGE, mHandler);
    }

    /**
     * 检查数据库是否存在，不存在，则复制
     */
    private void checkData () {
        File file = new File(LANGUAGE_PATH);
        if (!file.exists())
            SDUtils.assetsToSD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        file = new File(ENGLISH_PATH);
        if (!file.exists())
            SDUtils.assetsToSD(getApplicationContext(), ENGLISH_PATH, ENGLISH_LANGUAGE_NAME);
    }

    /**
     * 得到权限
     */
    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCamera () {

    }

    /**
     * 要求权限
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 结果数组
     */
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher
                .onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * 请求显示
     * @param request 请求
     */
    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCameraOnShow (final PermissionRequest request) {
        showRationaleDialog(request);
    }

    /**
     * 请求拒绝
     */
    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCameraDenied () {
        Toast.makeText(this, "你拒绝了该权限", Toast.LENGTH_SHORT).show();
    }

    /**
     * 不再提醒
     */
    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCameraNerver () {
        AskForPermission();
    }


    /**
     * 再用户拒绝过一次之后,告知用户具体需要权限的原因
     *
     * @param request 请求
     */
    private void showRationaleDialog (final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (@NonNull DialogInterface dialog, int which) {
                        request.proceed();//请求权限
                    }
                }).setTitle("请求权限").setCancelable(false).setMessage("我,存储，摄像头，开启授权").show();
    }

    /**
     * 被拒绝并且不再提醒,提示用户去设置界面重新打开权限
     */
    private void AskForPermission () {
        new AlertDialog.Builder(this).setTitle("缺少基础存储权限")
                .setMessage("当前应用缺少存储权限,请去设置界面授权.\n授权之后按两次返回键可回到该应用哦")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "你拒绝了该权限", Toast.LENGTH_SHORT)
                                .show();
                    }
                }).setNeutralButton("不在提醒", new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "不再提供权限", Toast.LENGTH_SHORT).show();
            }
        }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                startActivity(intent);
            }
        }).create().show();
    }

}
