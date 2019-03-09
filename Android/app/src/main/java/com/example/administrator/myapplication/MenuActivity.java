package com.example.administrator.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton Menu_One;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Menu_One = (ImageButton)findViewById(R.id.imageButton);
        Menu_One.setOnClickListener(this);
    }

    public void onClick(View v) {
        if(v.getId()==R.id.imageButton){
            Intent chart = new Intent(MenuActivity.this,ChartActivity.class);
            //login.putExtra("UserName",UserName);
            startActivity(chart);
        }
    }
}
