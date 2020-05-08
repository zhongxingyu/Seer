 package com.tuvistavie.meetup.event.util;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.tuvistavie.meetup.R;
 import com.tuvistavie.meetup.event.model.DateCell;
 import com.tuvistavie.meetup.util.DateTimeUtil;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by daniel on 9/4/13.
  */
 public class CalendarAdapter extends ArrayAdapter<DateCell> {
 
     private static final int firstDayOfWeek = Calendar.SUNDAY;
 
     private Date currentDate;
 
     private Map<String, List<DateCell>> dateCells;
 
     public CalendarAdapter(Context context, int resource) {
         super(context, resource);
         dateCells = new HashMap<String, List<DateCell>>();
         initDates();
     }
 
     private void initDates() {
         setDates(new Date());
     }
 
     public void gotoPreviousMonth() {
         setDates(DateTimeUtil.getPreviousMonth(currentDate));
     }
 
     public Date getCurrentDate() {
         return currentDate;
     }
 
     public void gotoNextMonth() {
         setDates(DateTimeUtil.getNextMonth(currentDate));
     }
 
     public void setDates(Date date) {
         currentDate = date;
         String key = DateTimeUtil.getMonthKey(date);
         clear();
        // FIXME: dates added for month n+1 while showing month n won't appear when showing month n + 1
         List<DateCell> dates =  dateCells.get(key);
         if(dates == null) {
             dates = DateTimeUtil.generateDateForMonth(date, firstDayOfWeek);
             dateCells.put(key, dates);
         }
         addAll(dates);
     }
 
     // FIXME: way too ugly and buggy
     private void setCellHeight(View cellView) {
         int rows = DateTimeUtil.getLastDayOfMonth(currentDate) / 7;
         Point size = new Point();
         ((Activity)getContext()).getWindowManager().getDefaultDisplay().getSize(size);
         cellView.setMinimumHeight((int)(size.y * 0.5) / rows);
     }
 
    private View initializeView(View contextView, ViewGroup parent) {
         View cellView;
         if(contextView == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                     Context.LAYOUT_INFLATER_SERVICE);
             cellView = inflater.inflate(R.layout.calendar_cell, parent, false);
             setCellHeight(cellView);
         } else {
             cellView = contextView;
         }
         return cellView;
     }
 
 
     @Override
     public View getView(int position, View contextView, ViewGroup parent) {
         DateCell dateCell = getItem(position);
        View cellView = initializeView(contextView, parent);
         TextView calendarTextView = (TextView)cellView.findViewById(R.id.day_text);
         int dayOfMonth = DateTimeUtil.getDayOfMonth(dateCell.getDate());
 
         calendarTextView.setText(String.valueOf(dayOfMonth));
         cellView.setOnClickListener(new CellClickListener(dateCell));
 
         if(dateCell.isSelected()) {
             calendarTextView.setBackgroundColor(getContext().getResources().getColor(R.color.light_blue));
             cellView.setBackgroundColor(getContext().getResources().getColor(R.color.light_blue));
         } else {
             calendarTextView.setBackgroundColor(Color.WHITE);
             cellView.setBackgroundColor(Color.WHITE);
         }
 
         return cellView;
     }
 
     private class CellClickListener implements View.OnClickListener {
         private DateCell dateCell;
         public CellClickListener(DateCell dateCell) {
             this.dateCell = dateCell;
         }
         @Override
         public void onClick(View v) {
             dateCell.setSelected(!dateCell.isSelected());
             notifyDataSetChanged();
         }
     }
 }
