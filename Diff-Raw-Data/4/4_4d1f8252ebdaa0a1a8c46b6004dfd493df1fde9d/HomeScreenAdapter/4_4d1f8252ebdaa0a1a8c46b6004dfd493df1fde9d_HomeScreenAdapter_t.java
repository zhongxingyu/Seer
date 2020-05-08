 package com.fict.opendag;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import android.widget.ImageView;
 
 import com.fict.opendag.model.HomeScreenItem;
 import com.fict.opendag.model.HomeScreenItemType;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * Created by Kaj on 19-9-13.
  */
 public class HomeScreenAdapter extends BaseAdapter {
 
     /**
      * Layout inflater for this listitem.
      */
     private LayoutInflater _inflater;
 
     /**
      * Items contained in listview.
      */
     private ArrayList<HomeScreenItem> _items;
 
     /**
      *
      * @param context application context
      */
     public HomeScreenAdapter(Context context, ArrayList<HomeScreenItem> items) {
         this._inflater = (LayoutInflater)
                 context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         this._items = items;
     }
 
     /**
      * Gets amount of item in adapter.
      * @return item count
      */
     @Override
     public int getCount() {
         return this._items.size();
     }
 
     /**
      * Gets item at specified index.
      * @param position index
      * @return item at index {@position}
      */
     @Override
     public HomeScreenItem getItem(int position) {
         return this._items.get(position);
     }
 
     /**
      * Gets ID of item at specified index.
      * @param position index
      * @return id of item at index {@position}
      */
     @Override
     public long getItemId(int position) {
         return position + 1;
 //        return position;
     }
 
     /**
      * View holder for door list items.
      */
     static class HomeScreenItemViewHolder {
         public TextView title;
         public ImageView icon;
     }
 
     /**
      * Fills list item view with information about a door. Data is
      * passed by using the viewholder pattern.
      * @param position index of item to display in list item
      * @param convertView
      * @param parent
      * @return list item view filled with data of item at index {@position}
      */
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         HomeScreenItemViewHolder holder;
 
         if ( convertView == null ) {
             convertView = this._inflater.inflate(R.layout.activity_main_item, parent, false);
             holder = new HomeScreenItemViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.main_item_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.main_item_icon);
             convertView.setTag(holder);
         } else {
             holder = (HomeScreenItemViewHolder) convertView.getTag();
         }
 
         HomeScreenItem item = this.getItem(position);
         holder.title.setText(item.getTitle());
         holder.icon.setImageResource(this.getIconId(item.getItem()));
 
         return convertView;
     }
 
     private int getIconId(HomeScreenItemType item) {
         if ( item == HomeScreenItemType.SCHEDULE ) {
             return R.drawable.calendar;
         }
         if ( item == HomeScreenItemType.NAVIGATION ) {
             return R.drawable.location_pointer;
         }
         if ( item == HomeScreenItemType.STUDY_INFO ) {
             return R.drawable.file;
         }
         if ( item == HomeScreenItemType.CONTACT ) {
             return R.drawable.email;
         }
         if ( item == HomeScreenItemType.QUIZ ) {
             return R.drawable.crest;
         }
         return R.drawable.paper_plane;
     }
 
 }
