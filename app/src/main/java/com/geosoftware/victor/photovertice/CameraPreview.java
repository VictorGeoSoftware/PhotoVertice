package com.geosoftware.victor.photovertice;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Victor on 4/1/15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mWidth;
    private int mHeight;

    public CameraPreview(Context context, Camera camera, int width, int height){
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            mCamera.setPreviewDisplay(holder);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mHeight, mWidth);
            mCamera.setParameters(parameters);

            mCamera.startPreview();
        }catch (Exception e){

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mHolder.getSurface() == null){
            return;
        }

        try{
            mCamera.stopPreview();
        }catch (Exception e){

        }

        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }catch (Exception e){

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
