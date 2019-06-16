package com.example.anirudh.locationman;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Button test=findViewById(R.id.button4);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        Button dtw=findViewById(R.id.button5);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtw();
            }
        });
    }
    void test()
    {
        Intent intent=new Intent(Home.this,MainActivity.class);
        startActivity(intent);
    }
    void dtw()
    {
        Intent intent=new Intent(Home.this,DTW.class);
        startActivity(intent);
    }

}
