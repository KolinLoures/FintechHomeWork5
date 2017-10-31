package com.example.kolin.fintechhomework5;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LineChartView chartView = findViewById(R.id.my_chart);

        List<Point> list = new LinkedList<>();
        list.add(new Point(1,1));
        list.add(new Point(1.5f,4));
        list.add(new Point(2,2));
        list.add(new Point(3,5));
        list.add(new Point(4,1));
        list.add(new Point(5,0));
        chartView.setPoints(list);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chartView.isShowGrid())
                    chartView.setShowGrid(false);
                else
                    chartView.setShowGrid(true);
            }
        });
    }
}
