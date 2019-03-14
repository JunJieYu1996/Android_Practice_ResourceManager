package com.example.administrator.myapplication;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistroyActivity extends AppCompatActivity implements View.OnClickListener{
    private Spinner history_selector_time;
    private Spinner history_selector_server;
    private MyLineChartView chartView_history;
    private TextView history_text;
    private TopBar topBar_history;
    private Button history_button;
    public static final int SHOW_RESPONSE = 0;//用于更新操作
    public static final int TEXT_HELPER = 2;
    List<String> xValues;   //x轴数据集合
    List<Float> yValues;  //y轴数据集合
    List<Float> yValues_2;
    List<Float> yValues_3;
    int xValues_change_flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histroy);

        chartView_history = (MyLineChartView) findViewById(R.id.linechartview_history);
        history_selector_time = (Spinner) findViewById(R.id.spinner_history);
        history_selector_server = (Spinner) findViewById(R.id.spinner_history_server);
        history_text = (TextView) findViewById(R.id.history_text);
        topBar_history = (TopBar) findViewById(R.id.topbar_history);
        history_button = (Button) findViewById(R.id.history_button);

        history_button.setOnClickListener(this);

        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        yValues_2 = new ArrayList<>();
        yValues_3 = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String date = sdf.format(new Date());
        int int_date = Integer.parseInt(date);
        int add_date;
        int x_date;
        for (int i = 0; i < 12; i++) {

            add_date = int_date - 5 * (11 - i);
            if (add_date >= 0) {
                x_date = (add_date / 5) * 5;
            } else {
                x_date = ((60 + add_date) / 5) * 5;
            }
            xValues.add(Integer.toString(x_date));
            yValues.add((float) 0);
            yValues_2.add((float) 0);
            yValues_3.add((float) 1);
        }
        // xy轴集合自己添加数据
        chartView_history.setXValues(xValues);
        chartView_history.setYValues(yValues);
        chartView_history.setYValues_2(yValues_2);
        chartView_history.setYValues_3(yValues_3);


        topBar_history.setOnLeftAndRightClickListener(new TopBar.OnLeftAndRightClickListener() {
            @Override
            public void OnLeftButtonClick() {
                finish();//左边按钮实现的功能逻辑
            }

            @Override
            public void OnRightButtonClick() {//右边按钮实现的功能逻辑
                Toast.makeText(getApplicationContext(), "RightButton", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void onClick(View v) {
        if(v.getId()==R.id.history_button){
            sendRequestWithHttpURLConnection((int)history_selector_server.getSelectedItemId()+1);
        }
    }
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            //如果返现msg.what=SHOW_RESPONSE，则进行制定操作，如想进行其他操作，则在子线程里将SHOW_RESPONSE改变
            switch (msg.what) {
                case SHOW_RESPONSE:
                    Bundle text_data = msg.getData();
                    String Cpu_Data = text_data.getString("0");
                    String Net_Data = text_data.getString("1");
                    String Memory_Data = text_data.getString("2");

                    setUsage_Data(Cpu_Data, 0);
                    setUsage_Data(Net_Data, 1);
                    setUsage_Data(Memory_Data, 2);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    SimpleDateFormat minu = new SimpleDateFormat("mm");
                    int minute = Integer.parseInt(minu.format(new Date()));
                    if (((minute % 5 == 0) || (minute % 5 == 5)) && (xValues_change_flag == 0)) {
                        change_xValues();
                        xValues_change_flag = 1;
                    } else if (!((minute % 5 == 0) || (minute % 5 == 5))) {
                        xValues_change_flag = 0;
                    }
                    break;
                case TEXT_HELPER:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    private void sendRequestWithHttpURLConnection(final int id) {
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                int link_flag = 1;
                String[] url_text = {
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id=" + id,
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id=" + id,
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostMemoryUsage?id=" + id};
                try {
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    Bundle data_text = new Bundle();
                    for (int i = 0; i < 3; i++) {
                        URL url = new URL(url_text[i]);
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
                        data_text.putString(Integer.toString(i), response.toString());
                    }
                    if (link_flag == 1) {
                        message.setData(data_text);
                        handler.sendMessage(message);
                    } else {
                        Toast.makeText(getApplicationContext(), "连接错误" + Integer.toString(connection.getResponseCode()), Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = TEXT_HELPER;
                    msg.obj = "无法访问服务器";
                    handler.sendMessage(msg);
                    Log.d("test", "无法访问服务器");
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }


    public void setUsage_Data(String Usage, int choice) {
        String[] Usage_Data = Tool.getNumber(Usage);
        String[] Useable_Data = Tool.invert_strs(Usage_Data);
        int time_index = (int) history_selector_time.getSelectedItemId()+1;
        //Log.d("test",Integer.toString(Usage_Data.length));
        switch (choice) {
            case 0:
                yValues.clear();
                for (int i = (Useable_Data.length - 12*time_index); i < Useable_Data.length-12*(time_index-1); i++) {
                    yValues.add(Float.parseFloat(Useable_Data[i]));
                }
                break;
            case 1:
                yValues_2.clear();
                for (int i = (Useable_Data.length - 12*time_index); i < Useable_Data.length-12*(time_index-1); i++) {
                    yValues_2.add(Float.parseFloat(Useable_Data[i]));
                }
                break;
            case 2:
                yValues_3.clear();
                for (int i = (Useable_Data.length - 12*time_index); i < Useable_Data.length-12*(time_index-1); i++) {
                    yValues_3.add(Float.parseFloat(Useable_Data[i]));
                }
                break;
        }
    }
    public void change_xValues(){
        String temp = xValues.get(0);
        for(int i=0;i<xValues.size()-1;i++){
            xValues.set(i,xValues.get(i+1));
        }
        xValues.set(xValues.size()-1,temp);
    }
}

