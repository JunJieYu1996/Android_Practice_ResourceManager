package com.example.administrator.myapplication;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
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
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartActivity extends AppCompatActivity {
    List<String> xValues;   //x轴数据集合
    List<Float> yValues;  //y轴数据集合
    List<Float> yValues_2;
    List<Float> yValues_3;
    int xValues_change_flag = 0;
    static int Server_id = 1;
    boolean stopThread = false;

    MyLineChartView chartView;
    private Spinner Server_choice;
    private TextView responseText;
    private TextView Timeshower;
    private TopBar topBar;

    public static final int SHOW_RESPONSE = 0;//用于更新操作
    public static final int TIME_PAUSE = 1;
    public static final int TEXT_HELPER = 2;
    public static final int CHECKER_RESPONSE = 4;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        chartView = (MyLineChartView) findViewById(R.id.linechartview);
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        yValues_2 = new ArrayList<>();
        yValues_3 = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String date = sdf.format(new Date());
        int int_date = Integer.parseInt(date);
        int add_date ;
        int x_date;
        for (int i=0;i<12;i++){
            add_date = int_date - 5 * (11 - i) ;
            if(add_date >= 0){
                x_date = (add_date/5)*5;
            }
            else {
                x_date = ((60 + add_date) / 5) * 5;
            }
            xValues.add(Integer.toString(x_date));
            yValues.add((float)0);
            yValues_2.add((float)0);
            yValues_3.add((float)0);
        }
        // xy轴集合自己添加数据
        chartView.setXValues(xValues);
        chartView.setYValues(yValues);
        chartView.setYValues_2(yValues_2);
        chartView.setYValues_3(yValues_3);

        TimerThread Timer_check = new TimerThread();

        responseText = (TextView)findViewById(R.id.input);
        Timeshower = (TextView)findViewById(R.id.time);
        Server_choice =(Spinner)findViewById(R.id.spinner);
        topBar = (TopBar) findViewById(R.id.topbar_chart);
        topBar.setOnLeftAndRightClickListener(new TopBar.OnLeftAndRightClickListener() {
            @Override
            public void OnLeftButtonClick() {
                stopThread = true;
                finish();//左边按钮实现的功能逻辑
            }

            @Override
            public void OnRightButtonClick() {//右边按钮实现的功能逻辑
                Toast.makeText(getApplicationContext(), "RightButton", Toast.LENGTH_SHORT).show();
            }
        });


        Server_choice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             //当选中某一个数据项时触发该方法
             //parent接收的是被选择的数据项所属的 Spinner对象，
             //view参数接收的是显示被选择的数据项的TextView对象
             //position接收的是被选择的数据项在适配器中的位置
             //id被选择的数据项的行号
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                Server_id = (int)Server_choice.getSelectedItemId()+1;//从spinner中获取被选择的数据
                sendRequestWithHttpURLConnection(Server_id);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String date2 = sdf2.format(new Date());
        Timeshower.setText("更新时间：" + date2);

        new Thread(Timer_check).start();
    }

    protected void onDestroy() {
        stopThread=true;
        super.onDestroy();
    }


    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
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
                    String date = sdf.format(new Date());
                    Timeshower.setText("更新时间：" + date);
                    int minute = Integer.parseInt(minu.format(new Date()));
                    if(((minute%5==0)||(minute%5==5))&&(xValues_change_flag == 0)){
                        change_xValues();
                        xValues_change_flag = 1;
                    }
                    else if (!((minute%5==0)||(minute%5==5))) {
                        xValues_change_flag = 0;
                    }
                    break;
                case TIME_PAUSE:
                    sendRequestWithHttpURLConnection(Server_id);
                    //Data_Checker_new();
                    break;
                case TEXT_HELPER:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case CHECKER_RESPONSE:
                    Bundle checker_data = msg.getData();
                    String output = "";
                    Set<String> keySet = checker_data.keySet();
                    if(keySet.isEmpty()){
                        responseText.setText("数据正常");
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

    public class TimerThread implements Runnable {
        int time_pause;
        public TimerThread(){
            time_pause = 30000;
        }
        public void setTime_pause(int millsecond){
            time_pause = millsecond;
        }
        public void run() {
            while (!stopThread) {
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




    private void sendRequestWithHttpURLConnection(final int id){
            //开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection=null;
                    int link_flag = 1;
                    String[] url_text = {
                            "http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id="+id,
                            "http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id="+id,
                            "http://192.168.20.52:8088/test2_war_exploded/json/hostMemoryUsage?id="+id};
                    try{
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        Bundle data_text = new Bundle();
                        for(int i =0 ; i<3 ; i++) {
                            URL url = new URL(url_text[i]);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            InputStream in = connection.getInputStream();
                            if(connection.getResponseCode() != 200) {
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
                            data_text.putString(Integer.toString(i),response.toString());
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
        //http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id=1
        //http://192.168.20.52:8088/test2_war_exploded/json/hostMemoryUsage?id=1
        //http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id=1

    public void setUsage_Data(String Usage,int choice){
        String[] Usage_Data = Tool.getNumber(Usage);
        String[] Useable_Data = Tool.invert_strs(Usage_Data);
        switch (choice){
            case 0:
                yValues.clear();
                for(int i=(Useable_Data.length-12);i<Useable_Data.length;i++){
                    yValues.add(Float.parseFloat(Useable_Data[i]));
                }
                break;
            case 1:
                yValues_2.clear();
                for(int i=(Useable_Data.length-12);i<Useable_Data.length;i++){
                    yValues_2.add(Float.parseFloat(Useable_Data[i]));
                }
                break;
            case 2:
                yValues_3.clear();
                for(int i=(Useable_Data.length-12);i<Useable_Data.length;i++){
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
