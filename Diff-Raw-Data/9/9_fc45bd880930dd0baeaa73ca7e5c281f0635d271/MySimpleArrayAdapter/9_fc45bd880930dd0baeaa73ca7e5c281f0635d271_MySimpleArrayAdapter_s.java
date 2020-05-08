 package org.hicapacity.techhui;
 
 import java.util.ArrayList;
 import java.util.List;
 
import android.R;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MySimpleArrayAdapter extends ArrayAdapter<ScheduleElement> {
   private final Context context;
   private final List<ScheduleElement> values;
 
   public MySimpleArrayAdapter(Context context, List<ScheduleElement> values) {
     super(context, org.hicapacity.techhui.R.layout.schedule_list, values);
     this.context = context;
     this.values = new ArrayList<ScheduleElement>(values);
   }
 
   /* (non-Javadoc)
  * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
  */
 @Override
   public View getView(int position, View convertView, ViewGroup parent) {
     LayoutInflater inflater = (LayoutInflater) context
         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     View rowView = inflater.inflate(org.hicapacity.techhui.R.layout.schedule_row, parent, false);
     TextView textView = (TextView) rowView.findViewById(org.hicapacity.techhui.R.id.schedule_description);
 //    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
     TextView timeView = (TextView) rowView.findViewById(org.hicapacity.techhui.R.id.schedule_time);
     
     ScheduleElement scheduleElement = values.get(position);
     
     if (scheduleElement.getTitle() != null) {
     	textView.setText(scheduleElement.getTitle());    	
     }
     else {
     	textView.setText("");
     }
     if (scheduleElement.getTime() != null) {
     	timeView.setText(scheduleElement.getTime());
     }
     else {
     	timeView.setText("");
     }
 
     return rowView;
   }
 }
