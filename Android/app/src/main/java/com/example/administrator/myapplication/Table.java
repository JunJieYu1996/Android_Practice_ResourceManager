package com.example.administrator.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

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
    public void setNets(float[] net){
        DecimalFormat fnum = new DecimalFormat( "##0.00 ");
        String ff=fnum.format(net[0]);
        Net1.setText(ff);
        ff = fnum.format(net[1]);
        Net2.setText(ff);
        ff = fnum.format(net[2]);
        Net3.setText(ff);
    }
    public void setCpus(float[] cpu) {
        DecimalFormat fnum = new DecimalFormat( "##0.00 ");
        String ff=fnum.format(cpu[0]);
        CPU1.setText(ff);
        ff = fnum.format(cpu[1]);
        CPU2.setText(ff);
        ff = fnum.format(cpu[2]);
        CPU3.setText(ff);
    }
    public void setMemorys(float[] memory) {
        DecimalFormat fnum = new DecimalFormat( "##0.00 ");
        String ff=fnum.format(memory[0]);
        Memory1.setText(ff);
        ff = fnum.format(memory[1]);
        Memory2.setText(ff);
        ff = fnum.format(memory[2]);
        Memory3.setText(ff);
    }
    public void setTemps(String[]temp) {
        Temp1.setText(temp[0]);
        Temp2.setText(temp[1]);
        Temp3.setText(temp[2]);
    }
}
