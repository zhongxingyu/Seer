 /* 
 This file is part of OpenSatNav.
 
     OpenSatNav is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     OpenSatNav is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with OpenSatNav.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cl.droid.transantiago;
 
 public class TransChooseLocationServiceActivity extends ListActivity {
 
 	protected Bundle locations;
 	protected GeoPoint from;
 	protected String to;
 	protected int selectedPoi = -1;
 	protected ProgressDialog progress;
 	@Override
 	public void onCreate(android.os.Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		from = GeoPoint.fromDoubleString(getIntent().getStringExtra("fromLocation"), ',');
 		setTitle(this.getResources().getText(R.string.choose_location));
 		
 		final LocationAdapter la = new LocationAdapter(from);
 		setListAdapter(la);
 		getListView().setTextFilterEnabled(true);
 		getListView().setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, final long position) {
 				progress = ProgressDialog.show(
 						TransChooseLocationServiceActivity.this, TransChooseLocationServiceActivity.this.getResources().getText(
 								R.string.please_wait), TransChooseLocationServiceActivity.this.getResources().getText(
 								R.string.searching), true, true);
 				final String paradero = la.getParadero((int)position);
 				final Handler handler = new Handler() {
 					@Override
 					public void handleMessage(Message msg) {
 						if (progress.isShowing())
 							try {
 								progress.dismiss();
 //								backgroundThreadComplete = true;
 							} catch (IllegalArgumentException e) {
 								// if orientation change, thread continue but the dialog cannot be dismissed without exception
 							}
 						if (locations != null) {
 							Intent intent = new Intent(TransChooseLocationServiceActivity.this,
 //									org.opensatnav.android.ServiceActivity.class);
 									cl.droid.transantiago.TransChooseServiceActivity.class);
 							intent.putExtra("fromLocation", from.toDoubleString());
 							intent.putExtra("locations", locations);
 							intent.putExtra("paradero", la.getParadero((int)position));
 							
 							String urlstring = "http://m.ibus.cl/index.jsp?paradero="+paradero+"&servicio=&boton.x=0&boton.y=0";
 							Log.i(OpenSatNavConstants.LOG_TAG, urlstring);
 							intent.putExtra("url", urlstring);
 							startActivityForResult(intent,0);
 							
 						}
 					}
 				};
 				new Thread(new Runnable() {
 					public void run() {
 						// put long running operations here
 						TransantiagoGeoCoder geoCoder = null;
 
 						
 						geoCoder = new TransantiagoGeoCoder();
 							
 						
 						if (selectedPoi == -1) { // text search, rank results within an area
 							locations = geoCoder.queryService(la.getParadero((int)position), from, GeoCoder.IN_AREA, 25,
 									TransChooseLocationServiceActivity.this);
 						}
 						else {  //POI search, just find the nearest matching POI
 						locations = geoCoder.queryService("", from, GeoCoder.FROM_POINT, 25,
 								TransChooseLocationServiceActivity.this);
 						}
 						// ok, we are done
 						handler.sendEmptyMessage(0);
 						
 					}
 				}).start();
 				to = la.getLocation((int) position).toString();
 //				Intent intent = new Intent(ChooseLocationActivity.this,
 //						org.opensatnav.android.ChooseServiceActivity.class);
 //				intent.putExtra("location", la.getLocation((int) position).toString());
 //				
 //				intent.putExtra("fromLocation", getIntent().getStringExtra("fromLocation"));
 //				intent.putExtra("locations", getIntent().getBundleExtra("locations"));
 //				startActivity(intent);
 				
 //				Intent data = getIntent();
 //				data.putExtra("location", la.getLocation((int) position).toString());
 //				setResult(RESULT_OK, data);
 //				finish();
 
 			}
 
 		});
 	}
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Intent intent = getIntent();
 		intent.putExtra("location", to);
 		setResult(RESULT_OK, intent);
 		finish();
 	}
 
 	protected class LocationAdapter extends BaseAdapter {
 
 		GeoPoint from;
 		Bundle b = getIntent().getBundleExtra("locations");
 		String[] locationInfo = b.getStringArray("info");
 		String[] locationNames = b.getStringArray("names");
 		final int[] locationLats = b.getIntArray("latitudes");
 		final int[] locationLongs = b.getIntArray("longitudes");
 
 		public LocationAdapter(GeoPoint from) {
 			this.from = from;
 		}
 
 		@Override
 		public int getCount() {
 			return locationNames.length;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LinearLayout mainView = new LinearLayout(TransChooseLocationServiceActivity.this);
 			mainView.setOrientation(LinearLayout.VERTICAL);
 			
 			TextView placeView = new TextView(TransChooseLocationServiceActivity.this);
 			TextView infoView = new TextView(TransChooseLocationServiceActivity.this);
 			TextView distanceView = new TextView(TransChooseLocationServiceActivity.this);
 			//add name
 			String place = locationNames[position];
 			// add unnamed text for places that need it
 			if (place==null || place.length() == 0)
 				place = (String) TransChooseLocationServiceActivity.this.getResources().getText(R.string.unnamed_place);
 			// add location type
 			String info = locationInfo[position];
 //			info = info.substring(0,1).toUpperCase()+info.substring(1);
 			// add distance away
 //			String distance = 
 //				new FormatHelper(getBaseContext()).formatDistanceFuzzy
 //				(from.distanceTo(new GeoPoint(locationLats[position], locationLongs[position])))
 //				+ " " + ChooseLocationServiceActivity.this.getResources().getText(R.string.away);
 			
 			placeView.setText(place);
 			placeView.setTextSize(20);
 			placeView.setTextColor(Color.WHITE);
 			infoView.setText(info);
 //			distanceView.setText(distance);
 			
 			mainView.addView(placeView, 0);
 			mainView.addView(infoView, 1);
 			mainView.addView(distanceView, 2);
 			
 			return mainView;
 		}
 
 		public GeoPoint getLocation(int position) {
 			return new GeoPoint(locationLats[position], locationLongs[position]);
 
 		}
 		public String getParadero(int position) {
 			return locationNames[position];
 
 		}
 
 	}
 
 	
 }
