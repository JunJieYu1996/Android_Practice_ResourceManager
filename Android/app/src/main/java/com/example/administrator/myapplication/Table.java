package com.example.administrator.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class Table extends TableLayout {
    private TextView Net1,Net2,Net3,CPU1,CPU2,CPU3,Memory1,Memory2,Memory3,Temp1,Temp2,Temp3;

    public Table(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_table, this);
        Net1 = (TextView) findViewById(R.id.Net1);
        Net2 = (TextView) findViewById(R.id.Net2);
        Net3 = (TextView) findViewById(R.id.Net3);
        CPU1 = (TextView) findViewById(R.id.CPU1);
        CPU2 = (TextView) findViewById(R.id.CPU2);
        CPU3 = (TextView) findViewById(R.id.CPU3);
        Memory1 = (TextView) findViewById(R.id.Memory1);
        Memory2 = (TextView) findViewById(R.id.Memory2);
        Memory3 = (TextView) findViewById(R.id.Memory3);
        Temp1 = (TextView) findViewById(R.id.Temp1);
        Temp2 = (TextView) findViewById(R.id.Temp2);
        Temp3 = (TextView) findViewById(R.id.Temp3);
    }
    public void setNets(String net1,String net2,String net3){
        Net1.setText(net1);
        Net2.setText(net2);
        Net3.setText(net3);
    }
    public void setCPUs(String net1,String net2,String net3){
        Net1.setText(net1);
        Net2.setText(net2);
        Net3.setText(net3);
    }
    public void setMemorys(String net1,String net2,String net3){
        Net1.setText(net1);
        Net2.setText(net2);
        Net3.setText(net3);
    }
    public void setTemps(String net1,String net2,String net3){
        Net1.setText(net1);
        Net2.setText(net2);
        Net3.setText(net3);
    }
}
