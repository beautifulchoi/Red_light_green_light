package com.example.test;


import android.os.Handler;
import android.os.Looper;

public class ButtonDriver implements InterruptListener{

    private boolean mConnectFlag;
    private TranseThread mTranseThread;
    private InterruptListener mMainActivity;
    static{
        System.loadLibrary("button");
    }

    private native static int openDriver(String path);
    private native static void closeDriver();
    private native char readDriver();
    private native int clickButton();

    public ButtonDriver(){mConnectFlag=false;}
    @Override
    public void onReceive(int val){

        if(mMainActivity!=null){ //버튼이 눌리면 mainActivity꺼 onReceive를 호출해
            mMainActivity.onReceive(val);
        }
    }
    public void setListener(InterruptListener a){ mMainActivity =a;}
    public int open(String driver){
        if(mConnectFlag) return -1;

        if(openDriver(driver)>0){
            mConnectFlag=true;
            mTranseThread=new TranseThread();
            mTranseThread.start();
            return 1;
        } else{
            return -1;
        }
    }
    public void close(){
        if(!mConnectFlag) return;
        mConnectFlag =false;
        closeDriver();
    }

    protected void finalize() throws Throwable{
        close();
        super.finalize();
    }

    public char read(){ return readDriver();}

    private class TranseThread extends Thread{

        @Override
        public void run(){
            super.run();
            try{
                while(mConnectFlag){
                    try {
                        onReceive(clickButton());
                        Thread.sleep(100);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
            }
        }
    }
}

