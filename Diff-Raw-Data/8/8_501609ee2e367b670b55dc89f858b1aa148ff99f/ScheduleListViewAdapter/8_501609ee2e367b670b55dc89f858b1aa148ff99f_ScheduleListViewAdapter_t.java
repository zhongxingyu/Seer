 package ua.edu.tntu.schedule;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 
 import ua.edu.tntu.R;
 
 public class ScheduleListViewAdapter extends ArrayAdapter<ScheduleBlock> {
 
     private Context context;
     private ArrayList<ScheduleBlock> scheduleBlockArrayList;
 
     private class ViewHolder {
         TextView textViewTimeBegin;
         TextView textViewDay;
         TextView textViewTimeEnd;
         TextView textViewLecture;
         TextView textViewLocation;
         View rightSeparatorForDay;
        //        View horizontalSeparator;
         View rightSeparatorForClass;
         View verticalSeparatorForClass;
     }
 
     public ScheduleListViewAdapter(Context context, ArrayList<ScheduleBlock> item) {
         super(context, R.layout.item_for_schedule_list_view, item);
         this.context = context;
         this.scheduleBlockArrayList = item;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder;
 
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.item_for_schedule_list_view, null);
             holder = new ViewHolder();
 
             holder.textViewDay = (TextView) convertView.findViewById(R.id.textViewNameOfDay);
             holder.textViewLecture = (TextView) convertView.findViewById(R.id.textViewLecture);
             holder.textViewLocation = (TextView) convertView.findViewById(R.id.textViewLocation);
             holder.textViewTimeBegin = (TextView) convertView.findViewById(R.id.textViewTimeBegin);
             holder.textViewTimeEnd = (TextView) convertView.findViewById(R.id.textViewTimeEnd);
             holder.rightSeparatorForDay = (View) convertView.findViewById(R.id.separatorRightForDay);
             holder.verticalSeparatorForClass = (View) convertView.findViewById(R.id.separator);
             holder.rightSeparatorForClass = (View) convertView.findViewById(R.id.separatorRightForClass);
 
             convertView.setTag(holder);
 
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         if (scheduleBlockArrayList.get(position).getNameOfDay() != null) {
             holder.textViewDay.setVisibility(View.VISIBLE);
             holder.rightSeparatorForDay.setVisibility(View.VISIBLE);
             holder.textViewLecture.setVisibility(View.GONE);
             holder.textViewTimeBegin.setVisibility(View.GONE);
             holder.textViewTimeEnd.setVisibility(View.GONE);
             holder.textViewLocation.setVisibility(View.GONE);
             holder.rightSeparatorForClass.setVisibility(View.GONE);
             holder.verticalSeparatorForClass.setVisibility(View.GONE);
             holder.textViewDay.setText(scheduleBlockArrayList.get(position).getNameOfDay());
         } else {
             holder.textViewDay.setVisibility(View.GONE);
             holder.rightSeparatorForDay.setVisibility(View.GONE);
             holder.textViewLecture.setVisibility(View.VISIBLE);
             holder.textViewTimeBegin.setVisibility(View.VISIBLE);
             holder.textViewTimeEnd.setVisibility(View.VISIBLE);
             holder.textViewLocation.setVisibility(View.VISIBLE);
             holder.rightSeparatorForClass.setVisibility(View.VISIBLE);
             holder.verticalSeparatorForClass.setVisibility(View.VISIBLE);
             holder.textViewTimeBegin.setText(scheduleBlockArrayList.get(position).getTimeBegin());
             holder.textViewTimeEnd.setText(scheduleBlockArrayList.get(position).getTimeEnd());
             holder.textViewLecture.setText(scheduleBlockArrayList.get(position).getLecture());
             holder.textViewLocation.setText(scheduleBlockArrayList.get(position).getLocation());
         }
 
 
         /*if (scheduleBlockArrayList.get(position).getNameOfDay() != null) {
             View rowViewDay = inflater.inflate(R.layout.item_day, parent, false);
             TextView textViewDay = (TextView) rowViewDay.findViewById(R.id.textViewDay);
             textViewDay.setText(scheduleBlockArrayList.get(position).getNameOfDay());
             return rowViewDay;
         } else {
             View rowView = inflater.inflate(R.layout.item_for_schedule_list_view, parent, false);
             TextView textViewTimeBegin = (TextView) rowView.findViewById(R.id.textViewTimeBegin);
             textViewTimeBegin.setText(scheduleBlockArrayList.get(position).getTimeBegin());
             TextView textViewTimeEnd = (TextView) rowView.findViewById(R.id.textViewTimeEnd);
             textViewTimeEnd.setText(scheduleBlockArrayList.get(position).getTimeEnd());
             TextView textViewLecture = (TextView) rowView.findViewById(R.id.textViewLecture);
             textViewLecture.setText(scheduleBlockArrayList.get(position).getLecture());
             TextView textViewLocation = (TextView) rowView.findViewById(R.id.textViewLocation);
             textViewLocation.setText(scheduleBlockArrayList.get(position).getLocation());
             return rowView;
         }*/
         return convertView;
     }
 }
