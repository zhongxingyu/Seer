 package com.data;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Wolfram
  * Date: 11.11.13
  * Time: 14:56
  */
 
 import android.content.Context;
import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import com.activities.R;
 
 import java.util.ArrayList;
 
 public class ChatAdapter extends ArrayAdapter<String> {
     private final Context context;
     private final ArrayList<String> messages, timestamps;
 
     public ChatAdapter(Context context, ArrayList<String> messages, ArrayList<String> timestamps) {
         super(context, R.layout.layout_chathistory_list, messages);
         this.context = context;
         this.messages = messages;
         this.timestamps = timestamps;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View rowView = inflater.inflate(R.layout.layout_chathistory_list, parent, false);
        if (messages.get(position).contains("ä")) {
            rowView.setBackgroundColor(Color.RED);
        }
         TextView msg = (TextView) rowView.findViewById(R.id.msg);
         TextView time = (TextView) rowView.findViewById(R.id.timestamp);
         msg.setText(messages.get(position));
         time.setText(timestamps.get(position));
         return rowView;
     }
 }
