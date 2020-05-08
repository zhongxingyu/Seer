 /* This program is free software: you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, either version 3 of
  the License, or (props, at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>. */
 
 package org.openplans.rcavl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Looper;
 import android.util.Log;
 
 public class GpsService extends Service {
 
 	private GpsServiceThread thread;
 	private LocalBinder binder = new LocalBinder();
 	private RCAVL activity;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 
 		String url = intent.getStringExtra("pingUrl");
 		String email = intent.getStringExtra("email");
 		String password = intent.getStringExtra("password");
 		thread = new GpsServiceThread(url, email, password);
 		new Thread(thread).start();
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		thread.stop();
 	}
 
 	public class LocalBinder extends Binder {
 		GpsService getService() {
 			// Return this instance of LocalService so clients can call public
 			// methods
 			return GpsService.this;
 		}
 	}
 
 	class GpsServiceThread implements LocationListener, Runnable {
 		private final String TAG = GpsServiceThread.class.toString();
 		private String url;
 		private String password;
 		private String email;
 		private String status;
 		private volatile boolean active;
 		private Location lastLocation;
 		private LocationManager locationManager;
 
 		public GpsServiceThread(String url, String email, String password) {
 			this.url = url;
 			this.email = email;
 			this.password = password;
 			this.status = "active";
 			active = true;
 		}
 
 		public void stop() {
 			Looper looper = Looper.myLooper();
 			looper.quit();
 		}
 
 		public void onLocationChanged(Location location) {
 			if (!active) {
 				return;
 			}
 			lastLocation = location;
 			ping(location);
 		}
 
 		private void ping(Location location) {
 			HttpClient client = new DefaultHttpClient();
 			HttpPost request = new HttpPost(url);
 
 			try {
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
 						4);
 				nameValuePairs.add(new BasicNameValuePair("email", email));
 				nameValuePairs
 						.add(new BasicNameValuePair("password", password));
 
 				if (location != null) {
					nameValuePairs.add(new BasicNameValuePair("lat",
 							Double.toString(location.getLatitude())));
					nameValuePairs.add(new BasicNameValuePair("lng",
 							Double.toString(location.getLongitude())));
 				}
 				nameValuePairs.add(new BasicNameValuePair("status", status));
 				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 				@SuppressWarnings("unused")
 				HttpResponse response = client.execute(request);
 				/* would be nice to do something with the result here */
 
 			} catch (ClientProtocolException e) {
 				Log.e(TAG, "exception sending ping " + e);
 			} catch (IOException e) {
 				Log.e(TAG, "exception sending ping " + e);
 			}
 		}
 
 		public void onProviderDisabled(String provider) {
 			toast("provider disabled " + provider);
 		}
 
 		public void onProviderEnabled(String provider) {
 			toast("provider enabled " + provider);
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			toast("on status changed " + provider + " status = " + status);
 		}
 
 		public void toast(String message) {
 			if (!active) {
 				return;
 			}
 			if (activity != null) {
 				activity.toast(message);
 			}
 		}
 
 		public void run() {
 			Looper.prepare();
 
 			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 			locationManager.requestLocationUpdates(
 					LocationManager.GPS_PROVIDER, 0, 0, this);
 			Looper.loop();
 			Looper.myLooper().quit();
 		}
 
 		public void setStatus(String status) {
 			this.status = status;
 			ping(lastLocation);
 		}
 
 		public String getStatus() {
 			return status;
 		}
 
 		public void setActive(boolean active) {
 			this.active = active;
 			if (active) {
 				locationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, 0, 0, this);
 			} else {
 				locationManager.removeUpdates(this);
 			}
 		}
 
 		public boolean isActive() {
 			return active;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder;
 	}
 
 	public void setActivity(RCAVL activity) {
 		this.activity = activity;
 	}
 
 	public void setStatus(String status, boolean active) {
 		thread.setActive(active);
 		thread.setStatus(status);
 	}
 
 	public boolean isActive() {
 		return thread.isActive();
 	}
 
 }
