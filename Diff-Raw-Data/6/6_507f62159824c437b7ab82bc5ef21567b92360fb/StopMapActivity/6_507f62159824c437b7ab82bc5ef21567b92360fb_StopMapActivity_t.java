 /*
  * Copyright 2010 Colin Paton - cozzarp@googlemail.com
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.redbus.PointTree.BusStopTreeNode;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Bitmap.Config;
 import android.location.Address;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.Projection;
 
 public class StopMapActivity extends MapActivity implements GeocodingResponseListener  {
 
 	private MapView mapView;
 	private MapController mapController;
 	private MyLocationOverlay myLocationOverlay;
 	private StopOverlay stopOverlay;
 
 	private ProgressDialog busyDialog = null;
 	private int expectedRequestId = -1;
 
 	public class StopOverlay extends Overlay {
 
 		private static final int stopRadius = 5;
 
 		private Projection projection;
 		private Paint blackBrush;
 
 		private ArrayList<BusStopTreeNode> nodes;
 
 		private double tlx, oldtlx;
 		private double tly, oldtly;
 		private double brx, oldbrx;
 		private double bry, oldbry;
 
 		private long serviceFilter;
 		private Bitmap normalStopBitmap;
 		private Paint normalStopPaint;
 		private Bitmap filteredStopBitmap;
 		private Paint filteredStopPaint;
 		private Paint nullPaint;
 		
 		private Bitmap showMoreStopsBitmap;
 		private Bitmap showServicesBitmap;
 
 		private static final String showMoreStopsText = "Zoom in to see more stops";
 		private static final String showMoreServicesText = "Zoom in to see services";
 
 		public StopOverlay(MapView view, long serviceFilter) {
 			this.serviceFilter = serviceFilter;
 			this.projection = view.getProjection();
 
 			oldtlx = oldtly = oldbrx = oldbry = -1;
 
 			blackBrush = new Paint();
 			blackBrush.setARGB(180,0,0,0);
 
 			normalStopPaint = new Paint();
 			normalStopPaint.setARGB(250, 187, 39, 66); // rEdB[r]us[h] ;-)
 			normalStopPaint.setAntiAlias(true);
 			normalStopBitmap = Bitmap.createBitmap(stopRadius * 2, stopRadius * 2, Config.ARGB_4444);
 			normalStopBitmap.eraseColor(Color.TRANSPARENT);
 			Canvas stopCanvas = new Canvas(normalStopBitmap);
 			stopCanvas.drawOval(new RectF(0,0,stopRadius*2,stopRadius*2), normalStopPaint);
 			
 			filteredStopPaint = new Paint();
 			filteredStopPaint.setARGB(250, 195, 195, 195);
 			filteredStopPaint.setAntiAlias(true);
 			filteredStopBitmap = Bitmap.createBitmap(stopRadius*2, stopRadius*2, Config.ARGB_4444);
 			filteredStopBitmap.eraseColor(Color.TRANSPARENT);
 			stopCanvas = new Canvas(filteredStopBitmap);
 			stopCanvas.drawOval(new RectF(0, 0, stopRadius*2, stopRadius*2), filteredStopPaint);
 
 			Rect bounds = new Rect();
 			normalStopPaint.getTextBounds(showMoreStopsText, 0, showMoreStopsText.length(), bounds);
 			showMoreStopsBitmap = Bitmap.createBitmap(bounds.right + 20, Math.abs(bounds.bottom) + Math.abs(bounds.top) + 20, Config.ARGB_8888);
 			showMoreStopsBitmap.eraseColor(blackBrush.getColor());
 			Canvas tmpCanvas = new Canvas(showMoreStopsBitmap);
 			tmpCanvas.drawText(showMoreStopsText, 10, Math.abs(bounds.top) + 10, normalStopPaint);
 			
 			normalStopPaint.getTextBounds(showMoreServicesText, 0, showMoreServicesText.length(), bounds);
 			showServicesBitmap = Bitmap.createBitmap(bounds.right + 20, Math.abs(bounds.bottom) + Math.abs(bounds.top) + 20, Config.ARGB_8888);
 			showServicesBitmap.eraseColor(blackBrush.getColor());
 			tmpCanvas = new Canvas(showServicesBitmap);
 			tmpCanvas.drawText(showMoreServicesText, 10, Math.abs(bounds.top) + 10, normalStopPaint);
 			
 			nullPaint = new Paint();
 		}
 
 		public void draw(Canvas canvas, MapView view, boolean shadow) {
 			super.draw(canvas, view,shadow);
 
 			if (shadow && (serviceFilter == 0xffffffffffffL))
 				return;
 			
 			PointTree pointTree = PointTree.getPointTree(StopMapActivity.this);
 
 			GeoPoint tl = projection.fromPixels(0,canvas.getHeight());
 			GeoPoint br = projection.fromPixels(canvas.getWidth(),0);
 			tlx = tl.getLatitudeE6() / 1E6;
 			tly = tl.getLongitudeE6() / 1E6;
 			brx = br.getLatitudeE6() / 1E6;
 			bry = br.getLongitudeE6() / 1E6;
 
 			// if we're zoomed out too far, switch to just iterating all stops and skipping to preserve speed.
 			if (view.getZoomLevel() < 15) {
 				int skip = 2;
 				if (view.getZoomLevel() == 13)
 					skip = 4;
 				if (view.getZoomLevel() == 12)
 					skip = 6;
 				if (view.getZoomLevel() < 12)
 					skip = 8;
 
 				Point stopCircle = new Point();
 				int idx = 0;
 				for (BusStopTreeNode node: pointTree.nodes) {
 					if ((idx++ % skip) != 0)
 						continue;
 					if ((node.x < tlx) || (node.y < tly) || (node.x > brx) || (node.y > bry))
 						continue;
 
 					projection.toPixels(new GeoPoint((int)(node.x * 1E6),(int)(node.y * 1E6)), stopCircle);
 					Bitmap bmp = normalStopBitmap;
 					if ((serviceFilter & node.servicesMap) == 0) {
 						if (!shadow)
 							continue;
 						bmp = filteredStopBitmap;
 					}
 					canvas.drawBitmap(bmp, (float) stopCircle.x - stopRadius, (float) stopCircle.y - stopRadius, nullPaint);
 				}
 				
 				canvas.drawBitmap(showMoreStopsBitmap, 0, 0, nullPaint);
 				return;
 			}
 
 			// For some reason, draw is called LOTS of times. Only requery the DB if
 			// the co-ords change.
 			if (tlx != oldtlx || tly != oldtly || brx != oldbrx || bry != oldbry) {
 				nodes = pointTree.findRect(tlx,tly,brx,bry);
 				oldtlx = tlx; oldtly = tly; oldbrx = brx; oldbry = bry;
 			}
 
 			// Prevent zoomed out view looking like abstract art
 			// with too many labels drawn...				
 			boolean showServiceLabels = view.getZoomLevel() > 16;				
 
 			// For each node, draw a circle and optionally service number list
 			Point stopCircle = new Point();
 			for (BusStopTreeNode node: nodes) {
 				projection.toPixels(new GeoPoint((int)(node.x * 1E6),(int)(node.y * 1E6)), stopCircle);
 				
 				Bitmap bmp = normalStopBitmap;
 				boolean showService = showServiceLabels;
 				if ((serviceFilter & node.servicesMap) == 0) {
 					if (!shadow)
 						continue;
 					bmp = filteredStopBitmap;
 					showService = false;
 				}
 				
 				canvas.drawBitmap(bmp, (float) stopCircle.x - stopRadius, (float) stopCircle.y - stopRadius, null);
 				if (showService)
 					canvas.drawText(pointTree.formatServices(node.servicesMap & serviceFilter, 3), stopCircle.x+stopRadius, stopCircle.y+stopRadius, normalStopPaint);
 			}
 
 			// draw service label info text last
 			if (!showServiceLabels)
 				canvas.drawBitmap(showServicesBitmap, 0, 0, nullPaint);
 		}
 		
 		
 		@Override
 		public boolean onTap(GeoPoint point, MapView mapView)
 		{
 			final double lat = point.getLatitudeE6()/1E6;
 			final double lng = point.getLongitudeE6()/1E6;
 			
 			final PointTree.BusStopTreeNode node = PointTree.getPointTree(StopMapActivity.this).findNearest(lat,lng);
 
 			// Yuk - there must be a better way to convert GeoPoint->Point than this?			
 			Location touchLoc = new Location("");
 			touchLoc.setLatitude(lat);
 			touchLoc.setLongitude(lng);
 
 			Location stopLoc = new Location("");
 			stopLoc.setLatitude(node.x);
 			stopLoc.setLongitude(node.y);
 
 			// Use distance of 50metres - ignore out of range touches
 			if (touchLoc.distanceTo(stopLoc) < 50) {
 				View v = StopMapActivity.this.getLayoutInflater().inflate(R.layout.stoppopup, null);
 				
 				final AlertDialog d = new AlertDialog.Builder(StopMapActivity.this).
 	    			setTitle(node.stopName + " (" + node.stopCode + ")").
 	    			setView(v).
 	    			create();
 				
 				((TextView) v.findViewById(R.id.stoppopup_services)).setText("Services from this stop:\n" + PointTree.getPointTree(StopMapActivity.this).formatServices(node.servicesMap, -1));
 				((Button) v.findViewById(R.id.stoppopup_streetview)).setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 			            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.streetview:cbll=" + node.x + "," + node.y + "&cbp=1,180,,0,2.0")));
 					}
 				});
 				((Button) v.findViewById(R.id.stoppopup_filter)).setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						stopOverlay.serviceFilter = node.servicesMap;
 						d.dismiss();
 						StopMapActivity.this.mapView.invalidate();
 					}
 				});
 				((Button) v.findViewById(R.id.stoppopup_viewtimes)).setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						BusTimesActivity.showActivity(StopMapActivity.this, node.stopCode);
 					}
 				});
 				((Button) v.findViewById(R.id.stoppopup_cancel)).setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						d.dismiss();
 					}
 				});
 				d.show();
 				return true;
 			}
 			return false;
 		}
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent e, MapView mapView) {
 			// disable my location if user drags the map
 			if (e.getAction() == MotionEvent.ACTION_MOVE)
 				myLocationOverlay.disableMyLocation();
 				
 			return super.onTouchEvent(e, mapView);
 		}
 	}
 	
 	public static void showActivity(Context context) {
 		Intent i = new Intent(context, StopMapActivity.class);
 		context.startActivity(i);
 	}
 	
 	public static void showActivity(Context context, 
 			double lat,
 			double lng) {
 		Intent i = new Intent(context, StopMapActivity.class);
 		i.putExtra("Lat", lat);
 		i.putExtra("Lng", lng);
 		context.startActivity(i);
 	}
 	
 	public static void showActivityForServiceMap(Context context, 
 			long serviceMap,
 			double lat,
 			double lng) {
 		Intent i = new Intent(context, StopMapActivity.class);
 		i.putExtra("StopFilter", serviceMap);
 		i.putExtra("Lat", lat);
 		i.putExtra("Lng", lng);
 		context.startActivity(i);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) { 
 		super.onCreate(savedInstanceState); 
 		setContentView(R.layout.stop_map);
 
 		mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		mapController = mapView.getController();
 		mapController.setZoom(17);
 
 		// Make map update automatically as user moves around
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 
 		// Check to see if we've been passed data
 		long stopFilter = getIntent().getLongExtra("StopFilter", 0xffffffff);
 		double lat = getIntent().getDoubleExtra("Lat", -1);
 		double lng = getIntent().getDoubleExtra("Lng", -1);
 		
 		// Not been passed a location, so use GPS and default to centre
 		if (lat == -1 && lng == -1) {
 			// Default map to centre of Edinburgh
 			lat = 55.946052;
 			lng = -3.188879;
 			
 			myLocationOverlay.runOnFirstFix(new Runnable() {
 				public void run() {
 					mapController.animateTo(myLocationOverlay.getMyLocation());
 				}
 			});
 			myLocationOverlay.enableMyLocation();
 		} else {
 			myLocationOverlay.disableMyLocation();
 		}
 		
 		stopOverlay = new StopOverlay(mapView,stopFilter);
 		mapView.getOverlays().add(stopOverlay);
 		mapController.setCenter(new GeoPoint((int)(lat*1E6),(int)(lng*1E6)));
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	public void onPause() {
 		myLocationOverlay.disableMyLocation();
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		myLocationOverlay.enableMyLocation();
 		super.onResume();
 		Toast.makeText(this, "Finding your location...", Toast.LENGTH_SHORT).show();
 	}	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.stopmap_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (mapView.isSatellite())
 			menu.findItem(R.id.stopmap_menu_satellite_or_map).setTitle("Map View");
 		else
 			menu.findItem(R.id.stopmap_menu_satellite_or_map).setTitle("Satellite View");		
 		
 		if (stopOverlay.serviceFilter == -1)
 			menu.findItem(R.id.stopmap_menu_showall).setEnabled(false);
 		else
 			menu.findItem(R.id.stopmap_menu_showall).setEnabled(true);
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.stopmap_menu_search: {
 			final EditText input = new EditText(this);
 			new AlertDialog.Builder(this)
 				.setTitle("Enter a location or postcode")
 				.setView(input)
 				.setPositiveButton(android.R.string.ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int whichButton) {
 								displayBusy("Finding location...");
 								StopMapActivity.this.expectedRequestId = GeocodingHelper.geocode(StopMapActivity.this, input.getText().toString(), StopMapActivity.this);
 							}
 						})
 				.setNegativeButton(android.R.string.cancel, null)
 				.show();
 			return true;				
 		}
 
 		case R.id.stopmap_menu_showall:
 			stopOverlay.serviceFilter = -1;
 			StopMapActivity.this.mapView.invalidate();
 			return true;
 
 		case R.id.stopmap_menu_filterservices: {
 			final EditText input = new EditText(this);
 			
 			new AlertDialog.Builder(this)
 				.setTitle("Enter services separated by spaces")
 				.setView(input)
 				.setPositiveButton(android.R.string.ok, 
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							long serviceFilter = 0;
 							PointTree pt = PointTree.getPointTree(StopMapActivity.this);
 							for(String serviceStr: input.getText().toString().split("[ ]+")) {
 								if (pt.serviceNameToServiceBit.containsKey(serviceStr.toUpperCase()))
 									serviceFilter |= 1L << pt.serviceNameToServiceBit.get(serviceStr.toUpperCase()).longValue();
 							}
 							stopOverlay.serviceFilter = serviceFilter;
 							StopMapActivity.this.mapView.invalidate();
 						}
 					})
 				.setNegativeButton(android.R.string.cancel, null)
 				.show();
 			return true;
 		}
 
 		case R.id.stopmap_menu_satellite_or_map:
 			mapView.setSatellite(!mapView.isSatellite());
 			return true;
 			
 		case R.id.stopmap_menu_mylocation:
 			myLocationOverlay.enableMyLocation();
 			if (myLocationOverlay.getMyLocation() != null)
 				mapController.animateTo(myLocationOverlay.getMyLocation());
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	protected void onDestroy() {
 		busyDialog = null;
 		super.onDestroy();
 	}
 
 	private void displayBusy(String reason) {
 		dismissBusy();
 
 		busyDialog = ProgressDialog.show(this, "", reason, true, true, new OnCancelListener() {
 			public void onCancel(DialogInterface dialog) {
 				StopMapActivity.this.expectedRequestId = -1;
 			}
 		});
 	}
 
 	private void dismissBusy() {
 		if (busyDialog != null) {
 			try {
 				busyDialog.dismiss();
 			} catch (Throwable t) {
 			}
 			busyDialog = null;
 		}
 	}
 
 	public void geocodeResponseError(int requestId, String message) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		dismissBusy();
 		
 		new AlertDialog.Builder(this).setTitle("Error").
 			setMessage("Unable to find location: " + message).
 			setPositiveButton(android.R.string.ok, null).
 			show();
 	}
 
 	public void geocodeResponseSucccess(int requestId, List<Address> addresses_) {
 		if (requestId != expectedRequestId)
 			return;
 		
 		dismissBusy();
		if (addresses_.size() == 1) {
			Address address = addresses_.get(0);
			GeoPoint gp = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
			mapController.animateTo(gp);			
			return;
		}
 		
 		final List<Address> addresses = addresses_;
 		ArrayList<String> addressNames = new ArrayList<String>();
 		for(Address a: addresses) {
 			StringBuilder strb = new StringBuilder();
 			for(int i =0; i< a.getMaxAddressLineIndex(); i++) {
 				if (i > 0)
 					strb.append(", ");
 				strb.append(a.getAddressLine(i));
 			}
 			addressNames.add(strb.toString());
 		}
 
 		new AlertDialog.Builder(this)
   	       .setSingleChoiceItems(addressNames.toArray(new String[addressNames.size()]), -1, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					if (which < 0)
 						return;
 					
 					Address address = addresses.get(which);
 					GeoPoint gp = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
 					mapController.animateTo(gp);
 					dialog.dismiss();
 				}
   	       })
   	       .show();
 	}
 }
