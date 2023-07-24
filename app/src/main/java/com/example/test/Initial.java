package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Initial extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);


        //시작 버튼 누르면 이동( 추후에 버튼 테스트 완료되면 gpio 버튼 누르는거로 바꿀 예정)
        Button start_btn = (Button) findViewById(R.id.start_button);
        start_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);// TODO 테스트시 테스트 액티비티로
                //Intent intent = new Intent(getApplicationContext(), Test.class); //TODO 보드에서는 위에꺼로
                startActivity(intent);
            }
        });
    }
}