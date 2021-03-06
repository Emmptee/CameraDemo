package com.yoyo.mediacodec;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.yoyo.mediacodec.codec.AudioPackage;
import com.yoyo.mediacodec.codec.AvcAppendEncoder;
import com.yoyo.mediacodec.codec.AvcJoinEncoder;
import com.yoyo.mediacodec.codec.DecoderInterface;
import com.yoyo.mediacodec.codec.ImageUtils;
import com.yoyo.mediacodec.codec.MyFrame;
import com.yoyo.mediacodec.codec.VideoDecoder;

import java.io.File;

public class YoyoMain extends Activity implements DecoderInterface {

    public static final String TAG = "YoyoMain";

    private AvcAppendEncoder avcAppendEncoder;
    private AvcJoinEncoder avcJoinEncoder;

    private VideoDecoder decoder1, decoder2;


    String path1 = "/mnt/sdcard/ffmpeg/chengdu.mp4";
    String path2 = "/mnt/sdcard/ffmpeg/ping20s.mp4";

//    String path1 = "/mnt/sdcard/ffmpeg/video1.mp4";
//    String path2 = "/mnt/sdcard/ffmpeg/video2.mp4";

    int width1, width2, height1, height2;

    /**
     * 0 Append 1 Join
     */
    private int funType = 1;
    String resultAppend = "/mnt/sdcard/ffmpeg/append.mp4";
    String resultJoin = "/mnt/sdcard/ffmpeg/join.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mymain);

        decoder1 = new VideoDecoder(path1, "decode1");
        decoder2 = new VideoDecoder(path2, "decode2");
        decoder1.setCallBack(this);
        decoder2.setCallBack(this);


        if (funType == 0) {
            new File(resultAppend).delete();
            avcAppendEncoder = new AvcAppendEncoder(704, 380, 25,
                    ImageUtils.getBitrate(704, 380, ImageUtils.Quality.MIDDLE), resultAppend);
            avcAppendEncoder.decoderInterface = this;
            avcAppendEncoder.YUVQueue = YoyoContext.YUVQueue1;
            //启动编码线程
//        avcCodec.StartEncoderThread();
            avcAppendEncoder.configAudioEncodeCodec();
            avcAppendEncoder.configVideoEncodeCodec();

            decoder1.start();
        } else {
            new File(resultJoin).delete();
            //创建AvEncoder对象
            avcJoinEncoder = new AvcJoinEncoder(1344, 378, 25,
                    ImageUtils.getBitrate(1504, 400, ImageUtils.Quality.LOW), resultJoin);
            //启动编码线程
            avcJoinEncoder.YUVQueue1 = YoyoContext.YUVQueue1;
            avcJoinEncoder.YUVQueue2 = YoyoContext.YUVQueue2;
            avcJoinEncoder.StartEncoderThread();
//            if(width1 > 0 && height1 >0 && width2>0 && height2>0){
//                final int outHeight = height1 > height2 ? height1 : height2;
//                final int outWidth = width1 > width2 ? 2*width1 : 2*width2;
//            }

//            decoder1.start();
//            decoder2.start();
        }
        Log.e(TAG, "START");

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(YoyoMain.this, RecordActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != avcJoinEncoder) {
            avcJoinEncoder.StopThread();
        }
        if (null != avcAppendEncoder) {
            avcAppendEncoder.StopThread();
        }
    }


    @Override
    public void decodedByteBuffer(String path, MyFrame frame, int bufferIndex) {
        Log.e(YoyoContext.TAG, path + " " + bufferIndex);

        if (funType == 0) {
            avcAppendEncoder.putVideoFrameToCodec(frame);
            avcAppendEncoder.processVideoEncodeOut();
        } else {
            if (this.path1.equals(path)) {
                YoyoContext.YUVQueue1.add(frame);
            }
            if (this.path2.equals(path)) {
                YoyoContext.YUVQueue2.add(frame);
            }
            Log.e(TAG, "size1 = " + YoyoContext.YUVQueue1.size() + ", size2 = " + YoyoContext.YUVQueue2.size());
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void decodedVideoInfo(String path, int width, int height, float duration) {
        if (this.path1.equals(path)) {
            width1 = width;
            height1 = height;
        }
        if (this.path2.equals(path)) {
            width2 = width;
            height2 = height;
        }
    }

    @Override
    public void decodedEnd(String path) {
        if (this.path1.equals(path)) {
            decoder1.destroy();
            if (funType == 0) {
                decoder2.start();
            }
        }
    }

    @Override
    public void decodedAudioBuffer(String path, AudioPackage bytes) {
        if (funType == 0) {
            avcAppendEncoder.putAudioDataToCodec(bytes);
            avcAppendEncoder.processAudioEncodeOut();
        } else {
            if (path.equals(path2)) {
                YoyoContext.audioQueue.add(bytes);
            }
        }
    }
}
