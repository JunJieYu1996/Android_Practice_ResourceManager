package com.example.administrator.myapplication;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartActivity extends AppCompatActivity implements View.OnClickListener {
        MyLineChartView chartView;
        List<String> xValues;   //x轴数据集合
        List<Float> yValues;  //y轴数据集合
        List<Float> yValues_2;
        private Button sendRequest;
        private Button Stringsplit;
        public static final int SHOW_RESPONSE=0;//用于更新操作
        public static final int TIME_PAUSE=1;
        private TextView responseText;
        String test_str;

        private Handler handler=new Handler(){
            public void handleMessage(Message msg){
                //如果返现msg.what=SHOW_RESPONSE，则进行制定操作，如想进行其他操作，则在子线程里将SHOW_RESPONSE改变
                switch (msg.what){
                    case SHOW_RESPONSE:
                        Bundle text_data = msg.getData();
                        String Cpu_Data = text_data.getString("0");
                        String Net_Data = text_data.getString("1");
                        //String response=(String)msg.obj;
                        setUsage_Data(Cpu_Data,0);
                        setUsage_Data(Net_Data,1);
                        //进行UI操作，将结果显示到界面上
                        responseText.setText("load");
                        break;
                    case TIME_PAUSE:
                        Toast.makeText(getApplicationContext(),"hello!-10s",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chart);
            chartView = (MyLineChartView) findViewById(R.id.linechartview);
            xValues = new ArrayList<>();
            yValues = new ArrayList<>();
            yValues_2 = new ArrayList<>();

            test_str="['1.1','2.2','3.3','4.4','5.5','6.6'，'7.7','8.8','9.9']";

            for (int i=0;i<10;i++){
                xValues.add(Integer.toString(i));
                yValues.add((float)i);
                yValues_2.add((float)i+1);
            }
            // xy轴集合自己添加数据
            chartView.setXValues(xValues);
            chartView.setYValues(yValues);
            chartView.setYValues_2(yValues_2);

            sendRequest = (Button) findViewById(R.id.enter);
            responseText=(TextView)findViewById(R.id.input);
            Stringsplit=(Button)findViewById(R.id.test);

            sendRequest.setOnClickListener(this);
            Stringsplit.setOnClickListener(this);
            new Thread(new TimerThread()).start();
        }

        public void onClick(View v) {
            if(v.getId()==R.id.enter){
                //responseText.setText("1234");
                String attr = "Cpu";
                int id = 1;
                sendRequestWithHttpURLConnection();
            }
            else if (v.getId()==R.id.test){
                String[] show_test;
                String history_str;
                show_test = getNumber(test_str);
                yValues.clear();
                for(int i =0;i < show_test.length;i++) {
                    yValues.add(Float.parseFloat(show_test[i]));
                    responseText.setText("change!");
                }
            }
        }

        private void sendRequestWithHttpURLConnection(){
            //开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection=null;
                    String[] url_text = {"http://192.168.20.52:8088/test2_war_exploded/json/hostCpuUsage?id=1","http://192.168.20.52:8088/test2_war_exploded/json/hostNetUsage?id=1"};
                    try{
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        Bundle data_text = new Bundle();
                        for(int i =0 ; i<2 ; i++) {

                            URL url = new URL(url_text[i]);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(8000);
                            connection.setReadTimeout(8000);

                            InputStream in = connection.getInputStream();
                            //下面对获取到的输入流进行读取
                            BufferedReader bufr = new BufferedReader(new InputStreamReader(in));
                            StringBuilder response = new StringBuilder();
                            String line = null;
                            while ((line = bufr.readLine()) != null) {
                                response.append(line);
                            }
                            //将服务器返回的数据存放到Message中
                            data_text.putString(Integer.toString(i),response.toString());
                            //message.obj = response.toString();
                        }
                        message.setData(data_text);
                        //message.sendToTarget();
                        handler.sendMessage(message);
                    }catch(Exception e){
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
                //Log.d("test", strs[i]);
            }
            return strs;
        }

        public void setUsage_Data(String Usage,int choice){
            String[] Usage_Data = getNumber(Usage);
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
}
