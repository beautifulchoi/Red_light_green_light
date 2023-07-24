package com.example.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera.Face;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private FaceOverlayView mFaceView;
    Camera.Face face_one;

    public CameraPreview(Context context, Camera camera, FaceOverlayView faceView){
        super(context);
        mCamera=camera;
        mHolder=getHolder();
        mHolder.addCallback(this);
        mFaceView=faceView;
        face_one=new Camera.Face();
    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                get_face(faces[0]);//얼굴 여러개면 첫번 째 얼굴만 가져옴
                // Update the view now!
                mFaceView.setFaces(faces);
            }
        }
    };
    public void get_face(Camera.Face face){
        this.face_one=face;
    };

    public void startFaceDetection(){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            mCamera.startFaceDetection();
            Log.d("FaceDetection-initial", "얼굴검출시작");
            Log.d("FaceDetection-initial", "가능여부: "+params.getMaxNumDetectedFaces());
        }
        else{
            Log.e("FaceDetection-error", "얼굴검출불가능");
        }
    }

    public void surfaceCreated(SurfaceHolder holder){
        try{
            if(mCamera==null){
                mCamera.setFaceDetectionListener(faceDetectionListener);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                startFaceDetection();
            }
        }catch (IOException e){
            Log.d(VIEW_LOG_TAG, "에러!"+e.getMessage());
        }
    }

    public void refreshCamera(Camera camera){
        if (mHolder.getSurface()==null){
            return;
        }
        try{
            mCamera.stopPreview();
        } catch (Exception e){
        }
        setCamera(camera);
        try{
            mCamera.setFaceDetectionListener(faceDetectionListener);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            startFaceDetection();

        }catch (Exception e){
            Log.d(VIEW_LOG_TAG, "에러!"+e.getMessage());
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
        refreshCamera(mCamera);
    }

    public void setCamera(Camera camera){
        mCamera=camera;
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        mCamera.release();
    }

    public boolean capture(Camera.PictureCallback callback) {
        if (mCamera != null) {
            mCamera.takePicture(null, null, callback);
            return true;
        } else {
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e) {
        }
        return c;
    }
}
