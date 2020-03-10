package com.example.yaksok.Utils;

import android.app.Activity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private int color;
    private HashSet<CalendarDay> dates;
    private float radius = 10;

    public EventDecorator(float size, int color, Collection<CalendarDay> dates, Activity context) {
        if(size != 0) radius = size;
        this.color = color;
        this.dates = new HashSet<>(dates);

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
//        view.setSelectionDrawable(drawable);          //  그림 넣기
        view.addSpan(new DotSpan(radius, color)); // 날자밑에 점
    }
}