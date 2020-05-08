 /**
  *	Copyright 2010-2012 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android.ui;
 
 import java.util.List;
 
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.text.format.DateUtils;
 import android.view.MotionEvent;
 import be.norio.twunch.android.BuildProperties;
 import be.norio.twunch.android.R;
 import be.norio.twunch.android.provider.TwunchContract.Twunches;
 import be.norio.twunch.android.util.TwunchItemizedOverlay;
 import be.norio.twunch.android.util.TwunchOverlayItem;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.readystatesoftware.maps.OnSingleTapListener;
 import com.readystatesoftware.maps.TapControlledMapView;
 
 public class TwunchesMapActivity extends MapActivity {
 
 	Cursor mCursor;
 
 	TapControlledMapView mMapView;
 	TwunchItemizedOverlay mItemizedOverlay;
 	MyLocationOverlay mMyLocationOverlay;
 
 	private Drawable mDrawable;
 
 	private ContentObserver mObserver;
 
 	private interface TwunchesQuery {
 		int _TOKEN = 0x1;
 
 		String[] PROJECTION = { BaseColumns._ID, Twunches.LATITUDE, Twunches.LONGITUDE, Twunches.TITLE, Twunches.DATE };
 
 		int _ID = 0;
 		int LATITUDE = 1;
 		int LONGITUDE = 2;
 		int TITLE = 3;
 		int DATE = 4;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mMapView = new TapControlledMapView(this, BuildProperties.MAPS_KEY);
 		mMapView.setClickable(true);
 		mMapView.setBuiltInZoomControls(true);
 		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
 			@Override
 			public boolean onSingleTap(MotionEvent e) {
 				mItemizedOverlay.hideAllBalloons();
 				return true;
 			}
 		});
 		setContentView(mMapView);
 
 		mDrawable = getResources().getDrawable(R.drawable.marker);
 		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 
 		mObserver = new ContentObserver(new Handler()) {
 			public void onChange(boolean selfChange) {
 				mCursor.requery();
 				showOverlays();
 			};
 		};
 		mCursor = getContentResolver().query(Twunches.buildFutureTwunchesUri(), TwunchesQuery.PROJECTION, null, null, null);
 		startManagingCursor(mCursor);
 		showOverlays();
 	}
 
 	protected void showOverlays() {
 		List<Overlay> mapOverlays = mMapView.getOverlays();
 		mapOverlays.clear();
 		mItemizedOverlay = new TwunchItemizedOverlay(mMapView, mDrawable, this);
 		mItemizedOverlay.setShowClose(false);
 		mItemizedOverlay.setShowDisclosure(true);
 		while (mCursor.moveToNext()) {
 			if (mCursor.getFloat(TwunchesQuery.LATITUDE) != 0 && mCursor.getFloat(TwunchesQuery.LONGITUDE) != 0) {
				GeoPoint point = new GeoPoint(Double.valueOf(mCursor.getFloat(TwunchesQuery.LATITUDE) * 1E6).intValue(), Double
						.valueOf(mCursor.getFloat(TwunchesQuery.LONGITUDE) * 1E6).intValue());
 				TwunchOverlayItem overlayitem = new TwunchOverlayItem(point, mCursor.getString(TwunchesQuery.TITLE), String.format(
 						getString(R.string.date),
 						DateUtils.formatDateTime(this, mCursor.getLong(TwunchesQuery.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
 								| DateUtils.FORMAT_SHOW_DATE),
 						DateUtils.formatDateTime(this, mCursor.getLong(TwunchesQuery.DATE), DateUtils.FORMAT_SHOW_TIME)),
 						Twunches.buildTwunchUri(Integer.toString(mCursor.getInt(TwunchesQuery._ID))));
 				mItemizedOverlay.addOverlay(overlayitem);
 			}
 		}
 		mapOverlays.add(mItemizedOverlay);
 		mapOverlays.add(mMyLocationOverlay);
 		mMapView.getController().zoomToSpan(mItemizedOverlay.getLatSpanE6(), mItemizedOverlay.getLonSpanE6());
 		mMapView.getController().animateTo(mItemizedOverlay.getCenter());
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mMyLocationOverlay.disableMyLocation();
 		getContentResolver().unregisterContentObserver(mObserver);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mMyLocationOverlay.enableMyLocation();
 		getContentResolver().registerContentObserver(Twunches.buildFutureTwunchesUri(), true, mObserver);
 	}
 
 }
