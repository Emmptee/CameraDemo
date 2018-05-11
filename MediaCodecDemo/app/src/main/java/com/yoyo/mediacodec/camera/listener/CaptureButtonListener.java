package com.yoyo.mediacodec.camera.listener;

/**
 * @author Qi
 * @date 2018/05/10
 */
public interface CaptureButtonListener {
    void takePictures();

    void recordShort(long time);

    void recordStart();

    void recordEnd(long time);

    void recordZoom(float zoom);

    void recordError();
}
