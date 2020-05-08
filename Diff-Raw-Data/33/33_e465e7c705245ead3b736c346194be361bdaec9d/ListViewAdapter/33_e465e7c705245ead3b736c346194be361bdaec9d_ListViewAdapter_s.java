 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.utility;
 
import no.hials.muldvarp.video.*;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
import no.hials.muldvarp.domain.Course;
 import no.hials.muldvarp.entities.ListItem;
 
 /**
  * Bla bla bla KUSTOM MEGA-ADAPTA
  * 
  * @author johan
  */
 public class ListViewAdapter extends ArrayAdapter {
     private LayoutInflater mInflater;
     private ArrayList items;
     private Context context;
     private int resource;
     private boolean showdetails;
     private List orig_items;
     
     
     /**
      * Constructor for the ListViewAdapter class.
      * 
      * @param context
      * @param resource Integer value of the resource ID
      * @param textViewResourceIdX
      * @param listItemArray
      * @param showdetails 
      */
     public ListViewAdapter(Context context, int resource, int textViewResourceId, ArrayList listItemArray, boolean showdetails) {
         super(context, textViewResourceId, listItemArray);
         mInflater = LayoutInflater.from(context);
         this.items = listItemArray;
         this.orig_items = listItemArray;
         this.context = context;
         this.resource = resource;
         this.showdetails = showdetails;
     }
 
     /**
      * Returns int value of the array's size
      * 
      * @return Returns int value of the array's size
      */
     @Override
     public int getCount() {
         return items.size();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder;
 
         if (convertView == null) {
                 convertView = mInflater.inflate(resource, parent, false);
                 holder = new ViewHolder();
                 holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                 holder.name = (TextView) convertView.findViewById(R.id.name);
                 holder.detail = (TextView) convertView.findViewById(R.id.detail);
 
                 convertView.setTag(holder);
         } else {
                 holder = (ViewHolder) convertView.getTag();
         }
 
         ListItem listItem = (ListItem) items.get(position);
         
         
         //Set ListItem name
         holder.name.setText(listItem.getItemName());
         
         
         //Show details if true
         if (showdetails == true) {
             holder.detail.setText(listItem.getSmallDetail());
         }
                 
 //        holder.icon.setImageBitmap(c.getThumb());
         
         return convertView;
     }
     
     /**
      * Function which filters out entries that don't match the supplied CharSequence
      * 
      * @param filter CharSequence 
      */
     public void filter(CharSequence filter) {
         
         ArrayList filtered = new ArrayList();
         
         for (ListItem listItem : (ArrayList<ListItem>)orig_items)
         {
             if (listItem.getItemName().toLowerCase().contains(filter.toString().toLowerCase()))
             {
                 filtered.add(listItem);
             }
         }
         
         this.items = filtered;
         
         if("".equals(filter.toString())) {
             this.items = (ArrayList) this.orig_items;
         }
         
         notifyDataSetChanged();  
     }
 
     static class ViewHolder {
         ImageView icon;
         TextView name;
         TextView detail;
     }
     
     
 }
 
