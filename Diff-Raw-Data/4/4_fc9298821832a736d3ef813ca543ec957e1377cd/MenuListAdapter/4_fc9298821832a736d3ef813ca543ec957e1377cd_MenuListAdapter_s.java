 package com.cocacola.climateambassador.adapters;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.cocacola.climateambassador.R;
 import com.cocacola.climateambassador.models.NavigationDrawerItem;
 
 import java.util.List;
 
 public class MenuListAdapter extends BaseAdapter {
 
     private List<NavigationDrawerItem> mNavigationItems;
 
     private Context mContext;
     private LayoutInflater mInflater;
 
     public MenuListAdapter(Context context, List<NavigationDrawerItem> navigationItems) {
         mNavigationItems = navigationItems;
         mContext = context;
         mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         return mNavigationItems.size();
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public NavigationDrawerItem getItem(int position) {
         return mNavigationItems.get(position);
     }
 
     @Override
     public int getViewTypeCount() {
         return 2;
     }
 
     @Override
     public int getItemViewType(int position) {
         return getItem(position).isHeader() ? 0 : 1;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
 
         NavigationDrawerItem item = getItem(position);
 
         View v = (item.isHeader()) ? getHeaderView(item) : getNavigationDrawerItemView(item);
 
         return v;
     }
 
     private View getHeaderView(NavigationDrawerItem item) {
 
         View v = mInflater.inflate(R.layout.drawer_header, null);
 
         TextView text = (TextView) v.findViewById(R.id.drawer_header_text);
         text.setText(item.getTitle());
 
         return v;
 
     }
 
     private View getNavigationDrawerItemView(NavigationDrawerItem item) {
 
         View v = mInflater.inflate(R.layout.drawer_list_item, null);
 
        TextView txtTitle = (TextView) v.findViewById(R.id.title);
        ImageView imgIcon = (ImageView) v.findViewById(R.id.icon);
 
         txtTitle.setText(item.getTitle());
         imgIcon.setImageResource(item.getIconId());
 
         v.setOnClickListener(new OnNavigationItemClickListener(item.getActivityClz()));
 
         return v;
 
     }
 
     private class OnNavigationItemClickListener implements View.OnClickListener {
 
         private Class<?> clazzToLaunch;
 
         private OnNavigationItemClickListener(Class<?> clazzToLaunch) {
             this.clazzToLaunch = clazzToLaunch;
         }
 
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(mContext, clazzToLaunch);
             mContext.startActivity(intent);
         }
     }
 
 }
