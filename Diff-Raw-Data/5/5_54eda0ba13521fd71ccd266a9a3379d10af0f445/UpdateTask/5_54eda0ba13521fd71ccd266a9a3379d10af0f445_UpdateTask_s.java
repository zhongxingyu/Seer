 package ru.allgage.geofriend;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.os.AsyncTask;
 
 
 public class UpdateTask extends AsyncTask<Long, Object, Void> {
 
 	private GoogleMap map;
 	DataOutputStream dout;
     DataInputStream din;
     private HashMap<String, Marker> markers;
 	
 	public UpdateTask(GoogleMap mMap, HashMap<String, Marker> mp) {
 		// TODO Auto-generated constructor stub
 		map = mMap;
 		din = TaskSocket.in;
 		markers = mp;
 	}
 	
 	@Override
	protected void onPreExecute () {
		map.clear();
	}
	
	@Override
 	protected Void doInBackground(Long... params) {
 		// TODO Auto-generated method stub
 		
 		try {
 			synchronized(TaskSocket.socket) {
 				if(params[0] == 0) {
 					TaskSocket.out.writeUTF("getOnlineStatuses");
 				}
 				else {
 					TaskSocket.out.writeUTF("updateAllStatuses");
 					TaskSocket.out.writeLong(params[0]);
 				}
 				String isEnd;
 				while(!(isEnd = din.readUTF()).equals("end")) {
 					String login = din.readUTF();
 					double lat = din.readDouble();
 					double lng = din.readDouble();
 					String txt = din.readUTF();
 					boolean isOnline = din.readBoolean();
 					publishProgress(login, lat, lng, txt, isOnline);
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 	
 	protected void onProgressUpdate(Object... status) {
 		if((Boolean)status[4]) {
 			Marker mrk = markers.get(status[0]);
 			if(mrk == null) {
 				mrk = (map.addMarker(new MarkerOptions()
 								.position(new LatLng((Double)status[1],(Double)status[2]))
 								.title((String)status[0])
 								.snippet((String)status[3])));
 				markers.put(mrk.getTitle(), mrk);
 				
 			}
 			else {
 				mrk.remove();
 				mrk = (map.addMarker(new MarkerOptions()
 				.position(new LatLng((Double)status[1],(Double)status[2]))
 				.title((String)status[0])
 				.snippet((String)status[3])));
 			}
 			
 			mrk.showInfoWindow();			    
 		}
 		else {
 			Marker mrk = markers.get(status[0]);
 			if(mrk != null) {
 				mrk.hideInfoWindow();
 				mrk.remove();
 				markers.remove(status[0]);
 			}
 		}
 		
 	}
 
 }
