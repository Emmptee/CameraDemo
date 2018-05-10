package com.yoyo.mediacodec;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.socks.library.KLog;
import com.yoyo.mediacodec.camera.CameraInterface;
import com.yoyo.mediacodec.camera.CaptureButton;
import com.yoyo.mediacodec.camera.CountDownProgress;
import com.yoyo.mediacodec.camera.JCameraView;
import com.yoyo.mediacodec.camera.listener.CaptureButtonListener;
import com.yoyo.mediacodec.camera.listener.CaptureListener;
import com.yoyo.mediacodec.camera.listener.ErrorListener;
import com.yoyo.mediacodec.camera.state.CameraMachine;
import com.yoyo.mediacodec.camera.util.LogUtil;
import com.yoyo.mediacodec.camera.util.ScreenUtils;
import com.yoyo.mediacodec.play.Player;

import java.io.IOException;
import java.util.List;

public class RecordActivity extends Activity {

    private View layoutVideo;

    private SurfaceView svLeft;
    private JCameraView svRight;
    private ImageView baffleView ,baffleBgView ;
    //Camera状态机
    private CameraMachine mMachine;
    private Camera mCamera;


    private MediaPlayer mp = new MediaPlayer();

    private Player player;

    private Camera.Size mVideoSize;

    private MediaRecorder mediaRecorder;

    private MediaProjection mediaProjection;
    private VideoView mVideoview;
    private ImageView mImageview;
    private float screenProp = 0f;
    private CaptureButton mRecordButton;
    private int layout_width;
    private int zoomGradient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        layoutVideo = findViewById(R.id.layout_video);
        svLeft = findViewById(R.id.sv_left);
        svRight = findViewById(R.id.sv_right);
        mVideoview = findViewById(R.id.video_preview);
        mImageview = findViewById(R.id.image_photo);

        mRecordButton = findViewById(R.id.record_button);

        player = new Player(svLeft);

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mediaRecorder.reset();
//                mediaRecorder.start();
                player.playUrl("/mnt/sdcard/ffmpeg/666.mp4");
            }
        });

        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });
        //拍摄按钮
        findViewById(R.id.btn_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //暂停按钮
        findViewById(R.id.btn_stop_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //切换按钮
        findViewById(R.id.btn_turn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraInterface.getInstance().switchCamera(mVideoview.getHolder(),screenProp);

            }
        });


        mediaRecorder = new MediaRecorder();

//        mediaRecorder.setPreviewDisplay(svRight.getHolder().getSurface());
        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                Log.e("====", "i:"+i+",i1:"+i1);
            }
        });
//        mediaRecorder.setCamera(CameraInstance.getInstance().getCamera());
        initData();
        initRecordButton();

    }

    private int button_size = 50;
    private CameraMachine machine;
    private ErrorListener errorLisenter;

    private void initData() {
        layout_width = ScreenUtils.getScreenWidth(getApplicationContext());
        //缩放梯度
        zoomGradient = (int) (layout_width / 16f);
        LogUtil.i("zoom = " + zoomGradient);
        machine = new CameraMachine(getApplicationContext());
    }
    public void initRecordButton(){
        //拍照按钮

        mRecordButton.setCaptureButtonListener(new CaptureButtonListener() {

            @Override
            public void recordShort(final long time) {
               /* postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        machine.stopRecord(true, time);
                    }
                }, 1500 - time);*/

            }

            @Override
            public void recordStart() {
//                machine.record(mVideoview.getHolder().getSurface(), screenProp);
            }

            @Override
            public void recordEnd(long time) {
                machine.stopRecord(false, time);

            }

            @Override
            public void recordZoom(float zoom) {
                machine.zoom(zoom, CameraInterface.TYPE_RECORDER);

            }

            @Override
            public void recordError() {
                if (errorLisenter != null) {
                    errorLisenter.AudioPermissionError();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
     /*   if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {*/
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);

    }

    private void stop(){

        player.stop();

        if (mediaRecorder == null){
            return;
        }
            try {
                mediaRecorder.stop();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private int getCurrentCameraId() {

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return 0;
    }

    private static Camera.Size chooseVideoSize(List<Camera.Size> choices) {
        Camera.Size backupSize = null;
        for (Camera.Size size : choices) {
            if (size.height <= 640) {
                if (size.width == size.height * 3 / 4) {
                    return size;
                }
                if (size.height > size.width) {
                    backupSize = size;
                }
            }
        }
        if (backupSize != null) {
            return backupSize;
        }
        return choices.get(choices.size() - 1);
    }

    private void initRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile("/mnt/sdcard/ffmpeg/output.mp4");
//        mediaRecorder.setVideoSize(600, 600);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(30);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个录屏 Virtual
     */

    private void createVirtualDisplay() {
//        virtualDisplay = mediaProjection
//                .createVirtualDisplay("mediaprojection", width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder
//                        .getSurface(), null, null);

    }
}
