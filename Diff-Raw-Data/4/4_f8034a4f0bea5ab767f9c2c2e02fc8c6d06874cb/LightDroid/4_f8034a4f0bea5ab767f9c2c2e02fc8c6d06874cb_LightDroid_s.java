 package com.mettadore.lightdroid;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.BitSet;
 
 import org.apache.commons.net.telnet.TelnetClient;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 //import com.mettadore.*;
 
 public class LightDroid extends Activity implements OnSeekBarChangeListener
 {
 
 	int seek_bar_value;
 	int[] channel_values;
 	ArrayList<ToggleButton> toggles;
 	String telnet_server;
 	String server_ip;
 	int server_port;
 	TelnetClient telnet = new TelnetClient();
 	InputStream in;
 	PrintStream out;
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		//		Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
 		//		startActivity(settingsActivity);
 
 		final SeekBar slider = (SeekBar) findViewById(R.id.slider);
 		slider.setOnSeekBarChangeListener(this);
 		slider.setMax(255);
 
 		final ToggleButton togglebutton1 = (ToggleButton) findViewById(R.id.togglebutton1);
 		final ToggleButton togglebutton2 = (ToggleButton) findViewById(R.id.togglebutton2);
 		final ToggleButton togglebutton3 = (ToggleButton) findViewById(R.id.togglebutton3);
 		final ToggleButton togglebutton4 = (ToggleButton) findViewById(R.id.togglebutton4);
 		final ToggleButton togglebutton5 = (ToggleButton) findViewById(R.id.togglebutton5);
 		final ToggleButton togglebutton6 = (ToggleButton) findViewById(R.id.togglebutton6);
 		final ToggleButton togglebutton7 = (ToggleButton) findViewById(R.id.togglebutton7);
 		final ToggleButton togglebutton8 = (ToggleButton) findViewById(R.id.togglebutton8);
 		final ToggleButton togglebutton9 = (ToggleButton) findViewById(R.id.togglebutton9);
 		final ToggleButton togglebutton10 = (ToggleButton) findViewById(R.id.togglebutton10);
 		final ToggleButton togglebutton11 = (ToggleButton) findViewById(R.id.togglebutton11);
 		final ToggleButton togglebutton12 = (ToggleButton) findViewById(R.id.togglebutton12);
 		final ToggleButton togglebutton13 = (ToggleButton) findViewById(R.id.togglebutton13);
 		final ToggleButton togglebutton14 = (ToggleButton) findViewById(R.id.togglebutton14);
 		final ToggleButton togglebutton15 = (ToggleButton) findViewById(R.id.togglebutton15);
 		final ToggleButton togglebutton16 = (ToggleButton) findViewById(R.id.togglebutton16);
 		final ToggleButton togglebutton17 = (ToggleButton) findViewById(R.id.togglebutton17);
 		final ToggleButton togglebutton18 = (ToggleButton) findViewById(R.id.togglebutton18);
 		final ToggleButton togglebutton19 = (ToggleButton) findViewById(R.id.togglebutton19);
 		final ToggleButton togglebutton20 = (ToggleButton) findViewById(R.id.togglebutton20);
 		final ToggleButton togglebutton21 = (ToggleButton) findViewById(R.id.togglebutton21);
 		final ToggleButton togglebutton22 = (ToggleButton) findViewById(R.id.togglebutton22);
 		final ToggleButton togglebutton23 = (ToggleButton) findViewById(R.id.togglebutton23);
 		final ToggleButton togglebutton24 = (ToggleButton) findViewById(R.id.togglebutton24);
 		final ToggleButton togglebutton25 = (ToggleButton) findViewById(R.id.togglebutton25);
 		final ToggleButton togglebutton26 = (ToggleButton) findViewById(R.id.togglebutton26);
 		final ToggleButton togglebutton27 = (ToggleButton) findViewById(R.id.togglebutton27);
 		final ToggleButton togglebutton28 = (ToggleButton) findViewById(R.id.togglebutton28);
 		final ToggleButton togglebutton29 = (ToggleButton) findViewById(R.id.togglebutton29);
 		final ToggleButton togglebutton30 = (ToggleButton) findViewById(R.id.togglebutton30);
 		final ToggleButton togglebutton31 = (ToggleButton) findViewById(R.id.togglebutton31);
 		final ToggleButton togglebutton32 = (ToggleButton) findViewById(R.id.togglebutton32);
 		final ToggleButton togglebutton33 = (ToggleButton) findViewById(R.id.togglebutton33);
 		final ToggleButton togglebutton34 = (ToggleButton) findViewById(R.id.togglebutton34);
 		final ToggleButton togglebutton35 = (ToggleButton) findViewById(R.id.togglebutton35);
 		final ToggleButton togglebutton36 = (ToggleButton) findViewById(R.id.togglebutton36);
 
 		toggles = new ArrayList();
 		toggles.add(togglebutton1);
 		toggles.add(togglebutton2);
 		toggles.add(togglebutton3);
 		toggles.add(togglebutton4);
 		toggles.add(togglebutton5);
 		toggles.add(togglebutton6);
 		toggles.add(togglebutton7);
 		toggles.add(togglebutton8);
 		toggles.add(togglebutton9);
 		toggles.add(togglebutton10);
 		toggles.add(togglebutton11);
 		toggles.add(togglebutton12);
 		toggles.add(togglebutton13);
 		toggles.add(togglebutton14);
 		toggles.add(togglebutton15);
 		toggles.add(togglebutton16);
 		toggles.add(togglebutton17);
 		toggles.add(togglebutton18);
 		toggles.add(togglebutton19);
 		toggles.add(togglebutton20);
 		toggles.add(togglebutton21);
 		toggles.add(togglebutton22);
 		toggles.add(togglebutton23);
 		toggles.add(togglebutton24);
 		toggles.add(togglebutton25);
 		toggles.add(togglebutton26);
 		toggles.add(togglebutton27);
 		toggles.add(togglebutton28);
 		toggles.add(togglebutton29);
 		toggles.add(togglebutton30);
 		toggles.add(togglebutton31);
 		toggles.add(togglebutton32);
 		toggles.add(togglebutton33);
 		toggles.add(togglebutton34);
 		toggles.add(togglebutton35);
 		toggles.add(togglebutton36);
 
 		channel_values = new int[36];
 		for (int value : channel_values) {
 			value = 0;
 		}
 
 		OnClickListener toggleclick = new OnClickListener() {
 			public void onClick(View v) {
				
 			}
 		};
 		
 		final ToggleButton connectbutton = (ToggleButton) findViewById(R.id.connectbutton);
 
 		OnClickListener connectionlistener = new OnClickListener() {
 			public void onClick(View v) {
 				if (((ToggleButton) v).isChecked()) {
 					try {
 						telnet.connect( "192.168.2.9", 3100 );
 						toast("Connected to telnet server");
 					} catch( Exception e ) {
 						e.printStackTrace();
 						toast("Cannot connect to server");
 					}
 				} else {
 					try {
 						telnet.disconnect();
 						toast("Disconnect from telnet server");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						toast("Problem disconnecting");
 					}
 				}
 			}
 		};
 		connectbutton.setOnClickListener(connectionlistener);
 		for (ToggleButton toggle : toggles) {
 			toggle.setOnClickListener(toggleclick);
 		}
 		
 		final Button allbutton = (Button) findViewById(R.id.allbutton);
 		allbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				for (ToggleButton toggle : toggles) {
 					toggle.setChecked(true);
 				}
 				toast("Select All");
 			}
 		});
 		final Button nonebutton = (Button) findViewById(R.id.nonebutton);
 		nonebutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				for (ToggleButton toggle : toggles) {
 					toggle.setChecked(false);
 				}
 				toast("Select None");
 			}
 		});
 		final Button resetbutton = (Button) findViewById(R.id.resetbutton);
 		resetbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				sendTelnetCommand("+1/36");
 				sendTelnetCommand("All @ 0");
 				sendTelnetCommand("Clear all");
 				slider.setProgress(0);
 				for (ToggleButton toggle : toggles) {
 					toggle.setChecked(false);
 				}
 				toast("Channels Reset");
 			}
 		});
 		final Button offbutton = (Button) findViewById(R.id.offbutton);
 		offbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				String channels = channelsList();
 				sendTelnetCommand(channels);
 				String command = String.format("%s @ 0", channels);
 				sendTelnetCommand(command);
 				slider.setProgress(0);
 			}
 		});
 		final Button fullbutton = (Button) findViewById(R.id.fullbutton);
 		fullbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				String channels = channelsList();
 				sendTelnetCommand(channels);
 				String command = String.format("%s @ 100", channels);
 				sendTelnetCommand(command);
				slider.setProgress(0);
 			}
 		});
 	}
 	
 	public void toast(String message) {
 		Toast.makeText(LightDroid.this, message, Toast.LENGTH_SHORT).show();
 	}
 
 	public int percentage() {
 		int value = (int) ((seek_bar_value / 255.0) * 100.0);
 		return value;
 	}
 	
 	public String channelsList() {
 		BitSet bs = new BitSet();
 		int i = 1;
 	    for (ToggleButton toggle : toggles) {
 	    	if (toggle.isChecked()) {
 	    		bs.set(i);
 	    	}
 	    	i++;
 	    }
 	
 	    StringBuilder sb = new StringBuilder();
 	    for (int begin, end = -1; (begin = bs.nextSetBit(end + 1)) != -1; ) {
 	        end = bs.nextClearBit(begin) - 1;
 	        if (sb.length() > 0) sb.append(", ");
 	        sb.append(
 	            (begin == end)
 	                ? String.format("+%d", begin)
 	                : String.format("+%d/%d", begin, end)
 	        );
 	    }
 	    return sb.toString();
 	}
 	
 	public void sendTelnetCommand(String command) {
 		// TODO: move back to the TelnetSample class and refactor
 		// Restore preferences
 		/*       SharedPreferences settings = getPreferences(MODE_PRIVATE);
        server_ip = settings.getString("serverip", "");
        String port = (String) settings.getString("serverport","");
        server_port = Integer.parseInt(port.trim());
 		 */
 		try {
 			// Get input and output stream references
 			in = telnet.getInputStream();
 			out = new PrintStream( telnet.getOutputStream() );
 			out.println( command );
 			out.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		//		freq_bar_value = progress;
 	}
 
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		seek_bar_value = seekBar.getProgress();
 		String s;
 		String channels = channelsList();
 		s = String.format("%s @ DMX %d", channels, seek_bar_value);
 		sendTelnetCommand(s);
 	}
 
 
 }
 
