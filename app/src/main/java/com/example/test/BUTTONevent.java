package com.example.test;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public class BUTTONevent implements InterruptListener{
    ButtonDriver mDriver=new ButtonDriver();
    static int which_button;
    protected int init(){ //호출하는 순간 버튼 쓰레드 동작하게 됨
        if (mDriver.open("/dev/sm9s5422_interrupt") < 0) {
            Log.e("BUTTON", "driver not open");
            return -1;
        }
        else Log.d("BUTTON", "get driver");
        mDriver.setListener(this);
        return 1;
    }

    protected void close(){
        mDriver.close();
    }

    protected int control_button(){//버튼 컨트롤-> 전역변수 which_button 이용
        return which_button;
    }

    public Handler mHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            switch(msg.arg1){
                case 1:
                    Log.d("업","업");
                    break;

                    case 2: Log.d("다운","다운");
                    break;

                    case 3: Log.d("왼쪽","왼쪽");
                    break;

                    case 4: Log.d("오른쪽","오른쪽");
                    break;

                    case 5: Log.d("센터","센터");
                    break;
            }
        }
    };

    @Override
    public void onReceive(int val){
        Message text = Message.obtain();
        text.arg1=val;
        which_button=val;
        mHandler.sendMessage(text);
    }
}





