package com.yoyo.mediacodec.camera.callback;

/**
 * 摄像头预览窗口变换的时候调用
 * @author WSH
 *
 */
public interface CameraViewChangeCallback
{
    /**
     * 预览穿过口变化是调用
     * @param width
     * @param height
     */
    public void onViewChanged(int width, int height);
}
