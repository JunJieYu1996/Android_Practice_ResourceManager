package com.example.administrator.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton Menu_One;
    private ImageButton Menu_Two;
    private ImageButton Menu_Three;
    private ImageButton Menu_Four;
    private TextView responseText;
    private TextView Timeshower;
    static int Abnormal_Percentage = 50;
    public static final int TIME_PAUSE = 1;
    public static final int TEXT_HELPER = 2;
    public static final int CHECKER_RESPONSE = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Menu_One = (ImageButton)findViewById(R.id.imageButton);
        Menu_Two = (ImageButton)findViewById(R.id.imageButton2);
        Menu_Three = (ImageButton)findViewById(R.id.imageButton3);
        Menu_Four = (ImageButton)findViewById(R.id.imageButton4);
        responseText = (TextView) findViewById(R.id.warnText);
        Timeshower = (TextView)findViewById(R.id.time);
        Menu_One.setOnClickListener(this);
        Menu_Two.setOnClickListener(this);
        Menu_Three.setOnClickListener(this);
        Menu_Four.setOnClickListener(this);
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String date2 = sdf2.format(new Date());
        Timeshower.setText("更新时间：" + date2);
        TimerThread Timer_check = new TimerThread();
        new Thread(Timer_check).start();
    }

    public void onClick(View v) {
        if(v.getId()==R.id.imageButton){
            Intent chart = new Intent(MenuActivity.this,ChartActivity.class);
            //login.putExtra("UserName",UserName);
            startActivity(chart);
        }
        else if(v.getId()==R.id.imageButton2){
            Intent history = new Intent(MenuActivity.this,HistroyActivity.class);
            startActivity(history);
        }
        else if(v.getId()==R.id.imageButton3){
            Intent history7 = new Intent(MenuActivity.this,Day7HistoryActivity.class);
            startActivity(history7);
        }
        else if(v.getId()==R.id.imageButton4){
            Intent otherInform = new Intent(MenuActivity.this,OtherInformation.class);
            startActivity(otherInform);
        }
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case TIME_PAUSE:
                    Data_Checker_new();
                    break;
                case TEXT_HELPER:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case CHECKER_RESPONSE:
                    SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
                    String date2 = sdf2.format(new Date());
                    Timeshower.setText("更新时间：" + date2);
                    Bundle checker_data = msg.getData();
                    String output = "";
                    Set<String> keySet = checker_data.keySet();
                    if(keySet.isEmpty()){
                        responseText.setText("当前数据正常");
                    }
                    else {
                        for (String key : keySet) {
                            Object value = checker_data.get(key);
                            output = output + "服务器" + key + "," + value.toString() + "异常" + "\n";
                        }
                        responseText.setText(output);
                        Log.d("test","checking");
                    }
                    break;
            }
        }
    };
    private void Data_Checker_new(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                int link_flag = 1;
                String[] Attr = {"Net","Cpu","Memory"};
                try{
                    Message message = new Message();
                    message.what = CHECKER_RESPONSE;
                    Bundle data_text = new Bundle();
                    for(int i = 1 ; i < 4 ; i++) {
                        String abnormal_attr = "";
                        String url_text = "http://192.168.20.52:8088/test2_war_exploded/json/hostNewUsage?id="+ i;
                        URL url = new URL(url_text);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        InputStream in = connection.getInputStream();
                        if (connection.getResponseCode() != 200) {
                            link_flag = 0;
                            break;
                        }
                        //下面对获取到的输入流进行读取
                        BufferedReader bufr = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line = null;
                        while ((line = bufr.readLine()) != null) {
                            response.append(line);
                        }
                        //将服务器返回的数据存放到Message中
                        String[] split_data = Tool.getNumber(response.toString());
                        for (int j = 0; j < 3; j++) {
                            float float_data = Float.parseFloat(split_data[j]);
                            if (float_data > Abnormal_Percentage) {
                                if (abnormal_attr.equals("")) {
                                    abnormal_attr = Attr[j];
                                } else {
                                    abnormal_attr = abnormal_attr + "," + Attr[j];
                                }
                            }
                        }
                        if(!abnormal_attr.equals(""))
                            data_text.putString(Integer.toString(i), abnormal_attr);
                    }
                    if(link_flag == 1) {
                        message.setData(data_text);
                        handler.sendMessage(message);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"连接错误"+Integer.toString(connection.getResponseCode()),Toast.LENGTH_SHORT);
                    }
                }catch(Exception e){
                    Message msg = new Message();
                    msg.what = TEXT_HELPER;
                    msg.obj = "无法访问服务器";
                    handler.sendMessage(msg);
                    Log.d("test","无法访问服务器");
                    e.printStackTrace();
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public class TimerThread implements Runnable {
        int time_pause;
        public TimerThread(){
            time_pause = 60000;
        }
        public void setTime_pause(int millsecond){
            time_pause = millsecond;
        }
        public void run() {
            while (true) {
                try {
                    Thread.sleep(time_pause);// 线程暂停10秒，单位毫秒
                    Message message = new Message();
                    message.what = TIME_PAUSE;
                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
