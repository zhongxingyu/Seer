 package se.tidensavtryck;
 
 import java.util.List;
 
 import com.google.android.maps.*;
 import se.tidensavtryck.model.Place;
 import se.tidensavtryck.model.Route;
 
 import com.markupartist.android.widget.ActionBar;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class RouteActivity extends MapActivity {
 
     MapView mMapView;
     List<Overlay> mMapOverlays;
     Route mRoute;
     ListView mPlacesList;
 	private PlaceItemizedOverlay itemizedOverlay;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.route);
 
         mRoute = (Route) getIntent().getParcelableExtra("route");
         initActionBar();
         
         mMapView = (MapView) findViewById(R.id.mapview);
         mMapView.setBuiltInZoomControls(true);
         
         mMapOverlays = mMapView.getOverlays();
         itemizedOverlay = new PlaceItemizedOverlay(this, mMapView);
 
         initList(mRoute);
     	int index = 1;
         for (Place place : mRoute.getPlaces()) {
             addPlaceOverlay(place, index);
             index++;
         }
         mMapOverlays.add(itemizedOverlay);
 
         final MapController mc = mMapView.getController();
 
         if (mMapOverlays.size() > 0) {
             PlaceItemizedOverlay first = (PlaceItemizedOverlay) mMapOverlays.get(0);
             GeoPoint point = first.getCenter();
 
             mc.animateTo(point);
             mc.setZoom(16);
         }
     }
 
     private void initActionBar() {
         ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
         actionBar.setHomeAction(new ActionBar.IntentAction(
                 this, StartActivity.createIntent(this),
                 R.drawable.ic_actionbar_home_default));
     }
 
     private void initList(Route route) {
         mPlacesList = (ListView) findViewById(R.id.places_list);
         // The list only exists in landscape mode.
         if (mPlacesList != null) {
             PlaceAdapter placeAdapter =
                 new PlaceAdapter(this, route.getPlaces());
             mPlacesList.setAdapter(placeAdapter);
         }
     }
 
     /**
      * Creates an overlay from a {@link Place}.
      * @param place The place.
      * @return An overlay.
      */
    private void addPlaceOverlay(Place place, int index) {
         GeoPoint point = new GeoPoint(
                 (int)(place.getGeoLocation().getLatitude()*1E6),
                 (int)(place.getGeoLocation().getLongitude()*1E6));
         OverlayItem overlayItem = new OverlayItem(point, place.getTitle(), 
                 place.getDescription());
         
         Drawable drawable = createMarker(index);
         
         itemizedOverlay.addOverlay(overlayItem, drawable, place);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     private Drawable createMarker(int index) {
     	Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dot);
 
     	int size = (index < 10 ? 27 : 24);
     	int x = (index < 10 ? 19 : 13);
     	int y = (index < 10 ? 74 : 72);
     	
     	// create a mutable bitmap with the same size as the background image
     	Bitmap bmOverlay = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), 
     	    Bitmap.Config.ARGB_4444);
     	// create a canvas on which to draw
     	Canvas canvas = new Canvas(bmOverlay);
 
     	Paint paint = new Paint();
     	paint.setColor(Color.BLACK);
     	paint.setTextSize(size);
     	paint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
 
     	// if the background image is defined in main.xml, omit this line
     	canvas.drawBitmap(mBitmap, 0, 0, null);
     	// draw the text and the point
 
     	canvas.drawText(""+index, x, y, paint);
     	
     	// two digit
     	// 21, 26, size 20
     	// single digit
     	// 23, 29, size 24
 
     	// set the bitmap into the ImageView
     	return new BitmapDrawable(bmOverlay);
     }
 
     /**
      * 
      * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
      */
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         if (newConfig.orientation == newConfig.ORIENTATION_LANDSCAPE) {
             Log.d("Route", "is landscape");
             mPlacesList.setVisibility(View.VISIBLE);
         } else {
             mPlacesList.setVisibility(View.GONE);
         }
 
         super.onConfigurationChanged(newConfig);
     }
 
     private class PlaceAdapter extends ArrayAdapter<Place> {
         private LayoutInflater mInflater;
 
         public PlaceAdapter(Context context, List<Place> places) {
             super(context, R.layout.place_list_row, places);
 
             mInflater = (LayoutInflater) context.getSystemService(
                     Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             convertView = mInflater.inflate(R.layout.place_list_row, null);
 
             Place place = getItem(position);
             TextView titleView = (TextView) convertView.findViewById(R.id.row_place_title);
             titleView.setText(String.format("%s", place.getTitle()));
             TextView placeNumber = (TextView) convertView.findViewById(R.id.row_place_number);
             placeNumber.setText(String.format("%s", position + 1));
             
             return convertView;
         }
     }
 }
