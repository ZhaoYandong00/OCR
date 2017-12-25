package com.zyd.ocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * 相机
 * Created by ZYD on 2017/12/25.
 */

public class CameraActivity {
    /**
     * 摄像头视图
     */
    private SurfaceView mSurfaceView;
    /**
     * 控制
     */
    private SurfaceHolder mSurfaceHolder;
    /**
     * 摄像头管理器
     */
    private CameraManager mCameraManager;
    /**
     * 相机捕获
     */
    private CameraCaptureSession mCameraCaptureSession;
    /**
     * 相机
     */
    private CameraDevice mCameraDevice;
    /**
     * 图像读取
     */
    private ImageReader mImageReader;
    /**
     * 线程通信
     */
    private Handler mHandler;
    /**
     * 照片视图
     */
    private ImageView mImageView;

    /**
     * 初始化
     */
    public void initView () {
        //得到控制器
        mSurfaceHolder = mSurfaceView.getHolder();
        //保持屏幕打开
        mSurfaceHolder.setKeepScreenOn(true);
        //增加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated (SurfaceHolder holder) {
                initCamera();
            }

            @Override
            public void surfaceChanged (SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed (SurfaceHolder holder) {
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    CameraActivity.this.mCameraDevice = null;
                }
            }
        });
    }

    private void initCamera () {
        //新建ImageReader
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1);
        //添加监听
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable (ImageReader reader) {
                //关闭摄像头
                mCameraDevice.close();
                //隐藏mSurfaceView,并重新布局
                mSurfaceView.setVisibility(View.GONE);
                //显示视图
                mImageView.setVisibility(View.VISIBLE);
                //得到图片
                Image image = reader.acquireNextImage();
                //得到图片缓冲
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                //由缓冲区存入字节数组
                buffer.get(bytes);
                //得到位图
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    mImageView.setImageBitmap(bitmap);
                }
            }
        }, mHandler);
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened (@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            takePreview();
        }

        @Override
        public void onDisconnected (@NonNull CameraDevice camera) {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                CameraActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError (@NonNull CameraDevice camera, int error) {

        }
    };

    private void takePreview () {
        try {
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(
                    Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured (@NonNull CameraCaptureSession session) {
                            if (mCameraDevice == null)
                                return;
                            mCameraCaptureSession = session;
                            try {
                                //自动对焦
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                                          CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                //闪光灯
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                                          CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                CaptureRequest previewRequest = previewRequestBuilder.build();
                                mCameraCaptureSession
                                        .setRepeatingRequest(previewRequest, null, mHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed (@NonNull CameraCaptureSession session) {

                        }
                    }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePicture () {
        if (mCameraDevice == null)
            return;
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                      CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                      CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, mHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
