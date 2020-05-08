 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  *
  */
 package no.ntnu.idi.socialhitchhiking.journey;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.protocol.JourneyRequest;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.freerider.protocol.RouteRequest;
 import no.ntnu.idi.freerider.protocol.RouteResponse;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.freerider.protocol.UserResponse;
 import no.ntnu.idi.socialhitchhiking.Main;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.map.MapRoute;
 import no.ntnu.idi.socialhitchhiking.utility.DateChooser;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This is the activity with a list of previous rides created by the user. The driver can reuse the old ride by selecting the ride form the list.
 * When a ride is selected the driver can change the ride preferences , time and date as he would do when creating a new ride. 
  * @author Pl
  * @author Christian Thurmann-Nielsen
  * @author Made Ziius
  *
  */
 public class ScheduleDrive extends SocialHitchhikingActivity {
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		//super.onBackPressed();
 		Log.e("Back","ScheduleDrive");
 		finish();
 	}
 	private Calendar dateAndTime;
 	private DateChooser dc;
 	private ListView listRoute;
 	private Route selectedRoute;
 	private RouteAdapter routeAdap;
 	private PropertyChangeListener propLis = new PropertyChangeListener() {
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			if(event.getPropertyName() == DateChooser.DATE_CHANGED){
 				dateAndTime = (Calendar) event.getNewValue();
 				if(dateAndTime != null){
 					sendJourneyRequest();
 				}
 				else{
 				}
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.schedule_drive);
 		listRoute = (ListView) findViewById(R.id.routeList);
 		
 		initRoutes();
 		
 		listRoute.setChoiceMode(ListView.CHOICE_MODE_SINGLE);  
 		listRoute.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parentView, View childView, final int position, long id) {
 				Log.e("Freq","" + ((Route)listRoute.getItemAtPosition(position)).getFrequency());
 				AlertDialog.Builder alertbox = new AlertDialog.Builder(ScheduleDrive.this);
 				alertbox.setTitle("Edit route");
 				alertbox.setMessage("Do you want to change the route?");
 				alertbox.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 						selectedRoute = (Route) listRoute.getItemAtPosition(position);
 						getApp().setOldEditRoute(new Route(selectedRoute));
 						
 						Intent intent = new Intent(ScheduleDrive.this, no.ntnu.idi.socialhitchhiking.map.MapActivityCreateOrEditRoute.class);
 						MapRoute mr = new MapRoute(selectedRoute.getOwner(), selectedRoute.getName(), selectedRoute.getSerial(), selectedRoute.getMapPoints());
 						getApp().setSelectedMapRoute(mr);
 						intent.putExtra("editMode", true);
 						intent.putExtra("routePosition", position);
 						startActivity(intent);
 					}
 				}); 
 				alertbox.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 						selectedRoute = (Route) listRoute.getItemAtPosition(position);
 						deleteRoute(selectedRoute);
 					}
 				});
 				alertbox.setNegativeButton("Cancel", null);
 				alertbox.show();
 				return false;
 			}
 		});
 
 		listRoute.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
 				listRoute.setItemChecked(position, true);
 				selectedRoute = (Route) listRoute.getItemAtPosition(position);
 				getApp().setSelectedRoute(selectedRoute);
 				createJourney(); 
 			}
 		});
 
 	}
 
 	private void deleteRoute(Route r){
 		Response res=null;
 		RouteRequest req;
 		
 		req = new RouteRequest(RequestType.DELETE_ROUTE, getApp().getUser(), r);
 		try {
 			res = RequestTask.sendRequest(req,getApp());
 			if(res instanceof UserResponse){
 				if(res.getStatus() == ResponseStatus.OK){
 					createAlertDialog(this, true, "Route","deleted","You are a retard!");
 					getApp().getRoutes().remove(selectedRoute);
 					routeAdap.notifyDataSetChanged();
 				}
 				if(res.getStatus() == ResponseStatus.FAILED){
 					System.out.println(res.getErrorMessage());
					if(res.getErrorMessage().toLowerCase().contains("is still referenced")){
 						createAlertDialog(this, false, "Route", "deleted", "Route is used in an active journey!");
 					}else{
						Log.e("Routeklikk",res.getErrorMessage());
 						createAlertDialog(this, false, "Route", "deleted", "Could not delete the route.");
 					}
 				}
 			}
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void initRoutes() {
 		List<Route> routes = null;
 			try {
 				routes = getRoutes();
 				getApp().setRoutes(routes);
 			} catch (ClientProtocolException e) {
 				routes = getApp().getRoutes();
 			}catch(NullPointerException e){
 				routes = getApp().getRoutes();
 			}
 		if(routes != null){
 			routeAdap = new RouteAdapter(this, 0, getApp().getRoutes());
 			listRoute.setAdapter(routeAdap);
 		}
 		if(routeAdap.getCount()==0){
 			Intent intent = new Intent(ScheduleDrive.this, Main.class);
 			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			Toast.makeText(this, "You have no saved routes", Toast.LENGTH_LONG).show();
 		}
 	}
 
 
 
 	private List<Route> getRoutes()throws ClientProtocolException{
 		Request req = new UserRequest(RequestType.GET_ROUTES, getApp().getUser());
 		Response res = null;
 		try {
 			res = RequestTask.sendRequest(req,getApp());
 			if(!(res instanceof RouteResponse) || res == null){
 				throw new ClientProtocolException("ERROR");
 			}				
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//		System.out.println(res.toString()+"\nERROR? "+res.getErrorMessage());
 		return ((RouteResponse) res).getRoutes();
 	}
 	
 
 	private void sendJourneyRequest(){
 		Journey jour = new Journey(-1);
 		jour.setRoute(selectedRoute);
 		jour.setStart(dateAndTime);
 		jour.setVisibility(getApp().getSettings().getFacebookPrivacy());
 		JourneyRequest req = new JourneyRequest(RequestType.CREATE_JOURNEY, getApp().getUser(), jour);
 
 		Response res = null;
 		try {
 			res = RequestTask.sendRequest(req,getApp());
 			if(res.getStatus() != ResponseStatus.OK){
 				createAlertDialog(this, false,  "Journey","created","");
 			}
 			else{
 				if(getApp().getJourneys() != null)
 					getApp().getJourneys().add(jour);
 				createAlertDialog(this, true, "Journey","created","");
 			}
 		} catch (ClientProtocolException e) {
 			createAlertDialog(this, false,  "Journey","created","");
 		} catch (IOException e) {
 			createAlertDialog(this, false,"Journey","created","");
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void createJourney(){
 		
 		Intent intent = new Intent(ScheduleDrive.this, no.ntnu.idi.socialhitchhiking.journey.TripOptions.class);
 		startActivity(intent);
 //		AlertDialog.Builder b = new AlertDialog.Builder(this);
 //		b.setTitle("Create a trip");
 //		b.setMessage("Do you want to create a trip with this route?");
 //		b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 //			@Override
 //			public void onClick(DialogInterface dialog, int which) {
 //				dc = new DateChooser(ScheduleDrive.this, propLis);
 //				dc.setTitle("Set Date of Journey", "Set Time of Journey");
 //				dc.show();
 ////				Intent intent = new Intent(ScheduleDrive.this, no.ntnu.idi.socialhitchhiking.journey.TripOptions.class);
 ////				startActivity(intent);
 //			}
 //		});
 //		b.setNegativeButton("Cancel", null);
 //		b.show();
 		
 	}
 	@Override
 	protected void onPause() {
 		super.onPause();
 		//Why on earth is this code here?
 		//if(routeAdap != null)routeAdap.notifyDataSetChanged();
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 		//if(routeAdap != null)routeAdap.notifyDataSetChanged();
 	}
 	private class RouteAdapter extends ArrayAdapter<Route>{
 		public RouteAdapter(Context context, int textViewResourceId,
 				List<Route> objects) {
 			super(context, textViewResourceId, objects);
 			// TODO Auto-generated constructor stub
 		}
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row;
 			try
 			{
 				Route current = this.getItem(position);
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.list_row, parent, false);
 				//sends the strings with ride info to list_row.xml 
 				TextView rideDescription =(TextView)row.findViewById(R.id.ride_description);
 				TextView name = (TextView)row.findViewById(R.id.ride_title);
 				rideDescription.setText("From: "+current.getStartAddress() +"\n"+ "To: "+current.getEndAddress());
 				name.setText(current.getName());
 				
 			}
 			catch(NullPointerException e)
 			{
 			 row = convertView;
 			}
 			return row;
 		}
 	}
 
 }
