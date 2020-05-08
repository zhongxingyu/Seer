 package fr.itinerennes.ui.adapter;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.model.Bookmark;
 
 /**
  * @author Jérémie Huchet
  */
 public class BookmarksAdapter extends BaseAdapter {
 
     /** A map containing icons to use for each type of bookmarks. */
     private static final Map<String, Integer> ICONS = new HashMap<String, Integer>();
     static {
        ICONS.put(TypeConstants.TYPE_BIKE, R.drawable.bike_activity_title_icon);
        ICONS.put(TypeConstants.TYPE_BUS, R.drawable.bus_activity_title_icon);
        ICONS.put(TypeConstants.TYPE_SUBWAY, R.drawable.subway_activity_title_icon);
     }
 
     /** The layout inflater. */
     private final LayoutInflater inflater;
 
     /** The bookmarks. */
     private final List<Bookmark> bookmarks;
 
     /**
      * Creates the bookmarks adapter.
      * 
      * @param context
      *            the application context
      */
     public BookmarksAdapter(final Context context, final List<Bookmark> bookmarks) {
 
         inflater = LayoutInflater.from(context);
         this.bookmarks = bookmarks;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.widget.Adapter#getCount()
      */
     @Override
     public final int getCount() {
 
         return bookmarks.size();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.widget.Adapter#getItem(int)
      */
     @Override
     public final Bookmark getItem(final int position) {
 
         return bookmarks.get(position);
     }
 
     /**
      * Returns the item position in the list.
      * <p>
      * {@inheritDoc}
      * 
      * @see android.widget.Adapter#getItemId(int)
      */
     @Override
     public final long getItemId(final int position) {
 
         return position;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
      */
     @Override
     public final View getView(final int position, final View convertView, final ViewGroup parent) {
 
         final Bookmark bm = bookmarks.get(position);
 
         final View row = inflater.inflate(R.layout.row_bookmark, null);
 
         ((TextView) row.findViewById(R.id.bookmark_label)).setText(bm.getLabel());
         final Integer iconResId = ICONS.get(bm.getType());
         if (null != iconResId) {
             ((ImageView) row.findViewById(R.id.bookmark_type_icon)).setImageResource(iconResId);
         }
         return row;
     }
 }
