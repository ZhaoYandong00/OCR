package com.zyd.ocr;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

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
    private TextView englishText, chineseText;
    private EditText name, specification, productNumber, productionDate, producer, inspector;
    private TextView warn;
    private ImageView OCRView;
    /**
     * 线程通信
     */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString(DEFAULT_LANGUAGE);
            if (string != null && !string.isEmpty()) {
                chineseText.setText(string);
                handleResult(string);
            }
            string = bundle.getString(ENGLISH_LANGUAGE);
            if (string != null && !string.isEmpty()) {
                englishText.setText(string);
            }
            string = bundle.getString("HTTP");
            if (string != null && !string.isEmpty()) {
                warn.setVisibility(View.VISIBLE);
                warn.setText(string);
            }
            return false;
        }
    });


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            warn.setText(null);
            warn.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.englishText = findViewById(R.id.english_text);
        this.chineseText = findViewById(R.id.chinese_text);
        this.name = findViewById(R.id.name);
        this.specification = findViewById(R.id.specification);
        this.productNumber = findViewById(R.id.productNO);
        this.productionDate = findViewById(R.id.productionDate);
        this.producer = findViewById(R.id.producer);
        this.inspector = findViewById(R.id.inspector);
        this.warn = findViewById(R.id.warn);
        name.addTextChangedListener(textWatcher);
        specification.addTextChangedListener(textWatcher);
        productionDate.addTextChangedListener(textWatcher);
        productNumber.addTextChangedListener(textWatcher);
        producer.addTextChangedListener(textWatcher);
        inspector.addTextChangedListener(textWatcher);
        this.OCRView = findViewById(R.id.main_image);
        // 检查权限获取
        MainActivityPermissionsDispatcher.getStorageAndCameraWithPermissionCheck(this);
        checkData();

    }


    /**
     * OCR识别
     *
     * @param view 按键
     */
    public void ocr(View view) {
        checkData();
        getPicture();
    }

    private void getPicture() {
        Intent intent = new Intent();
        // 开启Pictures画面Type设定为image
        intent.setType("image/*");
        // 使用Intent.ACTION_GET_CONTENT这个Action
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //取得相片后返回本画面
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                if (uri != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    OCRView.setImageBitmap(bitmap);
                    OCRUtils.ocr(bitmap, DATA_PATH, DEFAULT_LANGUAGE, mHandler);
                }
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(), e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理识别结果
     *
     * @param result 识别结果
     */
    private void handleResult(String result) {
        String regex = "1";
        // TODO
        if (result.matches(regex))
            name.setText(result);
    }

    public void commit(View view) {
        addProduction();
    }

    /**
     * 向服务器添加数据
     */
    private void addProduction() {
        String regex = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]|[0-9][1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)";
        String regex1 = "\\s*";
        if (name.getText().toString().matches(regex1)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("产品名称不能为空");
            return;
        }
        if (specification.getText().toString().matches(regex1)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("规格型号不能为空");
            return;
        }
        if (productNumber.getText().toString().matches(regex1)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("产品编号不能为空");
            return;
        }
        if (productionDate.getText().toString().matches(regex1)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("生产日期不能为空");
            return;
        } else if (!productionDate.getText().toString().matches(regex)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("生产日期格式不对");
            return;
        }
        if (producer.getText().toString().matches(regex1)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("生产人员不能为空");
            return;
        }
        if (inspector.getText().toString().matches(regex1)) {
            warn.setVisibility(View.VISIBLE);
            warn.setText("生产人员不能为空");
            return;
        }
        String url = "http://192.168.1.6:8080/record/add";
        String mode = "POST";
        Production production = new Production();
        production.setName(name.getText().toString());
        production.setSpecification(specification.getText().toString());
        production.setProductNumber(productNumber.getText().toString());
        production.setProductionDate(productionDate.getText().toString());
        production.setEndProductionDate(productionDate.getText().toString());
        production.setProducer(producer.getText().toString());
        production.setInspector(inspector.getText().toString());
        String param = production.toString();
        Log.i("Production", param);
        new MyHttpConnectionThread(url, mode, param, mHandler).start();
    }

    /**
     * 检查数据库是否存在，不存在，则复制
     */
    private void checkData() {
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
    void getStorageAndCamera() {

    }

    /**
     * 要求权限
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher
                .onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * 请求显示
     *
     * @param request 请求
     */
    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCameraOnShow(final PermissionRequest request) {
        showRationaleDialog(request);
    }

    /**
     * 请求拒绝
     */
    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCameraDenied() {
        Toast.makeText(this, "你拒绝了该权限", Toast.LENGTH_SHORT).show();
    }

    /**
     * 不再提醒
     */
    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getStorageAndCameraNerver() {
        AskForPermission();
    }


    /**
     * 再用户拒绝过一次之后,告知用户具体需要权限的原因
     *
     * @param request 请求
     */
    private void showRationaleDialog(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();//请求权限
                    }
                }).setTitle("请求权限").setCancelable(false).setMessage("我,存储，摄像头，开启授权").show();
    }

    /**
     * 被拒绝并且不再提醒,提示用户去设置界面重新打开权限
     */
    private void AskForPermission() {
        new AlertDialog.Builder(this).setTitle("缺少基础存储权限")
                .setMessage("当前应用缺少存储权限,请去设置界面授权.\n授权之后按两次返回键可回到该应用哦")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "你拒绝了该权限", Toast.LENGTH_SHORT)
                                .show();
                    }
                }).setNeutralButton("不在提醒", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "不再提供权限", Toast.LENGTH_SHORT).show();
            }
        }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                startActivity(intent);
            }
        }).create().show();
    }


}
