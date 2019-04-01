package com.example.administrator.myapplication;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class OtherInformation extends AppCompatActivity {
    private TextView Timeshower;
    private Table DataTable;
    private TopBar topBar_otherinform;
    public static final int SHOW_RESPONSE = 0;//用于更新操作
    public static final int TEXT_HELPER = 2;
    String[] Cpu_Data = new String[3];
    String[] Net_Data = new String[3];
    String[] Memory_Data = new String[3];
    String Temp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_information);
        Timeshower = (TextView)findViewById(R.id.time_other);
        topBar_otherinform = (TopBar)findViewById(R.id.topbar_otherinform);
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String date2 = sdf2.format(new Date());
        Timeshower.setText("更新时间：" + date2);
        DataTable = (Table)findViewById(R.id.table);
        topBar_otherinform.setOnLeftAndRightClickListener(new TopBar.OnLeftAndRightClickListener() {
            @Override
            public void OnLeftButtonClick() {
                finish();//左边按钮实现的功能逻辑
            }

            @Override
            public void OnRightButtonClick() {//右边按钮实现的功能逻辑
                Toast.makeText(getApplicationContext(), "RightButton", Toast.LENGTH_SHORT).show();
            }
        });
        sendRequestWithHttpURLConnection();
    }
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            //如果返现msg.what=SHOW_RESPONSE，则进行制定操作，如想进行其他操作，则在子线程里将SHOW_RESPONSE改变
            switch (msg.what) {
                case SHOW_RESPONSE:
                    Bundle text_data = msg.getData();
                    Cpu_Data[0] = text_data.getString("0");
                    Cpu_Data[1] = text_data.getString("3");
                    Cpu_Data[2] = text_data.getString("6");
                    Net_Data[0] = text_data.getString("1");
                    Net_Data[1] = text_data.getString("4");
                    Net_Data[2]= text_data.getString("7");
                    Memory_Data[0] = text_data.getString("2");
                    Memory_Data[1] = text_data.getString("5");
                    Memory_Data[2] = text_data.getString("8");
                    Temp = text_data.getString("9");
                    fillTable();
                    break;
                case TEXT_HELPER:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void sendRequestWithHttpURLConnection(){
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                int link_flag = 1;
                String[] url_text = {
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id=1",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id=1",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostMemoryUsage?id=1",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id=2",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id=2",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostMemoryUsage?id=2",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id=3",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id=3",
                        "http://192.168.20.52:8088/test2_war_exploded/json/hostMemoryUsage?id=3",
                        "http://192.168.20.52:8088/test2_war_exploded/json/gettemptochen"};
                try{
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    Bundle data_text = new Bundle();
                    for(int i =0 ; i<10 ; i++) {
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

    private void fillTable(){
        float[] CpuAvr = new float[3];
        float[] NetAvr = new float[3];
        float[] MemoryAvr = new float[3];
        String[] Tempre;
        CpuAvr[0]=setUsage_Data(Cpu_Data[0]);
        CpuAvr[1]=setUsage_Data(Cpu_Data[1]);
        CpuAvr[2]=setUsage_Data(Cpu_Data[2]);
        NetAvr[0]=setUsage_Data(Net_Data[0]);
        NetAvr[1]=setUsage_Data(Net_Data[1]);
        NetAvr[2]=setUsage_Data(Net_Data[2]);
        MemoryAvr[0]=setUsage_Data(Memory_Data[0]);
        MemoryAvr[1]=setUsage_Data(Memory_Data[1]);
        MemoryAvr[2]=setUsage_Data(Memory_Data[2]);
        Tempre = setTemp_Data(Temp);
        DataTable.setNets(NetAvr);
        DataTable.setCpus(CpuAvr);
        DataTable.setMemorys(MemoryAvr);
        DataTable.setTemps(Tempre);
    }

    public float setUsage_Data(String Usage){
        String[] Usage_Data = Tool.getNumber(Usage);
        float sum = 0;
        float num;
        float avr;
        for (int i = 0 ;i < 288; i++){
            num = Float.parseFloat(Usage_Data[i]);
            sum += num;
        }
        avr = sum/(float)288;
        return avr;
    }

    public String[] setTemp_Data(String Temp){
        String[] Temp_Data = Tool.getNumber(Temp);
        return Temp_Data;
    }
}
