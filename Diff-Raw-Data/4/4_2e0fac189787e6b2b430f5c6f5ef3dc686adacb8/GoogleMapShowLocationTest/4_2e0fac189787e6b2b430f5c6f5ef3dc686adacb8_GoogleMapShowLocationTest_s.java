 package com.chalmers.schmaps.test;
 
 import java.util.List;
 
 import com.chalmers.schmaps.GoogleMapShowLocation;
 import com.chalmers.schmaps.R;
 import com.google.android.maps.*;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 
 /**
  * Test class for the GoogleMapShowLocation activity, mainly for its drawLocationList
  * method since the method is central for the whole class.
  * @author Froll
  *
  */
 public class GoogleMapShowLocationTest extends
 		ActivityInstrumentationTestCase2<GoogleMapShowLocation> {
 
 	private GoogleMapShowLocation showLocationActivity;
 	private MapView mapView;
 	private List<Overlay> overlay;
 	private static final int JOHANNESBERG = 40;
 	private static final int MICROWAVEBUTTON = 1;
 	private static final int SIZEOFMICROWAVETABLE = 4;
	private static final int SIZEOFRESTAURANTTABLE = 27;
	private static final int SIZEOFATMTABLE = 7;
 	private static final int SIZEOFROOMSTABLE = 263;
 	
 	
 	public GoogleMapShowLocationTest() {
 		super(GoogleMapShowLocation.class);
 	}
 	/**
 	 * Setup method for the various variables.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		//Simulate the intent from the previous activities which is usually rendered from the user's inputs in the app.
 		setActivityIntent(new Intent("android.intent.action.CAMPUSMENUACTIVITY").putExtra("Show locations", MICROWAVEBUTTON).putExtra("Campus", JOHANNESBERG));
 		setActivityInitialTouchMode(false);
 		this.showLocationActivity = super.getActivity();
 		this.mapView = (MapView) this.showLocationActivity.findViewById(R.id.mapview);
 		overlay = mapView.getOverlays();
 	}
 	
 	public void testPreConditions(){
 		super.assertNotNull(showLocationActivity);
 		super.assertNotNull(mapView);
 		super.assertNotNull(overlay);
 	}
 
 	/**
 	 * Test to see if all the locations for the microwaves were drawn out, by comparing
 	 * the size of all the items drawn out to the size of the data table. The other tests
 	 * works just like this except for different tables.
 	 */
 	public void testDrawLocationsMicrowaves(){
 		this.showLocationActivity.drawLocationList("Microwaves");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFMICROWAVETABLE,overlay.size());
 	}
 	/**
 	 * See the test comments above.
 	 */
 
 	public void testDrawLocationsRestaurants(){
 		this.showLocationActivity.drawLocationList("Restaurants");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFRESTAURANTTABLE,overlay.size());
 	}
 	/**
 	 * See the test comments above.
 	 */
 
 	public void testDrawLocationsAtms(){
 		this.showLocationActivity.drawLocationList("Atm");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFATMTABLE,overlay.size());
 	}
 	/**
 	 * See the test comments above.
 	 */
 
 	public void testDrawLocationsRooms(){
 		this.showLocationActivity.drawLocationList("Rooms");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFROOMSTABLE, overlay.size());
 	}
 	
 	@Override
 	public void tearDown() throws Exception{
 		super.tearDown();
 	}
 
 
 }
