 package org.gnuton.newshub.adapters;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import org.gnuton.newshub.R;
 import org.gnuton.newshub.types.RSSEntry;
 import org.gnuton.newshub.utils.FontsProvider;
 import org.gnuton.newshub.utils.Utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 public class ArticleListAdapter extends ArrayAdapter<RSSEntry> {
     private final List<RSSEntry> mEntries;
     private int dayOfTheMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
     private final String mFeedTitle;
 
     public ArticleListAdapter(Context context, int textViewResourceId, List<RSSEntry> entries, String feedTitle) {
         super(context, textViewResourceId, entries);
         mEntries = entries;
         mFeedTitle = feedTitle;
     }
 
     public static class ViewHolder{
         public TextView title;
         public TextView date;
         public TextView url;
         public ImageView sideBar;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         ViewHolder holder;
 
         // Create delegate (View + view holder) when needed, or get the holder for the current view to convert.
         if (v == null) {
             LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.articlelist_item, null);
             holder = new ViewHolder();
             assert v != null;
             holder.title = (TextView) v.findViewById(R.id.ListItemTitleTextView);
 
             holder.url = (TextView) v.findViewById(R.id.ListItemProviderTextView);
             holder.url.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
             holder.date = (TextView) v.findViewById(R.id.ListItemDateTextView);
             holder.date.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
             holder.sideBar = (ImageView) v.findViewById(R.id.sidebar);
 
             v.setTag(holder);
         }
         else
             holder=(ViewHolder)v.getTag();
 
         // Update the delegate setting data stored in the holder
         final RSSEntry e = mEntries.get(position);
         if (e != null && !e.title.isEmpty()) {
             Spanned titleSpanned = Html.fromHtml(e.title, null, null);
             holder.title.setText(titleSpanned, TextView.BufferType.SPANNABLE);
             holder.title.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"), e.isRead ? Typeface.NORMAL : Typeface.BOLD);
 
 
             holder.url.setText(mFeedTitle);
             holder.date.setText(dateToString(e.date));
             holder.sideBar.setBackgroundColor(Utils.generateColor(mFeedTitle));
         } else {
             holder.title.setText("");
             holder.url.setText("");
             holder.date.setText("");
             holder.sideBar.setBackgroundColor(Color.TRANSPARENT);
         }
 
         // returns the updated delegate
         return v;
     }
 
     String dateToString(Calendar cal) {
        String strdate = null;
         if (cal == null)
             return "";
 
         SimpleDateFormat sdf;
 
         if (dayOfTheMonth == cal.get(Calendar.DAY_OF_MONTH))
             sdf = new SimpleDateFormat("HH:mm");
         else
             sdf = new SimpleDateFormat("EEE, dd MMM");
 
 
        strdate = sdf.format(cal.getTime());
 
        return strdate;
     }
 }
