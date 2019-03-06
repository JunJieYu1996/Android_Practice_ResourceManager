package com.example.administrator.myapplication;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText user_name;
    private EditText user_password;
    private Button login;
    private Button exit;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user_name = (EditText)findViewById(R.id.user_name);
        user_password = (EditText)findViewById(R.id.user_password);
        login = (Button)findViewById(R.id.login);
        exit = (Button)findViewById(R.id.exit);

        login.setOnClickListener(this);
        exit.setOnClickListener(this);
    }
    public void onClick(View v) {
        if(v.getId()==R.id.login){
          String UserName = user_name.getText().toString();
          String UserPassword = user_password.getText().toString();
          if((UserName.equals("admin")) && (UserPassword.equals("admin"))){
              Toast.makeText(getApplicationContext(),"成功登录",Toast.LENGTH_SHORT).show();
              Intent login = new Intent(MainActivity.this,ChartActivity.class);
              login.putExtra("UserName",UserName);
              startActivity(login);
          }
          else {
              Toast.makeText(getApplicationContext(),user_name.getText()+" "+user_password.getText(),Toast.LENGTH_LONG).show();
              user_name.setText("");
              user_password.setText("");
          }
        }
        else if (v.getId()==R.id.exit){
              this.finish();
        }
    }
}
