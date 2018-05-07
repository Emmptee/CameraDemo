package com.yoyo.mediacodec;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.yoyo.mediacodec.camera.CameraInstance;
import com.yoyo.mediacodec.camera.CameraView;
import com.yoyo.mediacodec.play.Player;

import java.io.IOException;
import java.util.List;

public class RecordActivity extends Activity {

    private View layoutVideo;

    private SurfaceView svLeft;
    private CameraView svRight;
    private ImageView baffleView ,baffleBgView ;

    private MediaPlayer mp = new MediaPlayer();

    private Player player;

    private Camera mCamera;

    private Camera.Size mVideoSize;

    private MediaRecorder mediaRecorder;

    private MediaProjection mediaProjection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        layoutVideo = findViewById(R.id.layout_video);
        svLeft = findViewById(R.id.sv_left);
        svRight = findViewById(R.id.sv_right);


        player = new Player(svLeft);

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.reset();
                mediaRecorder.start();
                player.playUrl("/mnt/sdcard/ffmpeg/666.mp4");
            }
        });

        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });


//
//        final int toOpen = getCurrentCameraId();
//        mCamera = Camera.open(toOpen);
//
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
//        if (videoSizes == null || videoSizes.size() == 0) {
//            videoSizes = parameters.getSupportedPreviewSizes();
//        }
//        mVideoSize = chooseVideoSize(videoSizes);
//
//
//        parameters.setRotation(90);
//        mCamera.setDisplayOrientation(90);
//
//
//
//        mCamera.setParameters(parameters);
//
//        CameraCallback callback = new CameraCallback(mCamera, svRight);




        baffleView   = (ImageView)   findViewById(R.id.baffle_view);
        baffleBgView = (ImageView)   findViewById(R.id.baffle_bg_view);

        svRight.setBaffleView(baffleView, baffleBgView);


//        try {
//            MediaMuxer mediaMuxer = new MediaMuxer("/mnt/sdcard/ffmpeg/output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//
//
//            final MediaFormat format = mMediaCodec.getOutputFormat(); // API >= 16
//            mTrackIndex = mediaMuxer.addTrack(format);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mediaRecorder = new MediaRecorder();

//        mediaRecorder.setPreviewDisplay(svRight.getHolder().getSurface());
        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                Log.e("====", "i:"+i+",i1:"+i1);
            }
        });
        mediaRecorder.setCamera(CameraInstance.getInstance().getCamera());
        initRecorder();


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
