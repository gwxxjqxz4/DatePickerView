package com.zixiao.datepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
    // 初始月份的日历对象
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

        // 绘制的行数，根据当月需要显示多少周计算
        int lineCount;
        // 设置画笔抗锯齿
        paint.setAntiAlias(true);
        // 字体大小，根据控件的宽度决定
        paint.setTextSize(getWidth() / 36);
        // 设置框线粗细
        paint.setStrokeWidth(2);
        // Android绘制文字的基线不在文字顶部或底部，具体可上网搜索，该参数可以协助调整文字的位置，使其垂直居中
        fontMetrics = paint.getFontMetrics();
        // 设置文字水平居中
        paint.setTextAlign(Paint.Align.CENTER);
        // 设置第一个要绘制的月为初始月份的四个月前
        currentCalendar.setTime(startCalendar.getTime());
        currentCalendar.add(Calendar.MONTH, -4);
        // 绘制前后9个月的日期，每次循环绘制一个月的日期（该设计考虑到当控件大小远小于屏幕大小时有可能需要一次性改变几个月）
        for (int monthPosition = -4; monthPosition <= 4; monthPosition++) {

            // 设置框线颜色为黑色
            paint.setColor(Color.parseColor("#333333"));
            // 绘制框线区分每个月
            canvas.drawLine(monthPosition * getWidth() + horizontalOffset, 0, monthPosition * getWidth() + horizontalOffset, getHeight(), paint);
            canvas.drawLine((monthPosition + 1) * getWidth() - 1 + horizontalOffset, 0, (monthPosition + 1) * getWidth() - 1 + horizontalOffset, getHeight(), paint);
            // 计算该月需要显示的行数
            lineCount = (int) getLineCount(currentCalendar);
            // 计算该月在控件上的第一行第一天
            dayCalendar.setTime(getFirstDay(currentCalendar).getTime());

            // 每次循环绘制一周的日期
            for (int verticalPosition = 0; verticalPosition <= lineCount; verticalPosition++) {

                // 每次循环绘制一天的日期
                for (int horizontalPosition = 0; horizontalPosition <= 6; horizontalPosition++) {

                    // 如果该日是当天则显示黑色背景，若在非该月的部分不显示该背景
                    if (isSameDay(dayCalendar, Calendar.getInstance()) && currentCalendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)) {
                        paint.setColor(Color.parseColor("#333333"));
                        canvas.drawCircle(getLeftBorder(monthPosition, horizontalPosition) + getWidth() / 14, getTopBorder(verticalPosition, lineCount) + getHeight() / 2 / lineCount, getWidth() / 36, paint);
                    }

                    // 如果该日是被选中的日期则显示红色背景，该日既是被选中的日期又是当天则红色背景覆盖黑色背景，若在非该月的部分不显示该背景
                    if (isSameDay(dayCalendar, selectedDate) && currentCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)) {
                        paint.setColor(Color.parseColor("#FF5B7C"));
                        canvas.drawCircle(getLeftBorder(monthPosition, horizontalPosition) + getWidth() / 14, getTopBorder(verticalPosition, lineCount) + getHeight() / 2 / lineCount, getWidth() / 36, paint);
                    }

                    // 默认文字颜色为黑色，即所选择的月非当天非被选择的那天的日期文字颜色为黑色
                    paint.setColor(Color.parseColor("#333333"));
                    // 若该日为当天则显示白色文字
                    if (isSameDay(dayCalendar, Calendar.getInstance())) {
                        paint.setColor(Color.parseColor("#FFFFFF"));
                    }
                    // 若该日为被选中的日期则显示白色文字
                    if (isSameDay(dayCalendar, selectedDate)) {
                        paint.setColor(Color.parseColor("#FFFFFF"));
                    }
                    // 若该日非初始月份的日期则显示灰色文字，非该月部分的当天和被选中的那天也显示灰色文字
                    if (dayCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
                        paint.setColor(Color.parseColor("#999999"));
                    }

                    // 计算文字的y轴
                    int textY = (int) (getTopBorder(verticalPosition, lineCount) + getHeight() / 2 / lineCount - fontMetrics.top / 2 - fontMetrics.bottom / 2);
                    // 计算文字的x轴
                    int textX = (int) (getLeftBorder(monthPosition, horizontalPosition) + getWidth() / 14);
                    // 将该日日期绘制在对应位置
                    drawDateText(textX, textY, dayCalendar.get(Calendar.DATE) + "", paint, canvas);
                    // 每次循环日期加一
                    dayCalendar.add(Calendar.DATE, 1);
                }

            }

            // 每次循环月份数加一
            currentCalendar.add(Calendar.MONTH, 1);
        }

    }

    // 绘制日期文字的方法
    private void drawDateText(int textX, int textY, String text, Paint paint, Canvas canvas) {
        // 只有该文字在屏幕中才调用绘制流程，否则不绘制以节省资源
        if (textX > -getWidth() / 20 && textX < getWidth() + getWidth() / 20) {
            canvas.drawText(text, textX, textY, paint);
        }
    }

    // 获取每个日期左边框坐标
    private float getLeftBorder(int monthPosition, int horizontalPosition) {
        return monthPosition * getWidth() + getWidth() / 7 * horizontalPosition + horizontalOffset;
    }

    // 获取每个日期上边框坐标
    private float getTopBorder(int verticalPosition, int lineCount) {
        return getHeight() / lineCount * verticalPosition;
    }

    // 获取某月第一天的日期
    private Calendar getFirstDay(Calendar month) {
        firstDayTempCalendar.setTime(month.getTime());
        // 获取当月第一天的日历对象
        firstDayTempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        // 如果第一天是周日则第一天为上周第一天，因为西方默认周日为每周第一天
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

    // 获取行数的方法，如果5行不够显示则显示6行
    private long getLineCount(Calendar month) {
        lineCountTempCalendar.setTime(month.getTime());
        // 设置日期为该月第一天
        lineCountTempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        // 获取该月第一行的第一天是1970年开始的第几天作为基准天数
        long firstDay = getFirstDay(lineCountTempCalendar).getTimeInMillis() / 1000 / 60 / 60 / 24;
        // 计算该月第一天在基准天数的第几周，不出意外应为0，故该行可考虑省去
        long firstWeek = (lineCountTempCalendar.getTimeInMillis() / 1000 / 60 / 60 / 24 - firstDay) / 7;
        // 设置日期为该月最后一天
        lineCountTempCalendar.set(Calendar.DAY_OF_MONTH, month.getActualMaximum(Calendar.DAY_OF_MONTH));
        // 计算该月最后一天在基准天数的第几周，可能为4、5、6三个取值
        long lastWeek = (lineCountTempCalendar.getTimeInMillis() / 1000 / 60 / 60 / 24 - firstDay) / 7;
        // 计算需要绘制的行数
        return lastWeek - firstWeek + 1;
        // Calendar中的WEEK_OF_YEAR在跨年情况下会有异常，故使用毫秒值计算
    }

    // OnTouchListener会覆盖OnClickListener，故官方建议在onTouch方法中适时调用performClick()方法，但本控件不需要
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 在平移动画显示的期间需要禁用触摸事件，避免出现异常
        if (isOnTouchOpen) {
            switch (event.getAction()) {
                // 按下时的处理
                case MotionEvent.ACTION_DOWN:
                    // 每次按下时初始化参数
                    scrollVariable = event.getX();
                    startVariable = event.getX();
                    startVertical = event.getY();
                    // 每次按下时默认为点击事件
                    isScrolling = false;
                    break;
                // 手指移动时的处理
                case MotionEvent.ACTION_MOVE:
                    // 手指移动时若发现移动范围超过一定数值视作滑动事件
                    if (Math.abs(event.getX() - startVariable) > 50 || Math.abs(event.getY() - startVertical) > 50) {
                        // 设置事件类型为滑动事件
                        isScrolling = true;
                    }
                    // 当事件确认为滑动事件时手指移动的处理
                    if (isScrolling) {
                        // 根据手指移动的水平距离（即该次事件触发位置的横坐标减上次事件触发位置的横坐标）计算控件的水平偏移量
                        horizontalOffset += event.getX() - scrollVariable;
                        // 记录该次事件触发位置的横坐标
                        scrollVariable = event.getX();
                        // 刷新控件
                        refreshView();
                    }
                    break;
                // 手指抬起时的处理
                case MotionEvent.ACTION_UP:
                    // 当事件确认为滑动事件时的手指抬起处理
                    if (isScrolling) {
                        // 开启一个子线程并将事件触发位置传递给该子线程，由该子线程处理事件
                        new Thread(new AnimationRunnable().initEventX(event.getX())).start();
                    }
                    // 当事件为触摸事件时的处理
                    else {
                        // 设置被选中的日期的月份为初始月份（具体日期在之后设置）
                        selectedDate.setTime(startCalendar.getTime());
                        // 获取该月第一行第一天的日期
                        selectedDate.setTime(getFirstDay(selectedDate).getTime());
                        // 计算点击位置的列数
                        int row = (int) (event.getX() / (getWidth() / 7));
                        // 计算点击位置的行数
                        int list = (int) (event.getY() / (getHeight() / getLineCount(startCalendar)));
                        // 计算点击位置的日期和初始月份第一行第一天差多少天
                        int dayOffset = list * 7 + row;
                        // 设置被选中的日期
                        selectedDate.add(Calendar.DAY_OF_YEAR, dayOffset);
                        // 若被选中的日期是上月或下月时重置被选中的日期，即不处理该屏中上月和下月的那几天
                        if (selectedDate.get(Calendar.MONTH) != startCalendar.get(Calendar.MONTH)) {
                            selectedDate.add(Calendar.DAY_OF_YEAR, dayOffset);
                        } else {
                            // 如果有接口则向外界传递选中的日期
                            if (onDatePicker != null) {
                                onDatePicker.onDatePicker(selectedDate);
                            }
                            // 刷新控件
                            refreshView();
                        }
                    }
                    break;
            }
        }
        return true;
    }

    // 用于处理滑动后抬起手指事件的子线程，滑动后会播放一段平移动画对齐切换的月份
    private class AnimationRunnable implements Runnable {

        // 抬起手指时的横坐标
        private float eventX;

        // 获取横坐标的方法，返回自身对象以进行链式编程
        private AnimationRunnable initEventX(float eventX) {
            this.eventX = eventX;
            return this;
        }

        @Override
        public void run() {
            // 开始播放平移动画前禁用触摸事件，避免出现异常
            isOnTouchOpen = false;
            // 初始化月份改变量
            int monthVariation;
            // 若抬起时的横坐标小于按下时的横坐标（即向左滑动）则平移月份加0.5来实现四舍五入效果
            if (eventX < startVariable) {
                monthVariation = (int) ((startVariable - eventX) / getWidth() + 0.5);
            }
            // 若抬起时的横坐标大于按下时的横坐标（即向左滑动）则平移月份减0.5来实现四舍五入效果
            else {
                monthVariation = (int) ((startVariable - eventX) / getWidth() - 0.5);
            }
            // 初始化目标位置的偏移量为月份数乘控件宽度，即每个控件宽度显示一个月的日期
            float targetOffset = -getWidth() * monthVariation;
            // 计算每次平移的改变量
            float unitOffset = (targetOffset - horizontalOffset) / 20;
            // 循环改变水平偏移量实现平移动画
            for (int i = 0; i < 20; i++) {
                try {
                    // 每次平移间隔10毫秒
                    Thread.sleep(10);
                    // 改变水平偏移量
                    horizontalOffset += unitOffset;
                    // 刷新控件显示平移效果
                    refreshView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 改变开始月份
            startCalendar.add(Calendar.MONTH, monthVariation);
            // 水平偏移量初始化为0
            horizontalOffset = 0;
            // 刷新控件
            refreshView();
            // 平移动画结束后放开触摸事件
            isOnTouchOpen = true;
        }
    }

    // 刷新控件的方法，在主线程中进行避免异常
    private void refreshView() {
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    // 将选中的日期传递给外界的接口
    public interface OnDatePicker {
        void onDatePicker(Calendar selectedCalendar);
    }
}
