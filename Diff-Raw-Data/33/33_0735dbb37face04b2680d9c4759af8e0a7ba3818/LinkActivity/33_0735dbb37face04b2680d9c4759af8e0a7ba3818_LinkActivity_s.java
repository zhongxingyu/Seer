 package nl.q42.hue2.activities;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.hue2.adapters.BridgeAdapter;
 import nl.q42.hue2.models.Bridge;
 import nl.q42.javahueapi.Networker;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 
 public class LinkActivity extends Activity {
 	private static final int SEARCH_TIMEOUT = 5000;
 	
 	private BridgeAdapter bridgesAdapter;
 	private ListView bridgesList;
 	
 	private ImageButton refreshButton;
 	private ProgressBar loadingSpinner;
 	
 	private BridgeSearchTask bridgeSearchTask;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_link);
 		
 		// Set up layout
 		bridgesList = (ListView) findViewById(R.id.link_bridges);
 		bridgesAdapter = new BridgeAdapter(this);
 		bridgesList.setAdapter(bridgesAdapter);
 		
 		// Set up loading UI elements		
 		ActionBar ab = getActionBar();
 		ab.setCustomView(R.layout.link_loader);
 		ab.setDisplayShowCustomEnabled(true);
 		
 		RelativeLayout loadingLayout = (RelativeLayout) ab.getCustomView();
 
 		loadingSpinner = (ProgressBar) loadingLayout.findViewById(R.id.link_loading_spinner);
 		
 		refreshButton = (ImageButton) loadingLayout.findViewById(R.id.link_refresh_button);
 		refreshButton.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				startSearching();
 			}
 		});
 		
 		// Start searching for bridges and add them to the results
 		startSearching();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		
 		stopSearching();
 	}
 	
 	private void stopSearching() {		
 		if (bridgeSearchTask != null) {
 			bridgeSearchTask.cancel(true);
 			bridgeSearchTask = null;
 			
 			bridgesList.post(new Runnable() {
 				@Override
 				public void run() {
 					setSearchIndicator(false);
 				}
 			});
 		}
 	}
 	
 	private void startSearching() {		
 		stopSearching();
 		
 		bridgesAdapter.clear();
 		
 		bridgeSearchTask = new BridgeSearchTask();
 		bridgeSearchTask.execute();
 		
 		setSearchIndicator(true);
 	}
 	
 	private void setSearchIndicator(boolean searching) {
 		if (searching) {
 			refreshButton.setVisibility(View.GONE);
 			loadingSpinner.setVisibility(View.VISIBLE);
 		} else {
 			refreshButton.setVisibility(View.VISIBLE);
 			loadingSpinner.setVisibility(View.GONE);
 		}
 	}
 	
 	private class BridgeSearchTask extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected void onPostExecute(Boolean result) {
 			bridgeSearchTask = null;
 			setSearchIndicator(false);
 			
 			if (!result) {
 				new AlertDialog.Builder(LinkActivity.this)
 					.setTitle(R.string.dialog_bridge_search_title)
 					.setMessage(R.string.dialog_bridge_search)
 					.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					}).create().show();
 			}
 		}
 		
 		@Override
 		protected Boolean doInBackground(Void... params) {			
 			// Search bridges on local network using UPnP
 			try {				
 				String upnpRequest = "M-SEARCH * HTTP/1.1\nHOST: 239.255.255.250:1900\nMAN: ssdp:discover\nMX: 8\nST:SsdpSearch:all";
 				DatagramSocket upnpSock = new DatagramSocket();
 				upnpSock.setSoTimeout(SEARCH_TIMEOUT);
 				upnpSock.send(new DatagramPacket(upnpRequest.getBytes(), upnpRequest.length(), new InetSocketAddress("239.255.255.250", 1900)));
 				
 				HashMap<String, Boolean> ipsDiscovered = new HashMap<String, Boolean>();
 				
 				while (true) {
 					byte[] responseBuffer = new byte[1024];
 					
 					DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
 					upnpSock.receive(responsePacket);
 					
 					final String ip = responsePacket.getAddress().getHostAddress();
 					final String response = new String(responsePacket.getData());
 					
 					if (!ipsDiscovered.containsKey(ip)) {
 						Matcher m = Pattern.compile("LOCATION: (.*)", Pattern.CASE_INSENSITIVE).matcher(response);
 						
 						if (m.find()) {
 							final String description = Networker.get(m.group(1)).getBody();
 							
 							// Parsing with RegEx allowed here because the output format is fairly strict
 							final String modelName = Util.quickMatch("<modelName>(.*?)</modelName>", description);
							final String friendlyName = Util.quickMatch("<friendlyName>(.*?)</friendlyName>", description);
 															
 							// Check from description if we're dealing with a hue bridge or some other device
							if (modelName.toLowerCase().contains("philips hue bridge")) {
								// Get bridge name this way, because you need a valid username to get it through the config API
								final String bridgeName = Util.quickMatch("(.*) \\([^)]+\\)$", friendlyName);
								
								bridgesList.post(new Runnable() {
									@Override
									public void run() {
										bridgesAdapter.add(new Bridge(ip, bridgeName));
									}
								});
 							}
 						}
 						
 						// Ignore subsequent packets
 						ipsDiscovered.put(ip, true);
 					}
 				}
 			} catch (SocketTimeoutException e) {
 				// Gracefully stop
 			} catch (SocketException e) {
 				return false;
 			} catch (IOException e) {
 				// Not sure what would cause this to happen
 				e.printStackTrace();
 			}
 			
 			return true;
 		}
 	}
 }
