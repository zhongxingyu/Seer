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
 
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.NotificationType;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.NotificationRequest;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 
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
 	
 	private String[] array_spinner;
 	
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		pickupPoint = getApp().getJourneyPickupPoint();
 		dropoffPoint = getApp().getJourneyDropoffPoint();
 
 		
 		if(pickupPoint != null) {
 			drawCross(pickupPoint, true);
 		}
 		if(dropoffPoint != null){
 			drawCross(dropoffPoint, false);
 		}
 		
 		boolean acc = getIntent().getBooleanExtra("journeyAccepted", false);
 		boolean rej = getIntent().getBooleanExtra("journeyRejected", false);
 		
 		User driver = getApp().getSelectedJourney().getDriver();
 		User hiker 	= getApp().getSelectedJourney().getHitchhikers().get(0);
 		
 		String text = "";
 		if(driver != null){
 			text += "Driver: "+driver.getFullName() +"\n";
 		}else{
 			text += "There's no driver, call 112! (or the system admin)\n";
 		}
 		
 		if(hiker != null){
			text += "Hitchhiker: "+hiker.getFullName() +"\n";
 		}else{
 			text += "No hitchhiker\n";
 		}
 		
 		if(acc){
 			text += "The request has been accepted";
 		}else if(rej){
 			text += "The request has been rejected";
 		}else{
 			text += "The request has not been accepted or rejected yet";
 		}
 		
 		((TextView)findViewById(R.id.mapViewJourneyTextView)).setText(text);
 		
 		btn = (FrameLayout)findViewById(R.id.mapViewJourneyBtn);
 		btn.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				sendMessageToDriver();
 			}
 			
 		});
 	
 	}
 	
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
 	    
 	    Button sendBtn = (Button)customDialog.findViewById(R.id.sendBtn);
 	    Button cancelBtn = (Button)customDialog.findViewById(R.id.cancelBtn);
 	    final EditText input = (EditText)customDialog.findViewById(R.id.input);
 	    
 	    sendBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				User mid = getApp().getUser();
 				if(spinner.getSelectedItem().toString().equals("Everyone")){
 					List<User> userList = new ArrayList<User>();
 					userList.add(getApp().getSelectedJourney().getDriver());
 					for(int k=0; k<getApp().getSelectedJourney().getHitchhikers().size(); k++){
 						userList.add(getApp().getSelectedJourney().getHitchhikers().get(k));
 					}
 					userList.remove(getApp().getUser());
 					
 					for(int k=0; k<userList.size(); k++){
 						sendMessage(userList.get(k), input);
 					}
 				} else{
 				
 					for(int j=0; j<spinnerArray.size(); j++){
 						if(spinner.getSelectedItem().toString().equals(getApp().getSelectedJourney().getHitchhikers().get(j).getFullName())){
 							mid = getApp().getSelectedJourney().getHitchhikers().get(j);
 						}
 					}
 				
 					if(spinner.getSelectedItem().toString().equals(getApp().getSelectedJourney().getDriver().getFullName())){
 						mid = getApp().getSelectedJourney().getDriver();
 					}
 				
 					sendMessage(mid, input);
 				}
 				customDialog.dismiss();
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
 }
