package com.zixiao.datepicker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView selectedDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedDateView = findViewById(R.id.tv_date_selected);
        DatePickerView datePickerView = findViewById(R.id.dpv);

        showSelectedDate(Calendar.getInstance());
        datePickerView.setOnDatePicker(new DatePickerView.OnDatePicker() {
            @Override
            public void onDatePicker(Calendar selectedCalendar) {
                showSelectedDate(selectedCalendar);
            }
        });
    }

    private void showSelectedDate(Calendar selectedDate) {
        SimpleDateFormat selectedDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        selectedDateView.setText(selectedDateFormat.format(selectedDate.getTime()));
    }
}
