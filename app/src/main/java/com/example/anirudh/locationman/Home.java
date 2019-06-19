package com.example.anirudh.locationman;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Home extends AppCompatActivity {
String m_Text;
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
        dtw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dtw();
            }
        });
    }
    void test() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the polling rate");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


// Set up the buttons
        builder.setPositiveButton("START", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();

                Intent intent = new Intent(Home.this, MainActivity.class);
                intent.putExtra("poll", m_Text);
                startActivity(intent);
          }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    void dtw()
    {
        Intent intent=new Intent(Home.this,DTWjava.class);
        startActivity(intent);
    }

}
