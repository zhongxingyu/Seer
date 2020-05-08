 package fr.itinerennes.ui.adapter;
 
 import java.util.Date;
 import java.util.List;
 
 import android.text.format.DateUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.onebusaway.model.TripStopTime;
 import fr.itinerennes.ui.activity.ItinerennesContext;
 
 /**
  * @author Jérémie Huchet
  */
 public class BusRouteStopsAdapter extends BaseAdapter {
 
     /** The itinérennes context. */
     private final ItinerennesContext context;
 
     /** A reference to the layout inflater. */
     private final LayoutInflater inflater;
 
     /** The initial stop identifier on which set a marker. */
     private final String initialStopId;
 
     /** The list of arrival times and departure times of the bus for each stop of the trip. */
     private final List<TripStopTime> arrivalAndDepartures;
 
     /**
      * Is the route accessible ? Determines if the handistar icon should be displayed for accessible
      * stops in the list.
      */
     private final boolean routeIsAccessible;
 
     /**
      * Constructor.
      * 
      * @param context
      *            The android context
      * @param initialStopId
      *            the stop id where to set a marker
      * @param arrivalAndDepartures
      *            the list of arrival times and departure times of the bus for each stop of the trip
      */
     public BusRouteStopsAdapter(final ItinerennesContext context, final String initialStopId,
             final List<TripStopTime> arrivalAndDepartures, final boolean routeIsAccessible) {
 
         this.context = context;
         this.inflater = LayoutInflater.from(context);
         this.arrivalAndDepartures = arrivalAndDepartures;
         this.initialStopId = initialStopId;
         this.routeIsAccessible = routeIsAccessible;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.widget.Adapter#getCount()
      */
     @Override
     public final int getCount() {
 
         return arrivalAndDepartures.size();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.widget.Adapter#getItem(int)
      */
     @Override
     public final TripStopTime getItem(final int position) {
 
         return arrivalAndDepartures.get(position);
     }
 
     /**
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
 
         final View view = inflater.inflate(R.layout.trip_time, null);
 
         final TripStopTime stopTime = arrivalAndDepartures.get(position);
 
         final TextView stopName = (TextView) view.findViewById(R.trip_time.stop_name);
         stopName.setText(stopTime.getStop().getName());
 
         final TextView departureTime = (TextView) view.findViewById(R.trip_time.departure_time);
         departureTime.setText(DateUtils.formatDateTime(context, stopTime.getDepartureTime()
                 .getTime(), DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME));
 
         final TextView relativeTime = (TextView) view.findViewById(R.trip_time.relative_time);
         relativeTime.setText(DateUtils.getRelativeTimeSpanString(stopTime.getDepartureTime()
                 .getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS,
                 DateUtils.FORMAT_ABBREV_RELATIVE));
 
         // If the current view is the view on which a marker is needed
         // sets a blue drawable on the left
         if (initialStopId.equals(stopTime.getStop().getId())) {
             ((ImageView) view.findViewById(R.trip_time.listview_separator))
                    .setImageResource(R.drawable.listview_separator);
         }
 
         // if the current view represents a stop where the bus is already passed
         // sets the font color to grey
         if (stopTime.getDepartureTime().before(new Date())) {
             stopName.setTextAppearance(context, R.style.text_grey_bold);
             departureTime.setTextAppearance(context, R.style.text_grey_bold);
             relativeTime.setTextAppearance(context, R.style.text_grey);
         }
 
         final ProgressBar progressBar = (ProgressBar) view.findViewById(R.trip_time.progress_bar);
 
         if (routeIsAccessible) {
             /* Display handistar icon if current stop and current route are accessible. */
             final ImageView handistar = (ImageView) view.findViewById(R.trip_time.stop_wheelchair);
             if (context.getAccessibilityService().isAccessible(stopTime.getStop().getId(),
                     TypeConstants.TYPE_BUS)) {
                 handistar.setVisibility(View.VISIBLE);
             }
         }
 
         return view;
     }
 
     /**
      * Gets the index of the item representing a stop time at the given stop.
      * 
      * @param stopId
      *            the identifier of the stop you want the index
      * @return the position of the requested stop.
      */
     public final int getIndexForStopId(final String stopId) {
 
         final int length = arrivalAndDepartures.size();
         for (int i = 0; i < length; i++) {
             if (arrivalAndDepartures.get(i).getStop().getId().equalsIgnoreCase(stopId)) {
                 return i;
             }
         }
         return 0;
     }
 }
