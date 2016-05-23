package com.zzg.demo_analytics;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzg.logservice.annotation.CustomerTrace;
import com.zzg.logservice.service.LogService;


public class AnalyticsHome extends Activity implements View.OnClickListener{
    boolean show = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.tv);
        Button bt = (Button) findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        Button bt2 = (Button) findViewById(R.id.button2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                "123".substring(10);
            }
        });

        Button bt4 = (Button) findViewById(R.id.button4);
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        Button bt5 = (Button) findViewById(R.id.button5);
        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    "123".substring(10);
                } catch (Exception exception) {
                }
            }
        });

        Button bt6 = (Button) findViewById(R.id.button6);
        bt6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int[] a = {1, 2};
                    int b = a[3];
                } catch (Exception exception) {
                    if (exception != null) {
                    }
                }
            }
        });
        Button bt7 = (Button) findViewById(R.id.button7);
        bt7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        Button bt8 = (Button) findViewById(R.id.button8);
        bt8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView) findViewById(R.id.tv);
                if (show) {
                    tv.setText(LogService.getLog());
                    System.out.print(LogService.getLog());
                    show = false;
                } else {
                    tv.setText("");
                    show = true;
                }
            }
        });
        Button bt9 = (Button) findViewById(R.id.button9);
        bt9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        Button bt10 = (Button) findViewById(R.id.button10);
        bt10.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        emptymethod();
    }

    @CustomerTrace(eventName = "定制事件",eventId = "dingzhishijian")
    public void emptymethod(){
    }
}