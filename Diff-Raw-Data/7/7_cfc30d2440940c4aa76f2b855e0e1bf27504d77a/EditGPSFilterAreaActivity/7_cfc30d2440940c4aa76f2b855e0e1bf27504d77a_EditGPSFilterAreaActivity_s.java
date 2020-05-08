 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.activity;
 
 import java.util.List;
 
 import net.engio.mbassy.listener.Handler;
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.android.location.LocationAdapter;
 import se.chalmers.dat255.sleepfighter.gps.GPSFilterLocationRetriever;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterArea;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterAreaSet;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterMode;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterPolygon;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSLatLng;
 import se.chalmers.dat255.sleepfighter.utils.GPSFilterTextUtils;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.location.Criteria;
 import android.location.Location;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.text.Html;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polygon;
 import com.google.android.gms.maps.model.PolygonOptions;
 import com.google.common.collect.Lists;
 
 /**
  * EditGPSFilterAreaActivity is the activity for editing an GPSFilterArea.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 6, 2013
  */
 public class EditGPSFilterAreaActivity extends FragmentActivity implements OnMapClickListener, OnMarkerDragListener, OnMarkerClickListener {
 	private static final String TAG = EditGPSFilterAreaActivity.class.getSimpleName();
 
 	public static final String EXTRAS_AREA_ID = "gpsfilter_area_id";
 	public static final String EXTRAS_AREA_IS_NEW = "gpsfilter_are_isnew";
 
 	private static final int POLYGON_EXCLUDE_FILL_COLOR = R.color.gpsfilter_polygon_fill_exclude;
 	private static final int POLYGON_INCLUDE_FILL_COLOR = R.color.gpsfilter_polygon_fill_include;
 	private static final int POLYGON_STROKE_COLOR = R.color.shadow;
 	private static final boolean POLYGON_GEODESIC = true; // The earth is a sphere, so bend polygon?
 
 	private static final float MAP_MY_LOCATION_ZOOM = 13f;
 	private static final float CAMERA_MOVE_BOUNDS_PADDING = 0.2f; // padding as a % of lowest of screen width/height.
 
 	private static final long MAX_LOCATION_FIX_AGE = 5 * 60 * 1000; // 5 minutes.
 
 	private static final long SPLASH_FADE_DELAY = 150;
 
 	private GoogleMap googleMap;
 
 	private List<Marker> markers;
 
 	private Polygon poly;
 
 	private LinearLayout splashInfoContainer;
 
 	private Animation splashFadeOut;
 
 	private GPSFilterArea area;
 
 	private GPSFilterAreaSet set;
 
 	private boolean isNew;
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		String name = this.printName();
 
 		if ( Build.VERSION.SDK_INT >= 11 ) {
 			// add the custom view to the action bar.
 			ActionBar actionBar = getActionBar();
 			actionBar.setCustomView( R.layout.gpsfilter_area_actionbar );
 			actionBar.setDisplayOptions( ActionBar.DISPLAY_SHOW_HOME
 					| ActionBar.DISPLAY_HOME_AS_UP
 					| ActionBar.DISPLAY_SHOW_CUSTOM );
 
 			View customView = actionBar.getCustomView();
 
 			// Setup edit name component.
 			EditText editNameView = (EditText) customView.findViewById( R.id.edit_gpsfilter_area_title_edit );
 			editNameView.setText( name );
 			editNameView.setOnEditorActionListener( new OnEditorActionListener() {
 				@Override
 				public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {
 					area.setName( GPSFilterTextUtils.printName( getResources(), v.getText().toString() ) );
 					InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
 					imm.hideSoftInputFromWindow( v.getWindowToken(), 0 );
 					return false;
 				}
 			} );
 			editNameView.clearFocus();
 
 			// Setup enabled switch.
 			CompoundButton activatedSwitch = (CompoundButton) customView.findViewById( R.id.edit_gpsfilter_area_toggle );
 			activatedSwitch.setChecked( this.area.isEnabled() );
 			activatedSwitch.setOnCheckedChangeListener( new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
 					area.setEnabled( isChecked );
 				}
 			} );
 		} else {
 			this.setTitle( name );
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.edit_gpsfilter_area, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item ) {
 		switch ( item.getItemId() ) {
 		case android.R.id.home:
 			finish();
 			return true;
 			
 		case R.id.action_edit_gpsfilter_area_help:
 			this.showSplash();
 			return true;
 
 		case R.id.action_edit_gpsfilter_area_rename:
 			this.showRenameDialog();
 			return true;
 
 		case R.id.action_edit_gpsfilter_area_clear:
 			this.clearPoints();
 			return true;
 
 		case R.id.action_edit_gpsfilter_area_undo:
 			this.undoLastPoint();
 			return true;
 
 		case R.id.action_edit_gpsfilter_area_remove:
 			this.removeArea();
 			return true;
 
 		case R.id.action_edit_gpsfilter_area_zoom:
 			this.moveCameraToPolygon( true );
 			return true;
 
 		case R.id.action_gpsfilter_settings:
 			this.gotoSettings();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected( item );
 		}
 	}
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState ) {
 		super.onCreate( savedInstanceState );
 
 		this.setContentView( R.layout.activity_edit_gpsfilter_area );
 
 		this.setupMap();
 
 		this.fetchArea();
 
 		if ( this.isConsideredFresh() ) {
 			this.moveToCurrentLocation();
 		}
 
 		this.setupActionBar();
 
 		this.setupMarkers();
 
 		this.setupSplash();
 
 		this.setupModeSpinner();
 
 		this.setupBottomUndo();
 
 		this.area.getMessageBus().subscribe( this );
 	}
 
 	/**
 	 * Moves the user to global options > location filter.
 	 */
 	private void gotoSettings() {
 		Intent i = new Intent( this, GlobalSettingsActivity.class );
 		this.startActivity( i );
 	}
 
 	/**
 	 * Moves the user to its current location.
 	 */
 	private void moveToCurrentLocation() {
 		GPSFilterLocationRetriever retriever = new GPSFilterLocationRetriever( new Criteria() );
 		Location loc = retriever.getLocation( this );
 
 		if ( loc == null ) {
 			// User turned off GPS, send it to device location settings.
 			Intent i = new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS );
 			this.startActivity( i );
 		} else {
 			// First move to the last known location..
 			this.moveCameraToLocation( loc, false );
 
 			// Check if location fix is out dated, if it is, request a new location, ONCE.
 			long elapsedTime = System.currentTimeMillis() - loc.getTime();
 			if ( elapsedTime > MAX_LOCATION_FIX_AGE ) {
 				// Therefore, we request a single fix.
 				retriever.requestSingleUpdate( this, new LocationAdapter() {
 					@Override
 					public void onLocationChanged( Location loc ) {
 						moveCameraToLocation( loc, true );
 					}
 				} );
 			}
 		}
 	}
 
 	/**
 	 * Prints the name of area to a human readable form.
 	 *
 	 * @return the area.
 	 */
 	private String printName() {
 		return GPSFilterTextUtils.printName( this.getResources(), this.area.getName() );
 	}
 
 	/**
 	 * Handles a change in area via event.
 	 *
 	 * @param evt the change event.
 	 */
 	@TargetApi( Build.VERSION_CODES.HONEYCOMB )
 	@Handler
 	public void handleAreaChange( GPSFilterArea.ChangeEvent evt ) {
 		switch ( evt.getModifiedField() ) {
 		case ENABLED:
 			if ( Build.VERSION.SDK_INT >= 11 ) {
 				CompoundButton activatedSwitch = (CompoundButton) this.getActionBar().getCustomView().findViewById( R.id.edit_gpsfilter_area_toggle );
 				activatedSwitch.setChecked( evt.getArea().isEnabled() );
 			}
 			break;
 
 		case NAME:
 			String name =  this.printName();
 			if ( Build.VERSION.SDK_INT >= 11 ) {
 				((EditText) this.getActionBar().getCustomView().findViewById( R.id.edit_gpsfilter_area_title_edit )).setText( name );
 			} else {
 				this.setTitle( name );
 			}
 			break;
 
 		case MODE:
 			break;
 
 		case POLYGON:
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Sets up the "action-bar" at the bottom undo button.
 	 */
 	private void setupBottomUndo() {
 		this.findViewById( R.id.action_edit_gpsfilter_area_undo_button ).setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick( View v ) {
 				undoLastPoint();
 			}
 		} );
 	}
 
 	/**
 	 * Sets up the markers if available.
 	 */
 	private void setupMarkers() {
 		GPSFilterPolygon polygon = this.area.getPolygon();
 
 		if ( polygon != null ) {
 			final LatLngBounds.Builder builder = LatLngBounds.builder();
 
 			// Put markers for each edge.
 			for ( GPSLatLng pos : polygon.getPoints() ) {
 				LatLng point = this.convertLatLng( pos );
 
 				builder.include( point );
 
 				this.addMarker( point );
 			}
 
 			// Add listener that moves camera so that all markers are in users view.
 			this.googleMap.setOnCameraChangeListener( new OnCameraChangeListener() {
 				@Override
 				public void onCameraChange( CameraPosition arg0 ) {
 					// Move camera.
 					moveCameraToPolygon( builder, false );
 
 					// Remove listener to prevent position reset on camera move.
 					googleMap.setOnCameraChangeListener( null );
 				}
 			} );
 
 			this.updateGuiPolygon();
 		}
 	}
 
 	/**
 	 * Converts from model {@link GPSLatLng} to {@link LatLng}
 	 *
 	 * @param pos the model object.
 	 * @return google:s version.
 	 */
 	private LatLng convertLatLng( GPSLatLng pos ) {
 		return new LatLng( pos.getLat(), pos.getLng() );
 	}
 
 	/**
 	 * Moves the camera to a given a Location.
 	 *
 	 * @param loc the Location object to use for location.
 	 * @param animate whether or not to animate the movement.
 	 */
 	private void moveCameraToLocation( Location loc, boolean animate ) {
 		LatLng pos = new LatLng( loc.getLatitude(), loc.getLongitude() );
 		CameraUpdate update = CameraUpdateFactory.newLatLngZoom( pos, MAP_MY_LOCATION_ZOOM );
 		this.cameraAnimateOrMove( update, animate );
 	}
 
 	/**
 	 * Moves and zooms the camera so that all the markers are visible in users view.
 	 *
 	 * @param builder the builder containing to use for making bounds.
 	 */
 	private void moveCameraToPolygon( boolean animate ) {
 		if ( this.markers.isEmpty() ) {
 			return;
 		}
 
 		final LatLngBounds.Builder builder = LatLngBounds.builder();
 		for ( GPSLatLng pos : this.area.getPolygon().getPoints() ) {
 			builder.include( this.convertLatLng( pos ) );
 		}
 
 		this.moveCameraToPolygon( builder, animate );
 	}
 
 	/**
 	 * Moves and zooms the camera so that all the markers are visible in users view.
 	 *
 	 * @param builder the builder containing to use for making bounds.
 	 */
 	private void moveCameraToPolygon( LatLngBounds.Builder builder, boolean animate ) {
 		CameraUpdate update = CameraUpdateFactory.newLatLngBounds( builder.build(), this.computeMoveCameraPadding() );
 		this.cameraAnimateOrMove( update, animate );
 	}
 
 	/**
 	 * Computes the padding to use when moving camera to polygon.
 	 *
 	 * @return the padding.
 	 */
 	private int computeMoveCameraPadding() {
 		Point dim = this.getScreenDim();
 		int min = Math.min( dim.x, dim.y );
 		return (int) (CAMERA_MOVE_BOUNDS_PADDING * min);
 	}
 
 	/**
 	 * Returns the screen dimensions as a Point.
 	 *
 	 * @return the screen dimensions.
 	 */
 	private Point getScreenDim() {
 		Display display = this.getWindowManager().getDefaultDisplay();
 
 		@SuppressWarnings( "deprecation" )
 		Point size = new Point( display.getWidth(), display.getHeight() );
 		return size;
 	}
 
 	/**
 	 * Moves or animates the camera with update.
 	 *
 	 * @param update the camera update to perform.
 	 * @param animate whether or not to animate, otherwise it instantaneously moves.
 	 */
 	private void cameraAnimateOrMove( CameraUpdate update, boolean animate ) {
 		if ( animate ) {
 			this.googleMap.animateCamera( update );
 		} else {
 			this.googleMap.moveCamera( update );
 		}
 	}
 
 	/**
 	 * Fetches the area from set.
 	 */
 	private void fetchArea() {
 		// Fetch the isNew.
 		Intent i = this.getIntent();
 		this.isNew = i.getBooleanExtra( EXTRAS_AREA_IS_NEW, true );
 
 		// Fetch ID.
 		int id = i.getIntExtra( EXTRAS_AREA_ID, -1 );
 		if ( id == -1 ) {
 			Toast.makeText( this, "No area ID provided, finishing!", Toast.LENGTH_LONG ).show();
 			this.finish();
 		}
 
 		// Get the set from application.
 		this.set = SFApplication.get().getGPSSet();
 
 		// For some weird reason, NullPointerException will happen if we don't do this.
 		this.set.setMessageBus( SFApplication.get().getBus() );
 
 		// Find area in set.
 		this.area = this.set.getById( id );
 
 		Log.d( TAG, this.area.toString() );
 
		Toast.makeText( this, "The area ID provided did not exist in set.", Toast.LENGTH_LONG ).show();
		this.finish();
 	}
 
 	/**
 	 * Removes the current area, finishes activity.
 	 */
 	private void removeArea() {
 		this.set.remove( this.area );
 		this.finish();
 	}
 
 	/**
 	 * Sets up the spinner for modes.
 	 */
 	private void setupModeSpinner() {
 		Spinner spinner = (Spinner) this.findViewById( R.id.action_edit_gpsfilter_area_mode );
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( this, R.array.action_edit_gpsfilter_area_mode, android.R.layout.simple_spinner_item );
 		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
 		spinner.setAdapter( adapter );
 
 		spinner.setSelection( this.area.getMode() == GPSFilterMode.INCLUDE ? 0 : 1 );
 
 		spinner.setOnItemSelectedListener( new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
 				updateMode( position );
 			}
 
 			@Override
 			public void onNothingSelected( AdapterView<?> parent ) {
 			}
 		} );
 	}
 
 	/**
 	 * Updates the mode from slider position.
 	 *
 	 * @param position the position in array, 0 == Include, 1 == exclude.
 	 */
 	protected void updateMode( int position ) {
 		GPSFilterMode mode = position == 0 ? GPSFilterMode.INCLUDE : GPSFilterMode.EXCLUDE;
 		this.area.setMode( mode );
 		this.updateGuiPolygon();
 	}
 
 	/**
 	 * Shows a rename dialog.
 	 */
 	private void showRenameDialog() {
 		AlertDialog.Builder alert = new AlertDialog.Builder( this );
 
 		alert.setTitle( this.getString( R.string.action_edit_gpsfilter_area_rename ) );
 
 		final EditText input = new EditText( this );
 		input.setText( this.area.getName() );
 		alert.setView( input );
 
 		alert.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
 			public void onClick( DialogInterface dialog, int whichButton ) {
 				area.setName( input.getText().toString() );
 			}
 		} );
 
 		alert.setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
 			public void onClick( DialogInterface dialog, int whichButton ) {
 			}
 		} );
 
 		alert.show();
 	}
 
 	/**
 	 * Sets up the help splash.
 	 */
 	private void setupSplash() {
 		this.splashInfoContainer = (LinearLayout) this.findViewById( R.id.edit_gpsfilter_area_splash );
 
 		TextView textView = (TextView) this.splashInfoContainer.findViewById( R.id.edit_gpsfilter_area_splash_text );
 		textView.setText( Html.fromHtml( this.getString( R.string.edit_gpsfilter_area_splash_text ) ) );
 
 		// Define fade out animation.
 		this.splashFadeOut = new AlphaAnimation( 1.00f, 0.00f );
 		this.splashFadeOut.setDuration( SPLASH_FADE_DELAY );
 		this.splashFadeOut.setAnimationListener( new AnimationListener() {
 			public void onAnimationStart( Animation animation ) {
 			}
 
 			public void onAnimationRepeat( Animation animation ) {
 			}
 
 			public void onAnimationEnd( Animation animation ) {
 				splashInfoContainer.setVisibility( View.GONE );
 			}
 		} );
 
 		// Don't show if not new and we got polygon.
 		if ( !this.isConsideredFresh() ) {
 			this.splashInfoContainer.setVisibility( View.GONE );
 		}
 	}
 
 	/**
 	 * Returns whether or not this activity instance is considered<br/>
 	 * fresh. It is fresh when the area is either new, or the polygon is undefined.
 	 *
 	 * @return true if new & undefined polygon.
 	 */
 	private boolean isConsideredFresh() {
 		return this.isNew || this.area.getPolygon() == null;
 	}
 
 	/**
 	 * Shows the help splash.
 	 */
 	private void showSplash() {
 		this.splashInfoContainer.setVisibility( View.VISIBLE );
 	}
 
 	/**
 	 * Hides the help splash.
 	 */
 	private void hideSplash() {
 		this.splashInfoContainer.startAnimation( this.splashFadeOut );
 	}
 
 	/**
 	 * Adds a point to polygon at point.<br/>
 	 * Also adds a marker to GUI.
 	 *
 	 * @param point the point to add to polygon and where to place marker.
 	 */
 	private void addPoint( LatLng point ) {
 		this.addMarker( point );
 		this.commitPolygon();
 	}
 
 	/**
 	 * Adds a marker on map at point.
 	 *
 	 * @param point the point to add maker at.
 	 */
 	private void addMarker( LatLng point ) {
 		this.markers.add( this.googleMap.addMarker( this.makeMarkerAt( point ) ) );
 	}
 
 	/**
 	 * Makes a marker at point.
 	 *
 	 * @param point the point in LatLng.
 	 * @return the marker.
 	 */
 	private MarkerOptions makeMarkerAt( LatLng point ) {
 		return new MarkerOptions().flat( true ).draggable( true ).position( point );
 	}
 
 	/**
 	 * Removes the last added marker/point.
 	 */
 	private void undoLastPoint() {
 		if ( this.markers.isEmpty() ) {
 			return;
 		}
 
 		this.markers.remove( this.markers.size() - 1 ).remove();
 		this.commitPolygon();
 
 		if ( this.markers.size() > 1 ) {
 			this.moveCameraToPolygon( true );
 		}
 
 		this.disableIfDissatisfied();
 	}
 
 	/**
 	 * Whether or not the current markers satisfy being a polygon (having 3 edges / markers).
 	 *
 	 * @return true if it satisfies being a polygon.
 	 */
 	private boolean satisfiesPolygon() {
 		return this.markers.size() >= 3;
 	}
 
 	/**
 	 * Clears all markers/points.
 	 */
 	private void clearPoints() {
 		for ( Marker marker : this.markers ) {
 			marker.remove();
 		}
 
 		this.markers.clear();
 		this.commitPolygon();
 		this.disableIfDissatisfied();
 	}
 
 	/**
 	 * Disables the area when/if dissatisfied.
 	 */
 	private void disableIfDissatisfied() {
 		if ( !this.satisfiesPolygon() ) {
 			this.area.setEnabled( false );
 		}
 	}
 
 	/**
 	 * Commits the polygon saving it while also performing a GUI update.<br/>
 	 * If the polygon doesn't honor {@link #satisfiesPolygon()}, the area is disabled.
 	 */
 	private void commitPolygon() {
 		this.updateGuiPolygon();
 		this.area.setPolygon( this.toFilterPolygon() );
 	}
 
 	/**
 	 * Converts the list of markers to a {@link GPSFilterPolygon}.
 	 *
 	 * @return the filter polygon object.
 	 */
 	private GPSFilterPolygon toFilterPolygon() {
 		if ( this.markers.isEmpty() ) {
 			return null;
 		} else {
 			List<GPSLatLng> points = Lists.newArrayListWithCapacity( this.markers.size() );
 			for ( Marker marker : this.markers ) {
 				LatLng pos = marker.getPosition();
 				points.add( new GPSLatLng( pos.latitude, pos.longitude ) );
 			}
 
 			return new GPSFilterPolygon( points );
 		}
 	}
 
 	/**
 	 * Updates the GUI polygon, removing the old one,<br/>
 	 * adding a new one if we have >= 3 points.
 	 */
 	private void updateGuiPolygon() {
 		if ( this.poly != null ) {
 			this.poly.remove();
 		}
 
 		// A polygon is at minimum a triangle.
 		if ( !this.satisfiesPolygon() ) {
 			return;
 		}
 
 		PolygonOptions options = new PolygonOptions();
 		options
 			.geodesic( POLYGON_GEODESIC )
 			.strokeWidth( 2 )
 			.fillColor( this.getFillColor() )
 			.strokeColor( this.getResources().getColor( POLYGON_STROKE_COLOR ) );
 
 		for ( Marker marker : this.markers ) {
 			options.add( marker.getPosition() );
 		}
 
 		this.poly = this.googleMap.addPolygon( options );
 	}
 
 	/**
 	 * Returns the fill-color to use.
 	 *
 	 * @return the color value.
 	 */
 	private int getFillColor() {
 		int id = this.area.getMode() == GPSFilterMode.INCLUDE ? POLYGON_INCLUDE_FILL_COLOR : POLYGON_EXCLUDE_FILL_COLOR;
 		return adjustAlpha( this.getResources().getColor( id ), 255 * 0.5f );
 	}
 
 	/**
 	 * Sets up the map.
 	 */
 	private void setupMap() {
         int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 		if ( status != ConnectionResult.SUCCESS ) {
 			// Google Play Services are not available.
 			int requestCode = 10;
 			GooglePlayServicesUtil.getErrorDialog( status, this, requestCode ).show();
 		} else {
 			// Google Play Services are available.
 			if ( this.googleMap == null ) {
 				FragmentManager fragManager = this.getSupportFragmentManager();
 				SupportMapFragment mapFrag = (SupportMapFragment) fragManager.findFragmentById( R.id.edit_gpsfilter_area_google_map );
 				this.googleMap = mapFrag.getMap();
 
 				if ( this.googleMap != null ) {
 					// The Map is verified. It is now safe to manipulate the map.
 					this.configMap();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Configures the map.
 	 */
 	private void configMap() {
 		// Enable all gestures.
 		this.googleMap.getUiSettings().setAllGesturesEnabled( true );
 
 		// Add button for my location.
 		this.googleMap.setMyLocationEnabled( true );
 
 		// Bind events for markers.
 		this.markers = Lists.newArrayList();
 		this.googleMap.setOnMapClickListener( this );
 		this.googleMap.setOnMarkerDragListener( this );
 		this.googleMap.setOnMarkerClickListener( this );
 	}
 
 	@Override
 	public void onMapClick( LatLng point ) {
 		if ( this.splashInfoContainer.getVisibility() == View.VISIBLE ) {
 			this.hideSplash();
 		} else {
 			this.addPoint( point );
 		}
 	}
 
 	@Override
 	public void onMarkerDrag( Marker marker ) {
 		this.updateGuiPolygon();
 	}
 
 	@Override
 	public void onMarkerDragEnd( Marker marker ) {
 		this.commitPolygon();
 	}
 
 	@Override
 	public void onMarkerDragStart( Marker marker ) {
 	}
 
 	@Override
 	public boolean onMarkerClick( final Marker marker ) {
 		return false;
 	}
 
 	/**
 	 * Alpha-adjusts a given color setting the alpha-component to alphaArg, rounded.
 	 *
 	 * @param color the color.
 	 * @param alphaArg the alpha value to use.
 	 * @return the color.
 	 */
 	private int adjustAlpha( int color, float alphaArg ) {
 		int alpha = Math.round( alphaArg );
 		int red = Color.red( color );
 		int green = Color.green( color );
 		int blue = Color.blue( color );
 		return Color.argb( alpha, red, green, blue );
 	}
 }
