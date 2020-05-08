 package com.example.portfolioapp;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FieldAdapter extends ArrayAdapter<Person>{
 
 	ArrayList<Person> persons = new ArrayList<Person>();
 
     private Activity activity;
  
     public FieldAdapter(Activity a, int textViewResourceId, ArrayList<Person> entries) {
         super(a, textViewResourceId, entries);
         this.persons = entries;
        this.activity = a;
     }
  
     public static class ViewHolder {
         public TextView item1;
         public ImageView item2;
     }
  
     @Override
     /* getView gets ONE view in the LIST! */
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         ViewHolder holder;
         if (v == null) {
        	/*
LayoutInflater inflater = (LayoutInflater)context.getSystemService
      (Context.LAYOUT_INFLATER_SERVICE);
        	 */
             LayoutInflater vi =
                 (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.field_input, null);
 	    // Fetch our item:s in this view so that we can modify them:
             holder = new ViewHolder();
             holder.item1 = (TextView) v.findViewById(R.id.textView);
             holder.item2 = (ImageView) v.findViewById(R.id.imageView);
             v.setTag(holder);
         }
         else
             holder=(ViewHolder)v.getTag();
  	// Load our data into this view..
         final Person person = persons.get(position);
         //final Project project = person.getProjectById(1); // Get the first project of this person.
         final int p = position;
         holder.item1.setText(person.name);
             
         return v;
     }
  
 }
