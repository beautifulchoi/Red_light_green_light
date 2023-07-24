package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalTime;
import java.util.Random;

public class Test extends AppCompatActivity {
    GameThread gameThread;
    //mainHandler mainHandler;
    SEGevent mSEG;
    //모든 이벤트는 죄다 "쓰레드에" 넣어야함 !!! -> 게임 이벤트 쓰레드
    public class GameThread implements Runnable {
        private boolean running;
        public int get_time=0;
        public GameThread( boolean running) {
            this.running=running;
        }
        private void whileMove(int time) throws InterruptedException {
            //움직이는거 허용됨 -> 걍 세그먼트 타임만 줄어들도록 하면됨
            mSEG.control_seg(time);
            long now_time = System.currentTimeMillis ();
            long after_time=now_time;
            Log.d("GameLoop", "움직 가능 시간: "+ time/1000);
            Log.d("GameLoop", "inital aftertime: "+after_time);
            while(after_time-now_time<time){
                Log.d("GameLoop", "남은 초"+(int)(after_time-now_time));
                Log.d("GameLoop", "aftertime: "+after_time);
                Thread.sleep(1000);
                after_time=System.currentTimeMillis ();
                this.get_time=(int)(after_time-now_time);
            }
        }
        private void whileStop(int time) throws InterruptedException {
            //움직이는거 불가능함 -> 1. 세그먼트 시간 줄어들도록, 2. 얼굴 객체의 움직임 감지(함수 따로 정의할 것)
            //mSEG.control_seg(time);
            long now_time = System.currentTimeMillis();
            long after_time = now_time;
            Log.d("GameLoop", "정지 시간: "+ time/1000);
            Log.d("GameLoop", "inital aftertime: " + after_time);
            while (after_time - now_time < time) {
                Log.d("GameLoop", "남은 초" + (int) (after_time - now_time));
                Log.d("GameLoop", "aftertime: " + after_time);
                Thread.sleep(1000);
                after_time = System.currentTimeMillis();
                this.get_time=(int)(after_time-now_time);
            }
        }
        @Override
        public void run() {
            while (running == true) {
                //랜덤 타임을 가져옴
                int moveTime =new Random().nextInt(5000) + 1000 ;//1~6초 사이의 시간 정의
                int stopTime =new Random().nextInt(5000) + 1000 ;
                try {
                    Log.d("GameLoop", "게임루프 동작 중");
                    whileMove(moveTime);//움직여도 되는 때 정의 -> 랜덤한 시간 동안 유지
                    Log.d("GameLoop", "게임루프 무브타임끝");
                    //현재 여기까지는 찍힘
                    whileStop(stopTime);//멈춰야하는 때의 이벤트 정의 -> 랜덤한 시간동안 유지
                    Log.d("GameLoop", "게임루프 스탑타임끝");
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //class mainHandler extends Handler{ //시간을 받아 다시 시간변화 쓰레드로 보내는 핸들러 클래스
    //    TextView tv;
    //    mainHandler(TextView tv){
    //        this.tv=tv;
    //    }
    //    public void handleMessage(@NonNull Message msg){
    //        super.handleMessage(msg);
    //        tv.setText((int) (msg.arg1));
    //    }
    //}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv= (TextView)findViewById(R.id.text1);
        //mainHandler=new mainHandler(tv);
        Handler mMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                tv.setText((int) (msg.arg1));
            }
        };

        gameThread =new GameThread(false);


        //led event setting TODO 폰 디버깅시 주석처리
        //Log.d("led", "load led driver file");
        //LEDevent mLED = new LEDevent();
        //int checkLED = mLED.init();
        //if (checkLED < 0) {
        //    Toast.makeText(Test.this, "driver open error", Toast.LENGTH_SHORT).show();
        //}
        ////seg event setting TODO 폰 디버깅시 주석처리
        //Log.d("SEG", "load seg driver file");
        //SEGevent mSEG = new SEGevent();
        //int checkSEG = mSEG.init();
        //if (checkSEG < 0) {
        //    Toast.makeText(Test.this, "driver open error", Toast.LENGTH_SHORT).show();
        //}
        ////seg event setting TODO 폰 디버깅시 주석처리
        //Log.d("BUTTON", "load seg driver file");
        //BUTTONevent mButton = new BUTTONevent(); //버튼 불러오면 자동으로 버튼 리스너 가동(할지는 모르겠다).. ㅎㅎ
        //int checkButton = mButton.init();
        //if (checkButton < 0) {
        //    Toast.makeText(Test.this, "driver open error", Toast.LENGTH_SHORT).show();
        //}

        //클릭시 gpio 메소드 // 테스트 위함
        Button test_btn1 = (Button) findViewById(R.id.test_button1);
        Button test_btn2 = (Button) findViewById(R.id.test_button2);
        test_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameThread.running=true;
                Log.d("Gameloop", "게임 스레드 시작");
                new Thread(gameThread).start();
                Log.d("Gameloop", "겜쓰레드 시간"+gameThread.get_time);//이니셜은 한번만 받음 동작중 변경점 받아오는게 안되네
                //mSEG.control_seg(1, 10);
                //mLED.control_led(1);
                //mButton.control_button(); //이벤트 정의(int형 return)
                    //mSEG.stop();
                    //mSEG.close();
                    //mLED.close();

                }
            });
        test_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameThread.running=false;
                Log.d("Gameloop", "게임 스레드 종료");
                Log.d("Gameloop", "겜쓰레드 시간"+gameThread.get_time); //종료 버튼 호출시에 받아와짐
                //new Thread(gameThread).start();
                //mSEG.control_seg(1, 10);
                //mLED.control_led(1);
                //mButton.control_button(); //이벤트 정의(int형 return)
                //mSEG.stop();
                //mSEG.close();
                //mLED.close();

            }
        });

    }
}

