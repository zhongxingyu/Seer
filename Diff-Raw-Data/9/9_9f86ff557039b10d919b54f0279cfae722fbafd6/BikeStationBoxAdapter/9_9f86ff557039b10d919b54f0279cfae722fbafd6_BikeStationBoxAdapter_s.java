 package fr.itinerennes.ui.adapter;
 
 import java.io.IOException;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import fr.dudie.keolis.client.KeolisClient;
 import fr.dudie.keolis.model.BikeStation;
 
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.ui.activity.ItineRennesActivity;
 import fr.itinerennes.ui.views.event.ToggleStarListener;
 import fr.itinerennes.ui.views.overlays.OverlayItem;
 import fr.itinerennes.ui.views.overlays.StopOverlayItem;
 
 /**
  * @author Jérémie Huchet
  */
 public class BikeStationBoxAdapter implements MapBoxAdapter<BikeStation> {
 
     /** The itinerennes context. */
     private final ItineRennesActivity context;
 
     /** The layout inflater. */
     private final LayoutInflater inflater;
 
     /**
      * Creates the bus station adapter for map box.
      * 
      * @param context
      *            the context
      */
     public BikeStationBoxAdapter(final ItineRennesActivity context) {
 
         this.context = context;
         inflater = context.getLayoutInflater();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#getView(java.lang.Object)
      */
     @Override
     public final View getView(final OverlayItem item) {
 
         final StopOverlayItem bikeStation = (StopOverlayItem) item;
 
         final View bikeView = inflater.inflate(R.layout.vw_mapbox_bike, null);
         ((TextView) bikeView.findViewById(R.id.map_box_title)).setText(bikeStation.getLabel());
 
         final ToggleButton star = (ToggleButton) bikeView
                 .findViewById(R.id.map_box_toggle_bookmark);
         star.setChecked(context.getApplicationContext().getBookmarksService()
                 .isStarred(TypeConstants.TYPE_BIKE, bikeStation.getId()));
         star.setOnCheckedChangeListener(new ToggleStarListener(context, TypeConstants.TYPE_BIKE,
                 bikeStation.getId(), bikeStation.getLabel()));
 
         return bikeView;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#onStartLoading(android.view.View)
      */
     @Override
     public final void onStartLoading(final View view) {
 
         view.findViewById(R.id.map_box_progressbar).setVisibility(View.VISIBLE);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#doInBackground(android.view.View,
      *      java.lang.Object)
      */
     @Override
     public final BikeStation doInBackground(final View view, final OverlayItem item) {
 
         final KeolisClient keolisClient = context.getApplicationContext().getKeolisClient();
         try {
             return keolisClient.getBikeStation(((StopOverlayItem) item).getId());
         } catch (final IOException e) {
             context.getApplicationContext().getExceptionHandler().handleException(e);
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#updateView(android.view.View, java.lang.Object)
      */
     @Override
     public final void updateView(final View view, final BikeStation upToDateStation) {
 
         final View bikeStationDetails = view.findViewById(R.id.map_box_bike_details);
 
         if (upToDateStation == null) {
             // an error occurred during backgroundLoad()
             bikeStationDetails.setVisibility(View.GONE);
             return;
         } else {
             bikeStationDetails.setVisibility(View.VISIBLE);
         }
 
         final TextView availablesSlots = (TextView) bikeStationDetails
                 .findViewById(R.id.available_slots);
         availablesSlots.setText(String.valueOf(upToDateStation.getAvailableSlots()));
 
         final TextView availablesBikes = (TextView) bikeStationDetails
                 .findViewById(R.id.available_bikes);
         availablesBikes.setText(String.valueOf(upToDateStation.getAvailableBikes()));
 
         final ProgressBar gauge = (ProgressBar) bikeStationDetails
                 .findViewById(R.id.bike_station_gauge);
         gauge.setMax(upToDateStation.getAvailableBikes() + upToDateStation.getAvailableSlots());
         gauge.setIndeterminate(false);
         gauge.setProgress(upToDateStation.getAvailableBikes());
         gauge.setSecondaryProgress(upToDateStation.getAvailableBikes()
                 + upToDateStation.getAvailableSlots());
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.adapter.MapBoxAdapter#onStopLoading(android.view.View)
      */
     @Override
     public final void onStopLoading(final View view) {
 
         view.findViewById(R.id.map_box_progressbar).setVisibility(View.GONE);
     }
 
 }
