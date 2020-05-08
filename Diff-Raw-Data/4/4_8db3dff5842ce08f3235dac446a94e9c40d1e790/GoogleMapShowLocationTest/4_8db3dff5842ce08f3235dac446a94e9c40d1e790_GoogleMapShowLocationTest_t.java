 /*
  * Copyright [2012] []
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
  */
 
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
 	private static final int RESTAURANTBUTTON = 2;
 	private static final int ATMBUTTON = 3;
	private static final int SIZEOFMICROWAVETABLE = 9;
	private static final int SIZEOFRESTAURANTTABLE = 34;
 	private static final int SIZEOFATMTABLE = 4;
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
 		setActivityInitialTouchMode(false);
 	}
 	
 	/**
 	 * Test to see if all the locations for the microwaves were drawn out, by comparing
 	 * the size of all the items drawn out to the size of the data table. The other tests
 	 * works just like this except for different tables.
 	 * Reason to why the mapView and overlay variables are in all the tests and not in the
  	 * setUp is because the intent needs to be customized for every different test to be
 	 * customized for every different case to get coverage of all the code.
 	 */
 	public void testDrawLocationsMicrowaves(){
 		setActivityIntent(new Intent("android.intent.action.CAMPUSMENUACTIVITY").putExtra("Show locations", MICROWAVEBUTTON).putExtra("Campus", JOHANNESBERG));
 		this.showLocationActivity = super.getActivity();
 		this.mapView = (MapView) this.showLocationActivity.findViewById(R.id.mapview);
 		overlay = mapView.getOverlays();
 		overlay.clear();
 		this.showLocationActivity.drawLocationList("Microwaves");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFMICROWAVETABLE,overlay.size());
 	}
 	/**
 	 * See the test comments above.
 	 */
 
 	public void testDrawLocationsRestaurants(){
 		setActivityIntent(new Intent("android.intent.action.CAMPUSMENUACTIVITY").putExtra("Show locations", RESTAURANTBUTTON).putExtra("Campus", JOHANNESBERG));
 		this.showLocationActivity = super.getActivity();
 		this.mapView = (MapView) this.showLocationActivity.findViewById(R.id.mapview);
 		overlay = mapView.getOverlays();
 		overlay.clear();
 		this.showLocationActivity.drawLocationList("Restaurants");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFRESTAURANTTABLE,overlay.size());
 	}
 	/**
 	 * See the test comments above.
 	 */
 
 	public void testDrawLocationsAtms(){
 		setActivityIntent(new Intent("android.intent.action.CAMPUSMENUACTIVITY").putExtra("Show locations", ATMBUTTON).putExtra("Campus", JOHANNESBERG));
 		this.showLocationActivity = super.getActivity();
 		this.mapView = (MapView) this.showLocationActivity.findViewById(R.id.mapview);
 		overlay = mapView.getOverlays();
 		overlay.clear();
 		this.showLocationActivity.drawLocationList("Atm");
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals(SIZEOFATMTABLE,overlay.size());
 	}
 	/**
 	 * See the test comments above.
 	 */
 //	Very long test, not used right now.
 //	public void testDrawLocationsRooms(){
 //		this.showLocationActivity.drawLocationList("Rooms");
 //		super.getInstrumentation().waitForIdleSync();
 //		assertEquals(SIZEOFROOMSTABLE, overlay.size());
 //	}
 	
 	@Override
 	public void tearDown() throws Exception{
 		super.tearDown();
 	}
 
 
 }
