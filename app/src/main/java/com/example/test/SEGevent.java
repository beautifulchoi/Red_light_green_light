package com.example.test;

import android.util.Log;
import java.sql.Timestamp;

public class SEGevent{
    static {
        System.loadLibrary("segment");
    }

    private native static int openDriver(String path);
    private native static void closeDriver();
    private native static void writeDriver(byte[] data, int length);
    boolean mThreadRun, mStart;
    SegmentThread mThread;
    //int data_int=0; 전역변수 사용 안하고 클래스에 생성자로 넣어줬음

    protected int init(){
        if (openDriver("/dev/sm9s5422_segment") < 0) {
            Log.e("SEGMENT", "driver not open");
            return -1;
        }
        else Log.d("SEGMENT", "get driver");

        return 1;
    }

    protected void close(){
        closeDriver();
    }

    protected void control_seg(int timelap){//세그먼트 돌리는 코드 ->segThread의 data_int를 카운트다운
        mThreadRun=true;
        mStart=true;
        Log.d("segment thread on", "세그먼트 쓰레드 동작");
        mThread=new SegmentThread(timelap);
        mThread.start();
        }

    public void stop() throws InterruptedException {
        mStart=false;
        mThreadRun=false;
        mThread.sleep(100);
        mThread.interrupt();
    }

    protected class SegmentThread extends Thread{ //쓰레드 클래스 상속
        int data_int;
        long before_time;
        long now_time;
        private SegmentThread(int data_int){
            this.data_int=data_int;
        }
        @Override
        public void run(){
            super.run();
            while(mThreadRun){ //쓰레드 돌아가면
                byte[] n= {0,0,0,0,0,0,0};

                if(mStart==false) {writeDriver(n, n.length);}//스타트 안했으면 드라이버가서 읽어라
                else{
                    for(int i=0; i<30; i++){
                        n[0] = (byte) (this.data_int % 1000000 / 100000);
                        n[1] = (byte) (this.data_int % 100000 / 10000);
                        n[2] = (byte) (this.data_int % 10000 / 1000);
                        n[3] = (byte) (this.data_int % 1000 / 100);
                        n[4] = (byte) (this.data_int % 100 / 10);
                        n[5] = (byte) (this.data_int % 10 );
                        writeDriver(n, n.length);}
                    before_time=System.currentTimeMillis();
                    now_time=System.currentTimeMillis();
                    while(now_time-before_time<1000) {
                        try {
                            Thread.sleep(10); //일단 일케 하고 초단위 밀리면 타이머 사용하자
                            now_time = System.currentTimeMillis();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(this.data_int>1){ //0->1 수정
                        //Log.d("SEG-timecheck", ""+data_int);
                        this.data_int--;}
                    else{
                        try {
                            SEGevent.this.stop();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }
}
