package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity
    {
    private static final String TAG = "GameActivity";

    private int RESULT_PERMISSIONS = 100;
    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView capturedImageHolder;
    public static MainActivity getInstance;
    private FaceOverlayView mFaceView;

    private Bitmap captured_bitmap;

    public LEDevent mLED=new LEDevent();
    public SEGevent mSEG= new SEGevent();
    public BUTTONevent mButton=new BUTTONevent();

    //캡쳐 당기고 이미지 뷰를 엔딩 인텐트로 넘길꺼임
    public Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("Endgame", "카메라 찍기 실행");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int w= bitmap.getWidth();
            int h= bitmap.getHeight();
            Matrix mtx=new Matrix();
            mtx.postRotate(180);
            mtx.postScale(0.25f, 0.25f);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0, w,h, mtx, true);
            MainActivity.this.captured_bitmap=Bitmap.createScaledBitmap(rotatedBitmap,600,
                    1000,true);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "onCreate 메소드");
        setContentView(R.layout.activity_game); //게임 레이어로 설정
        requestPermissionCamera(); //카메라 권한 가져옴(핸드폰에서 작업할 때는 필요)
        mCamera=CameraPreview.getCameraInstance();
        mCamera.setDisplayOrientation(180); //이부분 수정 90->180 TODO 폰디버깅시 90으로, 보드에서 180

        //led event setting TODO 폰 디버깅시 주석처리
        Log.d("led", "load led driver file");
        LEDevent mLED = new LEDevent();
        int checkLED = mLED.init();
        if (checkLED < 0) {
            Toast.makeText(MainActivity.this, "driver open error", Toast.LENGTH_SHORT).show();
        }
        //seg event setting TODO 폰 디버깅시 주석처리
        Log.d("SEG", "load seg driver file");
        SEGevent mSEG = new SEGevent();
        int checkSEG = mSEG.init();
        if (checkSEG < 0) {
            Toast.makeText(MainActivity.this, "driver open error", Toast.LENGTH_SHORT).show();
        }
        //button event setting TODO 폰 디버깅시 주석처리
        Log.d("BUTTON", "load seg driver file");
        BUTTONevent mButton = new BUTTONevent(); //버튼 불러오면 자동으로 버튼 리스너 가동(할지는 모르겠다).. ㅎㅎ
        int checkButton = mButton.init();
        if (checkButton < 0) {
            Toast.makeText(MainActivity.this, "driver open error", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart 메소드");
        mFaceView=new FaceOverlayView(this);
        mPreview= new CameraPreview(this, mCamera, mFaceView);
        FrameLayout preview =(FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        TextView text= (TextView)findViewById(R.id.text1);
        addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        text.setText("얼굴을 화면에 놓으세요");
    }

    class GameTextHandler extends Handler{ //UI 처리할 핸들러 생성
        TextView tv;

        GameTextHandler(TextView tv){
            this.tv=tv;
        }
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            Bundle bundle =msg.getData();
            int value=bundle.getInt("value");
            switch(value){
                case 1:
                {
                    tv.setText("얼굴을 화면에 놓으세요");
                    break;
                }
                case 2:
                {
                    tv.setText("움직시간. 움직이세요");
                    break;
                }
                case 3:
                {
                    tv.setText("정지시간. 멈추세요");
                    break;
                }
                case 4:
                {
                    tv.setText("게임 종료");
                    break;
                }
            }
        }
    }


    //Gameloop Thread
    public class GameThread implements Runnable {
        private boolean running;
        public int get_time=0;
        private Handler handler;
        private Camera capCamera;
        private int if_success;
        Bitmap bitmap;

        public GameThread(boolean running, Handler gameTextHandler, Camera capCamera, Bitmap bitmap) {
            this.running=running;
            this.handler=gameTextHandler;
            this.capCamera=capCamera;
            this.bitmap=bitmap;

        }

        private int whileMove(int time) throws InterruptedException {
            //움직이는거 허용됨 -> 걍 세그먼트 타임만 줄어들도록 하면됨
            mLED.control_led(2);
            //mSEG.control_seg((int)(time/1000));//TODO 스레드 충돌 방지로 움직일 때는 껐음
            long now_time = System.currentTimeMillis ();
            long after_time=now_time;
            Camera.Face before_face=mPreview.face_one;
            Camera.Face now_face=before_face;
            Log.d("GameLoop", "움직 시간: "+ time/1000);
            //Log.d("Gameloop", "inital aftertime: "+after_time);
            while(after_time-now_time<time){
                //Log.d("Gameloop", "남은 초"+(int)(after_time-now_time));
                if ((mPreview.face_one.rect.right-mPreview.face_one.rect.left>400)
                        ||(mPreview.face_one.rect.top-mPreview.face_one.rect.bottom>500))// 현재 얼굴이 화면 크기 이상이라면 끝 - 얼굴 와꾸 사이즈 600*600이상이면 통과(nowface 가로 길이로 판정하자)
                {
                    this.if_success=1;
                    running=false;
                    mLED.control_led(3);
                    mSEG.close();
                    break;
                }
                //Log.d("Gameloop", "aftertime: "+after_time);
                Thread.sleep(100);
                after_time=System.currentTimeMillis ();
                this.get_time=(int)(after_time-now_time);

                /*현재 얼굴과 이전 얼굴 위치를 비교*/
                now_face=mPreview.face_one;
                Log.d("Gameloop-moving", "now_face"+now_face.rect);
                Log.d("Gameloop-moving", "before_face"+before_face.rect);

                if(now_face.rect==null){
                    Thread.sleep(100);
                    now_face=mPreview.face_one;//새로 추가함
                }

                this.get_time=(int)(after_time-now_time);
                before_face=now_face;
            }
            //mSEG.stop();//TODO 주석
            return 2;
        }
        private int whileStop(int time) throws InterruptedException {
            //움직이는거 불가능함 -> 1. 세그먼트 시간 줄어들도록, 2. 얼굴 객체의 움직임 감지(함수 따로 정의할 것)
            mLED.control_led(2);
            mSEG.control_seg((int)(time/1000));
            long now_time = System.currentTimeMillis ();
            long after_time=now_time;
            Camera.Face before_face=mPreview.face_one;
            Camera.Face now_face=before_face;
            Log.d("GameLoop", "정지 시간: "+ time/1000);
            //Thread.sleep(100);

            while(after_time-now_time<time){//정지 시간 타이머 on
                Log.d("Gameloop", "남은 초"+(int)(after_time-now_time));
                Thread.sleep(1000);
                after_time=System.currentTimeMillis ();

                /*현재 얼굴과 이전 얼굴 위치를 비교*/
                now_face=mPreview.face_one;
                Log.d("Gameloop-moving", "now_face"+now_face.rect);
                Log.d("Gameloop-moving", "before_face"+before_face.rect);

                if(now_face.rect==null){
                    Thread.sleep(100);
                    now_face=mPreview.face_one;//새로 추가함
                }
                RectF check_box= new RectF((float)(before_face.rect.left*0.1), (float) (before_face.rect.top*0.1),
                        (float) (before_face.rect.right*0.1), (float) (before_face.rect.bottom*0.1));//이전 얼굴 박스의 0.1크기 생성
                Log.d("Gameloop-checkbox", ""+check_box);
                if ((check_box.contains((float) (now_face.rect.centerX()*0.1),
                        (float) (now_face.rect.centerY()*0.1))!=true))// 이전 얼굴 사각형에 현재 얼굴의 중심이 들어가면 out
                {
                    Log.d("Gameloop-moving", "벗어남!!");
                    this.if_success=-1;
                    running=false;
                    mLED.control_led(1);
                    mSEG.stop();
                    mSEG.close();
                    break;
                }
                this.get_time=(int)(after_time-now_time);
                before_face=now_face;
            }
            mSEG.stop();
            return 3;
        }

        @Override
        public void run() {
            while (running == true) {//TODO 현재 move와 stop 세그먼트에서 충돌있는중임-> move에서 세그먼트 안쓰는거로
                //랜덤 타임을 가져옴
                int moveTime =new Random().nextInt(2000) + 3000 ;//2~6초 사이의 시간 정의
                int stopTime =new Random().nextInt(2000) + 3000 ;

                Bundle bundle= new Bundle();
                Message message;
                int value;
                try {
                    Log.d("GameLoop", "게임루프 동작 중");
                    //stop time throw
                    message=this.handler.obtainMessage();
                    bundle.putInt("value", 2);
                    message.setData(bundle);
                    this.handler.sendMessage(message);
                    whileMove(moveTime);//움직여도 되는 때 정의 -> 랜덤한 시간 동안 유지
                    Log.d("GameLoop", "게임루프 무브타임끝");
                    //mSEG.stop(); //TODO 움직이는 시간에는 안써서 주석처리함
                    //Thread.sleep(500);//TODO 움직 시간에 안쓰기 때문에 주석처리함
                    //stop time throw
                    bundle.putInt("value", 3);
                    message=this.handler.obtainMessage();
                    message.setData(bundle);
                    this.handler.sendMessage(message);
                    whileStop(stopTime);//멈춰야하는 때의 이벤트 정의 -> 랜덤한 시간동안 유지
                    Log.d("GameLoop", "게임루프 스탑타임끝");
                    mSEG.stop();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running!=true){
                    value=4;
                    message=this.handler.obtainMessage();
                    bundle.putInt("value", value);
                    message.setData(bundle);
                    this.handler.sendMessage(message);
                    Log.d("Gameloop", "게임종료");

                    //결과 화면 넘기기
                    if(this.if_success==-1){
                        Intent intent = new Intent(MainActivity.this, EndFailActivity.class);
                        Log.d("GameEnd", "액티비티 이동");

                        this.capCamera.takePicture(null,null, MainActivity.this.pictureCallback);
                        Log.d("GameEnd", "찍기동작 완료");
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        MainActivity.this.captured_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        intent.putExtra("captured",byteArray);
                        startActivity(intent);
                    }
                    else if(this.if_success==1){
                        Intent intent = new Intent(MainActivity.this, EndSuccessActivity.class);
                        Log.d("GameEnd", "액티비티 이동");

                        this.capCamera.takePicture(null,null, MainActivity.this.pictureCallback);
                        Log.d("GameEnd", "찍기동작 완료");
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        MainActivity.this.captured_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        intent.putExtra("captured",byteArray);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    public class RunGame implements Runnable{
        Handler handler;
        Camera camera;
        public RunGame(Handler handler, Camera camera){
            this.handler=handler;
            this.camera=camera;
        }
        @Override
        public void run() {
            while(true) {
                Log.d("Gameloop-initial", "얼굴 없음");
                Camera.Face check_face=mPreview.face_one;
                if(check_face.rect!=null) {
                    Log.d("Gameloop-initial", "얼굴 체킹, 겜 시작");
                    GameThread gameinst = new GameThread(true, handler, this.camera,
                            MainActivity.this.captured_bitmap);
                    if(mButton.control_button()==5)
                            {
                                mCamera.takePicture(null,null, MainActivity.this.pictureCallback);
                                mCamera.startPreview();
                                new Thread(gameinst).start();
                                break;
                            }
                }
                try {
                    Thread.sleep(100);
                    //Log.d("Gameloop", "겜 밖에서 겟타임"+gameinst.get_time);//쓰레드 돌고 바로난 직후 것이 바로 찍힘
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView tv = (TextView) findViewById(R.id.text1);
        GameTextHandler gameTextHandler= new GameTextHandler(tv);
        //mCamera.takePicture(null,null, MainActivity.this.pictureCallback);
        //Log.d("GameEnd", "찍기동작 완료");
        new Thread(new RunGame(gameTextHandler,mCamera)).start();
        //mCamera.takePicture(null,null, MainActivity.this.pictureCallback);
        Log.d("GameEnd", "찍기동작 완료");

    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("GameEnd", "onPause 동작");
        releaseMediaRecorder();
        mSEG.close();
        mLED.close();
        mButton.close();
        releaseCamera();
    }

    private void releaseMediaRecorder() {
        if(mCamera!=null)
        {mCamera.lock();}
    }

    private void releaseCamera(){
        if (mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("onCreate", "onRestart 메소드");
        setContentView(R.layout.activity_game); //게임 레이어로 설정
        requestPermissionCamera(); //카메라 권한 가져옴(핸드폰에서 작업할 때는 필요)
        mCamera=CameraPreview.getCameraInstance();
        mCamera.setDisplayOrientation(180); //이부분 수정 90->180 TODO 폰디버깅시 90으로, 보드에서 180
        //led event setting TODO 폰 디버깅시 주석처리
        Log.d("led", "load led driver file");
        LEDevent mLED = new LEDevent();
        int checkLED = mLED.init();
        if (checkLED < 0) {
            Toast.makeText(MainActivity.this, "driver open error", Toast.LENGTH_SHORT).show();
        }
        //seg event setting TODO 폰 디버깅시 주석처리
        Log.d("SEG", "load seg driver file");
        SEGevent mSEG = new SEGevent();
        int checkSEG = mSEG.init();
        if (checkSEG < 0) {
            Toast.makeText(MainActivity.this, "driver open error", Toast.LENGTH_SHORT).show();
        }
        //button event setting TODO 폰 디버깅시 주석처리
        Log.d("BUTTON", "load seg driver file");
        BUTTONevent mButton = new BUTTONevent(); //버튼 불러오면 자동으로 버튼 리스너 가동(할지는 모르겠다).. ㅎㅎ
        int checkButton = mButton.init();
        if (checkButton < 0) {
            Toast.makeText(MainActivity.this, "driver open error", Toast.LENGTH_SHORT).show();
        }
    }


    // 카메라 권한 메소드
    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);
            }else {
                setInit();
            }
        }else{  // version 6 이하일때
            setInit();
            return true;
        }
        return true;
    }
    private void setInit(){
        getInstance = this;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (RESULT_PERMISSIONS == requestCode) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허가시
                setInit();
            } else {
                // 권한 거부시
            }
            return;
        }
    }

}