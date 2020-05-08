 package com.gnuton.newshub;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.gnuton.newshub.types.RSSFeed;
 import com.gnuton.newshub.utils.NetworkUtils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created by gnuton on 6/9/13.
  */
 public class FeedListAdapter extends ArrayAdapter<RSSFeed> {
     private final List<RSSFeed> feeds;
 
     public FeedListAdapter(Context context, int textViewResourceId, List<RSSFeed> feeds) {
         super(context, textViewResourceId, feeds);
         this.feeds = feeds;
     }
 
     public static class ViewHolder{
         public TextView title;
         public TextView desc;
         public TextView entriesCount;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         ViewHolder holder;
 
         // Create delegate (View + view holder) when needed, or get the holder for the current view to convert.
         if (v == null) {
             LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.feedlist_item, null);
             holder = new ViewHolder();
             holder.title = (TextView) v.findViewById(R.id.FeedListItemTitleTextView);
             holder.entriesCount = (TextView) v.findViewById(R.id.FeedListItemEntriesCountTextView);
             holder.desc = (TextView) v.findViewById(R.id.FeedListItemDescTextView);
             v.setTag(holder);
         }
         else
             holder=(ViewHolder)v.getTag();
 
         // Update the delegate setting data stored in the holder
         final RSSFeed f = feeds.get(position);
         if (f != null) {
            holder.title.setText(f.title);
             holder.entriesCount.setText("");//f.entries.size());
             holder.desc.setText(NetworkUtils.getDomainName(f.url));
         }
 
         // returns the updated delegate
         return v;
     }
 
     String dateToString(Calendar cal) {
         String strdate = "";
 
         SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yy HH:mm");
 
         if (cal != null) {
             strdate = sdf.format(cal.getTime());
         }
         return strdate;
     }
 }
