package com.zixiao.datepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

public class DatePickerView extends View {

    // TODO 两个月的同一天不同时显示特殊效果，即当天和选择的那天的特效只在当月图表里显示
    // 横向偏移量，即左右滑动的距离
    private float horizontalOffset = 0;
    // 画笔对象
    private Paint paint = new Paint();
    // 计算当月第一天使用的日历对象，避免大量新建对象设为成员变量
    private Calendar firstDayTempCalendar = Calendar.getInstance();
    // 需要绘制的那天的日历对象
    private Calendar dayCalendar = Calendar.getInstance();
    // 正在绘制的月份日历对象
    private Calendar currentCalendar = Calendar.getInstance();
    // 记录滑动距离的变量，用于计算横向偏移量
    private float scrollVariable = 0;
    // 记录开始滑动的变量，用于计算偏移月份
    private float startVariable = 0;
    // 记录开始滑动的纵向变量，用于判断是否是滑动事件
    private float startVertical = 0;
    // 开始月份的日历对象
    private Calendar startCalendar = Calendar.getInstance();
    // 判断触摸事件是否是滑动事件的变量
    private boolean isScrolling = false;
    // 被选中的日期
    private Calendar selectedDate = Calendar.getInstance();
    // 计算文字位置的对象
    Paint.FontMetrics fontMetrics;
    // 是否打开触摸事件
    private boolean isOnTouchOpen = true;
    // 计算每月行数时使用的日历对象
    private Calendar lineCountTempCalendar = Calendar.getInstance();
    // 用于将已选择日期传递给外界的接口
    private OnDatePicker onDatePicker;

    public void setOnDatePicker(OnDatePicker onDatePicker) {
        this.onDatePicker = onDatePicker;
    }

    public DatePickerView(Context context) {
        super(context);
    }

    public DatePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DatePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {


        int lineCount = 5;
        paint.setTextSize(getWidth() / 36);
        fontMetrics = paint.getFontMetrics();
        paint.setColor(Color.parseColor("#999999"));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(startCalendar.getTime());
        currentCalendar.add(Calendar.MONTH, -5);
        for (int monthPosition = -4; monthPosition <= 4; monthPosition++) {

            currentCalendar.add(Calendar.MONTH, 1);
            canvas.drawText(currentCalendar.get(Calendar.MONTH) + 1 + "月", getWidth() / 2 + monthPosition * getWidth(), 30, paint);
            dayCalendar.setTime(getFirstDay(currentCalendar).getTime());
            dayCalendar.add(Calendar.DATE, -1);
            lineCount = (int) getLineCount(currentCalendar);

            for (int verticalPosition = 0; verticalPosition <= lineCount; verticalPosition++) {

                for (int horizontalPosition = 0; horizontalPosition <= 6; horizontalPosition++) {
                    dayCalendar.add(Calendar.DATE, 1);
                    if (isSameDay(dayCalendar, Calendar.getInstance()) && currentCalendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)) {
                        paint.setColor(Color.parseColor("#333333"));
                        canvas.drawCircle(getLeftBorder(monthPosition, horizontalPosition) + getWidth() / 14, getTopBorder(verticalPosition, lineCount) + getHeight() / 2 / lineCount, getWidth() / 36, paint);
                    }
                    if (isSameDay(dayCalendar, selectedDate) && currentCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)) {
                        paint.setColor(Color.parseColor("#FF5B7C"));
                        canvas.drawCircle(getLeftBorder(monthPosition, horizontalPosition) + getWidth() / 14, getTopBorder(verticalPosition, lineCount) + getHeight() / 2 / lineCount, getWidth() / 36, paint);
                    }
                    paint.setColor(Color.parseColor("#333333"));
                    if (isSameDay(dayCalendar, Calendar.getInstance())) {
                        paint.setColor(Color.parseColor("#FFFFFF"));
                    }
                    if (isSameDay(dayCalendar, selectedDate)) {
                        paint.setColor(Color.parseColor("#FFFFFF"));
                    }
                    if (dayCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
                        paint.setColor(Color.parseColor("#999999"));
                    }

                    int textY = (int) (getTopBorder(verticalPosition, lineCount) + getHeight() / 2 / lineCount - fontMetrics.top / 2 - fontMetrics.bottom / 2);
                    canvas.drawText(dayCalendar.get(Calendar.DATE) + "", getLeftBorder(monthPosition, horizontalPosition) + getWidth() / 14, textY, paint);
                }

            }
        }

    }

    // 获取左边框坐标
    private float getLeftBorder(int monthPosition, int horizontalPosition) {
        return monthPosition * getWidth() + getWidth() / 7 * horizontalPosition + horizontalOffset;
    }

    // 获取上边框坐标
    private float getTopBorder(int verticalPosition, int lineCount) {
        return getHeight() / lineCount * verticalPosition;
    }

    // 获取某月第一天的日期
    private Calendar getFirstDay(Calendar month) {
        firstDayTempCalendar.setTime(month.getTime());
        // 获取当月第一天的日历对象
        firstDayTempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        // 如果第一天是周日则第一天为上周第一天
        if (firstDayTempCalendar.get(Calendar.DAY_OF_WEEK) < Calendar.MONDAY) {
            firstDayTempCalendar.add(Calendar.DATE, -7);
        }
        // 获取该天对应的周一的日历对象
        firstDayTempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        // 返回计算得到的日历对象
        return firstDayTempCalendar;
    }

    // 判断是否同一天
    private boolean isSameDay(Calendar firstCalendar, Calendar secondCalendar) {
        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR) && firstCalendar.get(Calendar.MONTH) == secondCalendar.get(Calendar.MONTH) && firstCalendar.get(Calendar.DATE) == secondCalendar.get(Calendar.DATE);
    }

    // TODO 跨年时无法正常得出结果，周日与周一异常
    // 获取行数的方法，如果5行不够显示则显示6行
    private long getLineCount(Calendar month) {
        lineCountTempCalendar.setTime(month.getTime());
        lineCountTempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        long firstDay = getFirstDay(lineCountTempCalendar).getTimeInMillis() / 1000 / 60 / 60 / 24;
        long firstWeek = (lineCountTempCalendar.getTimeInMillis() / 1000 / 60 / 60 / 24 - firstDay) / 7;
        lineCountTempCalendar.set(Calendar.DAY_OF_MONTH, month.getActualMaximum(Calendar.DAY_OF_MONTH));
        long lastWeek = (lineCountTempCalendar.getTimeInMillis() / 1000 / 60 / 60 / 24 - firstDay) / 7;
        return lastWeek - firstWeek + 1;
    }

    // OnTouchListener会覆盖OnClickListener，故官方建议在onTouch方法中适时调用performClick()方法，但本控件不需要
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isOnTouchOpen) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    scrollVariable = event.getX();
                    startVariable = event.getX();
                    startVertical = event.getY();
                    isScrolling = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(event.getX() - startVariable) > 50 || Math.abs(event.getY() - startVertical) > 50) {
                        isScrolling = true;
                    }
                    if (isScrolling) {
                        horizontalOffset += event.getX() - scrollVariable;
                        scrollVariable = event.getX();
                        refreshView();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isScrolling) {
                        AnimationRunnable runnable = new AnimationRunnable();
                        runnable.setEventX(event.getX());
                        new Thread(runnable).start();
                    } else {
                        selectedDate.setTime(currentCalendar.getTime());
                        selectedDate.add(Calendar.MONTH, -4);
                        selectedDate.setTime(getFirstDay(selectedDate).getTime());
                        int row = (int) (event.getX() / (getWidth() / 7));
                        int list = (int) (event.getY() / (getHeight() / getLineCount(startCalendar)));
                        int dayOffset = list * 7 + row;
                        selectedDate.add(Calendar.DAY_OF_YEAR, dayOffset);
                        if (selectedDate.get(Calendar.MONTH) == startCalendar.get(Calendar.MONTH)) {
                            if (onDatePicker != null) {
                                onDatePicker.onDatePicker(selectedDate);
                            }
                            refreshView();
                        } else {
                            selectedDate.add(Calendar.DAY_OF_YEAR, dayOffset);
                        }
                    }
                    break;
            }
        }
        return true;
    }

    private class AnimationRunnable implements Runnable {

        private float eventX;

        private void setEventX(float eventX) {
            this.eventX = eventX;
        }

        @Override
        public void run() {
            isOnTouchOpen = false;
            float targetOffset;
            if (horizontalOffset > getWidth() / 2) {
                targetOffset = getWidth();
            } else if (horizontalOffset < -getWidth() / 2) {
                targetOffset = -getWidth();
            } else {
                targetOffset = 0;
            }

            float unitOffset = (targetOffset - horizontalOffset) / 20;
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(10);
                    horizontalOffset += unitOffset;
                    refreshView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int monthVariation;
            if (eventX < startVariable) {
                monthVariation = (int) ((startVariable - eventX) / getWidth() + 0.5);
            } else {
                monthVariation = (int) ((startVariable - eventX) / getWidth() - 0.5);
            }
            startCalendar.add(Calendar.MONTH, monthVariation);
            refreshView();
            horizontalOffset = 0;
            isOnTouchOpen = true;
        }
    }

    private void refreshView() {
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public interface OnDatePicker {
        public void onDatePicker(Calendar selectedCalendar);
    }
}
