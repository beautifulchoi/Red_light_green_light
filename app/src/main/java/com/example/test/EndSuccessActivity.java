package com.example.test;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class EndSuccessActivity extends AppCompatActivity { //성공 시 얼굴 캡쳐
    private TextView tv;
    private int RESULT_PERMISSIONS = 100;
    private ImageView capturedImageHolder;
    public Bitmap buf_bitmap;

    static {
        System.loadLibrary("picture_change");
    }

    public native Bitmap blurGPU(Bitmap bitmap);


    static {
        if(!OpenCVLoader.initDebug()){
            Log.d("opencv", "안열려");
        }
        else{
            Log.d("opencv", "잘열려");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        Log.d("EndGame", "success");


        tv = (TextView) findViewById(R.id.text);


        capturedImageHolder = (ImageView) findViewById(R.id.captured_image);
        tv = (TextView) findViewById(R.id.text);
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //buf_bitmap = BitmapFactory.decodeFile("/data/local/tmp/lena.bmp", options);
        ////buf_bitmap = blurGPU(buf_bitmap);

        Intent intent = new Intent(getIntent());
        byte[] byteArray = intent.getByteArrayExtra("captured");
        Bitmap captured_bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        //captured_bitmap=blurGPU(captured_bitmap);

        detectEdge(captured_bitmap);
        capturedImageHolder.setImageBitmap(captured_bitmap);
        tv.setText("성공!");
    }

    public void detectEdge(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Mat edge = new Mat();
        Imgproc.Canny(src, edge, 50, 150);
        Utils.matToBitmap(edge, bitmap);
        src.release();
    }

}
