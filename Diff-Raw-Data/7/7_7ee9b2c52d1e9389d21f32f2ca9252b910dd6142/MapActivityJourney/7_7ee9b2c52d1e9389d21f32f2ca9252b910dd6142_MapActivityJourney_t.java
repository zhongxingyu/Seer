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
  */
 package no.ntnu.idi.socialhitchhiking.map;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.client.ClientProtocolException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.NotificationType;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.JourneyRequest;
 import no.ntnu.idi.freerider.protocol.NotificationRequest;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.SocialHitchhikingApplication;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.MapView;
 
 /**
  * This activity is where a user (the driver) can see a journey request, 
  * and choose to accept or reject it.
  */
 public class MapActivityJourney extends MapActivityAbstract{
 
 	/**
 	 * The pickup point.
 	 */
 	private Location pickupPoint;
 	
 	/**
 	 * The dropoff point.
 	 */
 	private Location dropoffPoint;
 	
 	private FrameLayout btn;
 	private FrameLayout leaveRide;
 	
 	private String[] array_spinner;
 	
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		pickupPoint = getApp().getJourneyPickupPoint();
 		dropoffPoint = getApp().getJourneyDropoffPoint();
 		
 		TextView driverView = (TextView)findViewById(R.id.mapViewJourneyDriver);
		TextView leaveTxt = (TextView)findViewById(R.id.leaveRideText);
		
		if(getApp().getUser().getFullName().equals(getApp().getSelectedJourney().getDriver().getFullName())){
			leaveTxt.setText("Cancel ride");
		}else{
			leaveTxt.setText("Leave ride");
		}
 		
 		if(pickupPoint != null) {
 			drawCross(pickupPoint, true);
 		}
 		if(dropoffPoint != null){
 			drawCross(dropoffPoint, false);
 		}	
 		driverView.setText(getApp().getSelectedJourney().getDriver().getFullName());
 		
 		
 		if(getApp().getSelectedJourney().getHitchhikers().size() != 0){
 			
 			Log.e("Du har kommet hit", "1");
 			TextView firstHitchTxt = (TextView)findViewById(R.id.firstHikerTxt);
 				
 			firstHitchTxt.setText(getApp().getSelectedJourney().getHitchhikers().get(0).getFullName());
 			
 			
 			if(getApp().getSelectedJourney().getHitchhikers().size() > 1){
 				Log.e("Du har kommet hit", "2");
 				for(int c=1; c<getApp().getSelectedJourney().getHitchhikers().size(); c++){
 					HitchList hitch = new HitchList(getApp().getSelectedJourney().getHitchhikers().get(c));
 				}
 			}
 			
 			
 		}else{
 			Log.e("Du har kommet hit", "3");
 			TextView firstHitchTxt = (TextView)findViewById(R.id.firstHikerTxt);
 			firstHitchTxt.setText("No hitchhikers");
 		}
 		
 		leaveRide = (FrameLayout)findViewById(R.id.leaveRide);
 		leaveRide.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				
 					final Dialog confirmDialog = new Dialog(MapActivityJourney.this);
 					confirmDialog.setContentView(R.layout.cancel_ride_layout);
 					
 					ImageView okBtn = (ImageView)confirmDialog.findViewById(R.id.okBtn);
 					ImageView cancelBtn = (ImageView)confirmDialog.findViewById(R.id.cBtn);
 					TextView contentTxt = (TextView)confirmDialog.findViewById(R.id.questionField);
 					confirmDialog.setTitle("Confirm");
 					
 					if(getApp().getUser().getFullName().equals(getApp().getSelectedJourney().getDriver().getFullName())){
 						contentTxt.setText("Do you want to cancel this ride?");
 					} else{
 						contentTxt.setText("Do you want to leave this ride?");
 					}
 					
 					
 					okBtn.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							handleJourney(getApp().getSelectedJourney());
 						}
 					});
 					
 					cancelBtn.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							confirmDialog.dismiss();
 						}
 					});
 					
 					confirmDialog.show();
 			}
 			
 		});
 		
 		btn = (FrameLayout)findViewById(R.id.mapViewJourneyBtn);
 		btn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				sendMessageToDriver();
 			}
 			
 		});
 	
 	}
 	
 	private void handleJourney(Journey j){
 		if(j.getHitchhikers() != null){
 			sendCancelJourney(j);
 		}
 		else sendDeleteJourney(j);
 	}
 	
 	private void sendCancelJourney(Journey j) {
 		NotificationType type;
 		String id = getApp().getUser().getID();
 		Notification notif;
 		if(!j.getDriver().getID().equals(id) ){
 			type = NotificationType.HITCHHIKER_CANCEL;
 			notif = new Notification(id, j.getRoute().getOwner().getID(),"", "", j.getSerial(), type);
 		}else{ 
 			type = NotificationType.HITCHHIKER_ACCEPTS_DRIVER_CANCEL;
 			notif = new Notification(id, id, "","", j.getSerial(), type);
 		}
 		NotificationRequest req = new NotificationRequest(getApp().getUser(), notif);
 		boolean succeded = sendJourneyRequest(req);
 		if(succeded){
 			deleteJourneyFromList(j);
 		}
 	}
 	
 	private boolean sendJourneyRequest(Request req){
 		Response res;
 		try {
 			res = RequestTask.sendRequest(req,getApp());
 			boolean succeded = res.getStatus() == ResponseStatus.OK;
 			return succeded;
 		} catch (ClientProtocolException e) {
 		} catch (IOException e) {
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.e("ExecutionMelvin", e.getMessage());
 		}
 		return false;
 	}
 	
 	private void deleteJourneyFromList(Journey j){
 		getApp().getJourneys().remove(j);
 	}
 	
 	private void sendDeleteJourney(Journey j){
 		JourneyRequest req = new JourneyRequest(RequestType.DELETE_JOURNEY, getApp().getUser(), j);
 
 		boolean succeded = sendJourneyRequest(req);
 		if(succeded){
 			deleteJourneyFromList(j);
 		}
 	}
 	/*
 	private void cancelJourney(final Journey j){
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Journey");
 		b.setMessage("What do you want to do with this journey?");
 		b.setPositiveButton("Cancel it", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				handleJourney(j);
 			}
 		});
 		b.setNegativeButton("Show in map", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				showInMap(j);
 			}
 		});
 		b.setNeutralButton("Nothing", new OnClickListener() {
 			public void onClick(DialogInterface arg0, int arg1) {
 				
 			}
 		});
 		b.show();
 	}
 	*/
 	private void sendMessageToDriver(){
 		
 		final Dialog customDialog = new Dialog(this);
 		customDialog.setContentView(R.layout.custom_dialog_layout);
 		customDialog.setTitle("Message");
 		
 		final List<String> spinnerArray =  new ArrayList<String>();
 		spinnerArray.add("Everyone");
 		if(!getApp().getSelectedJourney().getDriver().equals(getApp().getUser())){
 			spinnerArray.add(getApp().getSelectedJourney().getDriver().getFullName());
 		}
 		
 		for(int i=0; i<getApp().getSelectedJourney().getHitchhikers().size(); i++){
 			if(!getApp().getSelectedJourney().getHitchhikers().get(i).equals(getApp().getUser())){
 				spinnerArray.add(getApp().getSelectedJourney().getHitchhikers().get(i).getFullName());
 			}
 	    }
 		
 		final Spinner spinner = (Spinner)customDialog.findViewById(R.id.spinner);
 	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapActivityJourney.this, android.R.layout.simple_spinner_item, spinnerArray);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner.setAdapter(adapter);
 	    
 	    ImageView sendBtn = (ImageView)customDialog.findViewById(R.id.sendBtn);
 	    ImageView cancelBtn = (ImageView)customDialog.findViewById(R.id.cancelBtn);
 	    final EditText input = (EditText)customDialog.findViewById(R.id.input);
 	    
 	    sendBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				User mid = getApp().getUser();
 				if(spinner.getSelectedItem().toString().equals("Everyone")){
 					if(input.getText().toString().equals("")){
 						input.setHint("Please fill in your message");
 						Toast toast = Toast.makeText(MapActivityJourney.this, "Please fill in your message", Toast.LENGTH_SHORT);
 						toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 						toast.show();
 					}else{
 						List<User> userList = new ArrayList<User>();
 						userList.add(getApp().getSelectedJourney().getDriver());
 						for(int k=0; k<getApp().getSelectedJourney().getHitchhikers().size(); k++){
 							userList.add(getApp().getSelectedJourney().getHitchhikers().get(k));
 						}
 						userList.remove(getApp().getUser());
 						
 						for(int k=0; k<userList.size(); k++){
 							sendMessage(userList.get(k), input);
 						}
 						
 						Toast toast = Toast.makeText(MapActivityJourney.this, "Message sent", Toast.LENGTH_SHORT);
 						toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 						toast.show();
 						customDialog.dismiss();
 					}
 				} else{
 				
 					for(int j=0; j<getApp().getSelectedJourney().getHitchhikers().size(); j++){
 						if(spinner.getSelectedItem().toString().equals(getApp().getSelectedJourney().getHitchhikers().get(j).getFullName())){
 							mid = getApp().getSelectedJourney().getHitchhikers().get(j);
 						}
 					}
 				
 					if(spinner.getSelectedItem().toString().equals(getApp().getSelectedJourney().getDriver().getFullName())){
 						mid = getApp().getSelectedJourney().getDriver();
 					}
 					if(input.getText().toString().equals("")){
 						input.setHint("Please fill in your message");
 						Toast toast = Toast.makeText(MapActivityJourney.this, "Please fill in your message", Toast.LENGTH_SHORT);
 						toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 						toast.show();
 					}else{
 						sendMessage(mid, input);
 						Toast toast = Toast.makeText(MapActivityJourney.this, "Message sent", Toast.LENGTH_SHORT);
 						toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 						toast.show();
 						customDialog.dismiss();
 					}
 					
 				}
 				
 			}
 			
 		});
 	    
 	    cancelBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				customDialog.dismiss();
 			}
 		});
 	    
 		customDialog.show();
 		
 	}
 	
 	private void sendMessage(User mid, EditText input){
 		Log.e("SendMessage", "Message to: " + mid.getFullName() + ", content: " + input.getText().toString());
 		Notification not = new Notification(getApp().getUser().getID(), mid.getID(), getApp().getUser().getFullName(), input.getText().toString(), getApp().getSelectedJourney().getSerial(), NotificationType.MESSAGE, getApp().getSelectedMapRoute().getStartLocation(), getApp().getSelectedMapRoute().getEndLocation(), Calendar.getInstance());
     	NotificationRequest req = new NotificationRequest(getApp().getUser(), not);
     	
     	try {
 			Response res = RequestTask.sendRequest(req, getApp());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	@Override
 	protected void initContentView() {
 		setContentView(R.layout.mapactivity_journey);
 	}
 	@Override
 	protected void initMapView() {
 		mapView = (MapView)findViewById(R.id.mapViewJourneyMapView); 
 	}
 	@Override
 	protected void initProgressBar() {
 		setProgressBar((ProgressBar)findViewById(R.id.mapViewJourneyProgressBar));
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
 	 */
 	@Override
 	public void onLongPress(MotionEvent e) {
 	} 
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
 	 */
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		return false;
 	}
 	
 	public class HitchList{
 		private User hiker;
 		private ImageView icon;
 		private TextView hitchTxt;
 		private FrameLayout frame;
 		private Resources r;
 		
 		public HitchList(User hitchHiker){
 			r = getApp().getResources();
 			hiker = hitchHiker;
 			this.icon = new ImageView(MapActivityJourney.this);
 			this.hitchTxt = new TextView(MapActivityJourney.this);
 			this.frame = new FrameLayout(MapActivityJourney.this);
 			LinearLayout linear = (LinearLayout)findViewById(R.id.linLayout);
 			
 			frame.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 			//frame.setPadding(left, top, right, bottom)
 			frame.setPadding(0, dipToPx(8), 0, 0);
 			//frame.setPadding(0, 8, 0, 0);
 			FrameLayout.LayoutParams lli2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 			lli2.setMargins(0, 0, 0, 0);
 			icon.setLayoutParams(lli2);
 			icon.setImageResource(R.drawable.ic_menu_cc);
 			
 			frame.addView(icon);
 			
 			FrameLayout.LayoutParams lliDest = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 			lliDest.setMargins(dipToPx(35), dipToPx(4), 0, 0);
 			//lliDest.setMargins(35, 4, 0, 0);
 			//lliDest.setMargins(left, top, right, bottom)
 			hitchTxt.setLayoutParams(lliDest);
 			hitchTxt.setText(hiker.getFullName());
 			
 			frame.addView(hitchTxt);
 			linear.addView(frame);
 			
 		}
 		
 		public int dipToPx(int dip){
 			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
 			return (int)px;
 		}
 		
 	}
 }
