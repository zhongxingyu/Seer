 package com.nerdery.android.codechallenge.presentation;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import com.nerdery.android.codechallenge.R;
 import com.nerdery.android.codechallenge.presentation.model.Listing;
 
 import java.util.List;
 
 public class ListingsAdapter extends ArrayAdapter<Listing> {
     private final Context context;
     private final List<Listing> values;
 
     public ListingsAdapter(Context context, List<Listing> values) {
         super(context, R.layout.activity_main, values);
         this.context = context;
         this.values = values;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View rowView = inflater.inflate(R.layout.activity_main, parent, false);
 
         Listing targetListing = values.get(position);
 
        TextView title = (TextView)rowView.findViewById(R.id.activity_main_textview_title);
         title.setText(targetListing.getTitle());
 
        TextView subreddit = (TextView)rowView.findViewById(R.id.activity_main_textview_subreddit);
         subreddit.setText(targetListing.getSubreddit());

        return rowView;
     }
 }
