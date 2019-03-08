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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartActivity extends AppCompatActivity implements View.OnClickListener {
    List<String> xValues;   //x轴数据集合
    List<Float> yValues;  //y轴数据集合
    List<Float> yValues_2;
    List<Float> yValues_3;
    String test_str;
    static int Server_id = 1;
    static int Abnormal_Percentage = 50;

    MyLineChartView chartView;
    private Button sendRequest;
    private Button Stringsplit;
    private Spinner Server_choice;
    private TextView responseText;

    public static final int SHOW_RESPONSE = 0;//用于更新操作
    public static final int TIME_PAUSE = 1;
    public static final int TEXT_HELPER = 2;
    public static final int TIME_CHECKER = 3;
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

        test_str="['0','0','0','0','0','0'，'0','0','0']";

        for (int i=0;i<10;i++){
            xValues.add(Integer.toString(i));
            yValues.add((float)0);
            yValues_2.add((float)0);
            yValues_3.add((float)0);
        }
        // xy轴集合自己添加数据
        chartView.setXValues(xValues);
        chartView.setYValues(yValues);
        chartView.setYValues_2(yValues_2);
        chartView.setyValues_3(yValues_3);

        sendRequest = (Button) findViewById(R.id.enter);
        responseText = (TextView)findViewById(R.id.input);
        Stringsplit = (Button)findViewById(R.id.test);
        Server_choice =(Spinner)findViewById(R.id.spinner);

        sendRequest.setOnClickListener(this);
        Stringsplit.setOnClickListener(this);

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
        new Thread(new TimerThread()).start();
        Data_Checker();
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
                    //String response=(String)msg.obj;
                    setUsage_Data(Cpu_Data, 0);
                    setUsage_Data(Net_Data, 1);
                    setUsage_Data(Memory_Data, 2);
                    //进行UI操作，将结果显示到界面上
                    //responseText.setText("数据正常");
                    break;
                case TIME_PAUSE:
                    sendRequestWithHttpURLConnection(Server_id);
                    break;
                case TEXT_HELPER:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case TIME_CHECKER:
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
                    }
                    break;
            }
        }
    };


    public void onClick(View v) {
        if(v.getId()==R.id.enter){
            int a=1;
        }
        else if (v.getId()==R.id.test){
            String[] show_test;
            show_test = getNumber(test_str);
            yValues.clear();
            for(int i =0;i < show_test.length;i++) {
                yValues.add(Float.parseFloat(show_test[i]));
                responseText.setText("change!");
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

        private void Data_Checker(){
            //开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection=null;
                    int link_flag = 1;
                    String[] Attr = {"Cpu","Net","Memory"};
                    try{
                        Message message = new Message();
                        message.what = CHECKER_RESPONSE;
                        Bundle data_text = new Bundle();
                        for(int i = 1 ; i < 4 ; i++) {
                            String abnormal_attr = "";
                            for(int j = 0 ; j<Attr.length ; j++) {
                                String url_text = "http://192.168.20.52:8088/test2_war_exploded/json/host"+Attr[j]+"Usage?id="+ i;
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
                                String[] split_data = getNumber(response.toString());
                                for(int k =0 ;k<10;k++){
                                    float float_data = Float.parseFloat(split_data[k]);
                                    //Log.d("url_text",Float.toString(float_data));
                                    if(float_data>Abnormal_Percentage){
                                        if (abnormal_attr.equals("")){
                                            abnormal_attr = Attr[j];
                                        }
                                        else{
                                            abnormal_attr = abnormal_attr + "," + Attr[j];
                                        }
                                        break;
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

    public static String[] getNumber(String str) {
        // 需要取整数和小数的字符串
        // 控制正则表达式的匹配行为的参数(小数)
        String strs[] = str.split(",");
        for(int i = 0;i < strs.length;i++) {
            Pattern p = Pattern.compile("(\\d+\\.\\d+)");
            //Matcher类的构造方法也是私有的,不能随意创建,只能通过Pattern.matcher(CharSequence input)方法得到该类的实例.
            Matcher m = p.matcher(strs[i]);
            //m.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
            if (m.find()) {
                //如果有相匹配的,则判断是否为null操作
                //group()中的参数：0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则,在这只有一个括号,即1和0是一样的
                strs[i] = m.group(1) == null ? "" : m.group(1);
            } else {
                //如果匹配不到小数，就进行整数匹配
                p = Pattern.compile("(\\d+)");
                m = p.matcher(strs[i]);
                if (m.find()) {
                    //如果有整数相匹配
                    strs[i] = m.group(1) == null ? "" : m.group(1);
                } else {
                    //如果没有小数和整数相匹配,即字符串中没有整数和小数，就设为空
                    strs[i] = "";
                }
            }
        }
        return strs;
    }

    public void setUsage_Data(String Usage,int choice){
        String[] Usage_Data = getNumber(Usage);
        //Log.d("test",Integer.toString(Usage_Data.length));
        switch (choice){
            case 0:
                yValues.clear();
                for(int i=0;i<10;i++){
                    yValues.add(Float.parseFloat(Usage_Data[i]));
                }
                break;
            case 1:
                yValues_2.clear();
                for(int i=0;i<10;i++){
                    yValues_2.add(Float.parseFloat(Usage_Data[i]));
                }
                break;
            case 2:
                yValues_3.clear();;
                for(int i=0;i<10;i++){
                    yValues_3.add(Float.parseFloat(Usage_Data[i]));
                }
                break;
        }

    }

    public class TimerThread implements Runnable {
        int time_pause;
        public TimerThread(){
            time_pause = 10000;
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

    public class TimerThread_checker implements Runnable {
        int time_pause;
        public TimerThread_checker(){
            time_pause = 10000;
        }
        public void setTime_pause(int millsecond){
            time_pause = millsecond;
        }
        public void run() {
            while (true) {
                try {
                    Thread.sleep(time_pause);// 线程暂停10秒，单位毫秒
                    Message message = new Message();
                    message.what = TIME_CHECKER;
                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
