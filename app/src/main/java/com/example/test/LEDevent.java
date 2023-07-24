package com.example.test;

import android.util.Log;

import java.sql.Timestamp;

public class LEDevent {
    static {
        System.loadLibrary("led");
    }

    private native static int openDriver(String path);

    private native static void closeDriver();

    private native static void writeDriver(byte[] data, int length);

    boolean mThreadRun = false;
    LEDThread mThread;

    protected int init() {
        if (openDriver("/dev/sm9s5422_led") < 0) {
            Log.e("LED", "driver not open");
            return -1;
        } else Log.d("LED", "get driver");

        return 1;
    }


    protected void control_led(int check_success) {
        mThread = new LEDThread(check_success);
        mThreadRun=true;
        Log.d("led thread on", "led 쓰레드 동작");
        mThread.start();
    }

    protected void close() {
        closeDriver();
    }

    private class LEDThread extends Thread { //쓰레드 클래스 상속
        int check_success;

        private LEDThread(int check_success) {
            this.check_success = check_success;
        }

        @Override
        public void run() {
            super.run();
            while (mThreadRun) { //쓰레드 돌아가면
                byte[] data = {0, 0, 0, 0, 0, 0, 0, 0};
                if (check_success == 1) { //성공시 0,1,2번 LED 순차적으로 on
                    data[0] = 1;
                    writeDriver(data, data.length); //하나키고 write 반복
                    try {
                        Thread.sleep(500); //0.5초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    data[1] = 1;
                    writeDriver(data, data.length);
                    try {
                        Thread.sleep(500); //0.5초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    data[2] = 1;
                    writeDriver(data, data.length);
                    try {
                        Thread.sleep(500); //0.5초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    close();
                    break;
                } else if (check_success == 2) {//매 턴마다
                    for (int i = 0; i < 8; i++) {
                        data[i] = 1;
                    }
                    writeDriver(data, data.length); //다켜고 write
                    try {
                        Thread.sleep(1000); //0.5초 대기
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < 8; i++) {
                        data[i] = 0;
                    }
                    writeDriver(data, data.length); //끄고 다시 write
                    close();
                    break;
                } else {
                    int cnt = 3;
                    while (cnt != 0) { //전체 불 켰다 껐다 5번 반복
                        for (int i = 0; i < 8; i++) {
                            data[i] = 1;
                        }
                        writeDriver(data, data.length); //다켜고 write
                        try {
                            Thread.sleep(1000); //0.5초 대기
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < 8; i++) {
                            data[i] = 0;
                        }
                        writeDriver(data, data.length); //끄고 다시 write
                        try {
                            Thread.sleep(1000); //0.5초 대기
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        cnt--;
                    }
                    close();
                    break;
                }
            }
        }
    }
}