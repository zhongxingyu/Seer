 //
 // Copyright 2012 Shaun Simpson
 // shauns2029@gmail.com
 //
 
 package uk.co.immutablefix.ClockControl;
 
 import java.util.Locale;
 

 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.TextView;
 
 public class ClockControlActivity extends Activity {
 	String hostname = "";
 	String weather = "";
 	Boolean running = true, paused = true;
 
 	DnssdDiscovery dns;
 	TCPClient tcp;
 	
 	Button btnVolDown, btnVolUp;
 	RadioButton rbtnClock1, rbtnClock2;
 	TextView txtPlaying;
 	SharedPreferences prefs = null;
 	
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        
         dns = DnssdDiscovery.getInstance(getBaseContext());
         
         tcp = new TCPClient();
         tcp.setTimeout(5000);
         
         //  Log.d("Events", "Starting ... ");
     	
     	txtPlaying = (TextView) findViewById(R.id.txt_playing);	    
         btnVolUp = (Button) findViewById(R.id.btn_volumeup);
 	    btnVolUp.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:VOLUP");
 			}
 		});	
 
 	    btnVolDown = (Button) findViewById(R.id.btn_volumedown);
 	    btnVolDown.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:VOLDOWN");
 			}
 		});	
 	
 	    Button btnNext = (Button) findViewById(R.id.btn_next);
 	    btnNext.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:NEXT");
 			}
 		});	
 	
 	    Button btnMusic = (Button) findViewById(R.id.btn_music);
 	    btnMusic.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:MUSIC");
 			}
 		});	
 	
 	    Button btnSleep = (Button) findViewById(R.id.btn_sleep);
 	    btnSleep.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:SLEEP");
 				//  Log.d("Events", "Sleep");
 			}
 		});	
 	
 	    Button btnMeditation = (Button) findViewById(R.id.btn_meditation);
 	    btnMeditation.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:MEDITATION");
 				//  Log.d("Events", "Meditation");
 			}
 		});	
 
 	    Button btnPause = (Button) findViewById(R.id.btn_pause);
 	    btnPause.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				tcp.sendMessage(getTargetIp(), 44558, "CLOCK:PAUSE");
 				//  Log.d("Events", "Sleep");
 
 			}
 		});	
 	    
 	    rbtnClock1 = (RadioButton) findViewById(R.id.radioButton1);
 	    rbtnClock2 = (RadioButton) findViewById(R.id.radioButton2);	    
 	    
 		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		rbtnClock1.setText(prefs.getString("clock1_name", (String) rbtnClock1.getText()));
 		rbtnClock2.setText(prefs.getString("clock2_name", (String) rbtnClock2.getText()));
 	
 	    rbtnClock1.setOnClickListener(new View.OnClickListener() {	
 			@Override
 			public void onClick(View v) {
 				updatePlaying();
 			}
 		});			
 	    
 	    rbtnClock2.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				updatePlaying();
 			}
 		});	
 
 	    final Handler handler = new Handler();
 	    
 		Runnable runnable = new Runnable() {
 		    String reply;
 		    
 			@Override
 			public void run() {
 				int time = 500;
 				
 				while (running) {
 	                try {
                 		Thread.sleep(time);
 	                } catch (InterruptedException e1) {
 	                	// TODO Auto-generated catch block
 	                	e1.printStackTrace();
 	                }	
 			    	
 					if (!paused) {
 						if (btnVolUp.isPressed())
 						{
 							tcp.sendMessage(getTargetIp(), 44558, "CLOCK:VOLUP");
 						}
 						else if (btnVolDown.isPressed())
 						{
 							tcp.sendMessage(getTargetIp(), 44558, "CLOCK:VOLDOWN");
 						}
 						else
 						{
 							//  Log.d("TREAD", "Getting data ...");
 							reply = tcp.getMessage(getBaseContext(), getTargetIp(), 44558, "CLOCK:PLAYING");
 						}
 					}
 
 					handler.post(new Runnable() {
 						@Override
 						public void run() {
 							if ((reply != null) && (reply.length() > 0) && (!reply.equals(txtPlaying.getText()))) 
 								txtPlaying.setText(reply);
 						}
 					});
 				}
 			}
 		};
 		new Thread(runnable).start();
 		//  Log.d("Events", "Starting ... Fin");
      }
 
     public void onStart()
     {
     	super.onStart();
 
 		rbtnClock1.setText(prefs.getString("clock1_name", (String) rbtnClock1.getText()));
 		rbtnClock2.setText(prefs.getString("clock2_name", (String) rbtnClock2.getText()));
 
 		paused = false;
 	}
     
     public void onPause()
     {
     	super.onPause();    	
     	paused = true;
     }
     
     public void onReStart()
     {
     	super.onRestart();   	
     	paused = false;
     }
     
     //Creates menus
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     }
     
     //Handles menu clicks
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     	case R.id.mitmWeather:
     		Bundle basket = new Bundle();
     		basket.putString("ipAddress", getTargetIp());
     		Intent weather = new Intent(this, WeatherActivity.class);
     		weather.putExtras(basket);    		
     		startActivity(weather);
     		return true;
     	case R.id.mitmPreferences:
     		startActivity(new Intent(this, Preferences.class));
     		return true;
     	case R.id.mitmQuit:
     		finish();
     		break;
     	}
     	
     	return false;
     }    
   
     @Override
     public void onDestroy(){
     	super.onDestroy();
     	running = false;
     }
     
 	private String getTargetIp() {
     	if (rbtnClock1.isChecked())
     		hostname = prefs.getString("clock1_address", hostname).toLowerCase(Locale.getDefault());
     	else
     		hostname = prefs.getString("clock2_address", hostname).toLowerCase(Locale.getDefault());
         return dns.getHostAddress(hostname);
     }
 	private void updatePlaying() {
 		String reply;
 		txtPlaying.setText("Updating ...");
 		reply = tcp.getMessage(getBaseContext(), getTargetIp(), 44558, "CLOCK:PLAYING");
 		txtPlaying.setText(reply);
 	}
 }
