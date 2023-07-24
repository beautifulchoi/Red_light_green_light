package com.example.test;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EndFailActivity extends AppCompatActivity { //실패 시 얼굴 캡쳐 후 grayscale
    private ImageView captured_image;
    private TextView tv;
    private ImageView capturedImageHolder;
    public Bitmap buf_bitmap;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("picture_change");
    }

    public native Bitmap grayGPU(Bitmap bitmap);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        Log.d("EndGame", "fail");
        captured_image=(ImageView)findViewById(R.id.captured_image);
        tv=(TextView)findViewById(R.id.text);

        //BitmapFactory.Options options =new BitmapFactory.Options();
        //options.inPreferredConfig=Bitmap.Config.ARGB_8888;
        //buf_bitmap=BitmapFactory.decodeFile("/data/local/tmp/ending_img.jpg", options);
        //Bitmap result_bitmap = grayGPU(buf_bitmap);
        //captured_image.setImageBitmap(result_bitmap);

        Intent intent = new Intent(getIntent());
        byte[] byteArray = intent.getByteArrayExtra("captured");
        Bitmap captured_bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        captured_bitmap=grayGPU(captured_bitmap);
        captured_image.setImageBitmap(captured_bitmap);
        tv.setText("실패 ㅠㅠ");

    }

}
