 package de.proquariat.flokati;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.os.IBinder;
 import android.os.Message;
 import android.widget.Toast;
 import de.proquariat.flokati.R;
 
 
 public class RemoteConnectionService extends Service {
 
 	Vector<FlokatiDevice> devices;
 	
 	private InetAddress flokatiGroup=null;
 	
 	DeviceControlServerThread controlThread=null;
 	InformationServerThread infoThread=null;
 	
 	private WifiManager.MulticastLock multicast_lock=null;
 	
 	static final int NOTIFICATION = 7237918;
 	
 	class InformationServerThread implements Runnable {
 		public MulticastSocket sock;
 		private volatile ArrayList<DeviceRequest> requests = new ArrayList<DeviceRequest>(5);
 		
 		public InformationServerThread(MulticastSocket sock) {
 			this.sock=sock;
 		}
 		public void run() {
 			byte[] buffer = new byte[4096];
			DatagramPacket p = new DatagramPacket(buffer, 4096);
 			while (this.sock!=null){
 				try {
 					this.sock.receive(p);
 				} catch (IOException e) {
 					//e.printStackTrace();
 					this.sock=null;
 					return;
 				}
 				if (this.requests.size()>0) {
 					JSONObject info;
 					try {
						info=new JSONObject(new String(buffer));
 						//TODO: Can we make this more efficient?
 					} catch (JSONException e) {
 						e.printStackTrace();
 						continue;
 					}
 					
 					// not every json-implementation supports long types
 					long family=0;
 					try {
 						family = Long.parseLong(info.optString("family", "0"), 16);
 					} catch (NumberFormatException e) {	}
 					long mask = 0;
 					for (int i=0; i<64; i++) {
 						mask<<=1;
 						if (i<info.optInt("mask",64))
 							mask+=1;
 					}
 					
 					DeviceRequest orig=null;
 					for (DeviceRequest rq: requests) {
 						if ((rq.id & mask) == family) {
 							orig=rq;
 							break;
 						}
 					}
 					if (orig==null) {
 						continue;
 					}
 					
 					String language = FlokatiApp.connection.getResources().
 							getConfiguration().locale.getLanguage();
 					
 					JSONObject locNames = info.optJSONObject("names");
 					String devName="";
 					if (locNames!=null) devName=locNames.optString(language);
 					if (devName.equals("")) devName=info.optString("name");
 					if (devName.equals("")) continue;
 					
 					JSONArray sinks = info.optJSONArray("sinks");
 					
 					FlokatiDevice dev = new FlokatiDevice(sinks.length(), 
 							orig.id, devName);
 					
 					for (int i=0; i<sinks.length(); i++) {
 						JSONObject sink = sinks.optJSONObject(i);
 						if (sink!=null) {
 							JSONObject sinkLocales = sink.optJSONObject("names");
 							String sinkName="";
 								if (sinkLocales!=null) sinkName=sinkLocales.optString(language);
 							if (sinkName.equals("")) sinkName=sink.optString("name");
 							dev.setControl(sink.optInt("id"), new FlokatiDeviceControl(
 								dev, sink.optInt("id"), sinkName, sink.optString("type")));
 						}
 					}
 					
 					if (dev.isComplete()) {
 						Iterator<DeviceRequest> i = requests.iterator();
 						while (i.hasNext()) {
 							DeviceRequest rq = i.next();
 							if (rq.id == dev.id) {
 								dev.updateFromPacketBytes(rq.packet, rq.offset);
 								i.remove();
 							}
 						}
 						
 						Message m = new Message();
 						m.arg1=FlokatiApp.FROMSOCKET;
 						m.obj=dev;
 						FlokatiApp.infoSocketHandler.sendMessage(m);
 					}
 				}
 			}
 		}
 		public void sendPacket(DeviceRequest request) {
 			if (this.sock!=null && flokatiGroup!=null) {
 				JSONObject jsonRq = new JSONObject();
 				try {
 					// not every json implementation supports long types
 					// so we send long values as hex-encoded strings
 					jsonRq.accumulate("request", Long.toHexString(request.id));
 				} catch (JSONException e1) {
 					return;
 				}
 				
 				if (this.requests.size()>=5) this.requests.remove(0);
 				this.requests.add(request);
 				
 				try {
 					String rqString = jsonRq.toString();
 					this.sock.send(new DatagramPacket(rqString.getBytes(), rqString.length(), flokatiGroup, 5332));
 				} catch (IOException e) {
 					return;
 				}
 				
 			}
 		}
 	}
 
 	class DeviceControlServerThread implements Runnable {
 		public MulticastSocket sock;
 		
 		public DeviceControlServerThread(MulticastSocket sock) {
 			this.sock=sock;
 		}
 		public void run() {
 			byte[] buffer = new byte[33];
 			DatagramPacket p = new DatagramPacket(buffer, 33);
 			while (this.sock!=null){
 				try {
 					this.sock.receive(p);
 				} catch (IOException e) {
 					//e.printStackTrace();
 					return;
 				}
 				Message m = new Message();
 				m.arg1=FlokatiApp.FROMSOCKET;
 				m.obj=buffer;
 				FlokatiApp.controlSocketHandler.sendMessage(m);
 			}
 		}
 		public void sendPacket(byte[] packet) {
 			if (this.sock!=null && flokatiGroup!=null) {
 				try {
 					this.sock.send(new DatagramPacket(packet, 33, flokatiGroup, 5331));
 				} catch (IOException e) {
 //					Log.v("RemoteConnection", "Problems while sending packet");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void showNotification() {
 			NotificationManager mNM = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
 			Notification notification = new Notification(R.drawable.ic_launcher, 
 					getString(R.string.wifi_activated), System.currentTimeMillis());
 			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 	                new Intent(this, FlokatiActivity.class), 0);
 			notification.setLatestEventInfo(this, getString(R.string.app_name),
 	                getString(R.string.wifi_active), contentIntent);
 			mNM.notify(NOTIFICATION, notification);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 //		Log.v("service","service started");
 		if (this.controlThread==null && this.infoThread==null) {
 			NetworkInfo info = 
 					((ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE)).
 					getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 			
 			if (info.isConnected()) {
 				MulticastSocket controlSock, infoSock;
 				
 				try {
 					controlSock=new MulticastSocket(5331);
 					infoSock=new MulticastSocket(5332);
 				} catch (IOException e) {
 					Toast.makeText(this, 
 							getString(R.string.errors_mcast_nosocket), 
 							Toast.LENGTH_SHORT).show();
 					return START_STICKY;
 				}
 				
 				try {
 					this.flokatiGroup=InetAddress.getByName("233.133.133.133");
 				} catch (UnknownHostException e1) {
 					Toast.makeText(this, 
 							getString(R.string.errors_mcast_unknhost), 
 							Toast.LENGTH_SHORT).show();
 					return START_STICKY;
 				}
 				
 				try {
 					controlSock.joinGroup(this.flokatiGroup);
 					infoSock.joinGroup(this.flokatiGroup);
 				} catch (IOException e) {
 					Toast.makeText(this, 
 							getString(R.string.errors_mcast_join), 
 							Toast.LENGTH_SHORT).show();
 					return START_STICKY;
 				}
 				
 				try {
 					controlSock.setLoopbackMode(true); // true disables loopback mode
 					infoSock.setLoopbackMode(true);
 				} catch (SocketException e) {
 					Toast.makeText(this, 
 							getString(R.string.errors_mcast_loopback), 
 							Toast.LENGTH_SHORT).show();
 					return START_STICKY;
 				}
 				// point of no return
 				
 			    WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 			    multicast_lock = wm.createMulticastLock("FlokatiMcast");
 			    multicast_lock.acquire();
 			    
 			    this.showNotification();
 				
 				controlThread=new DeviceControlServerThread(controlSock);
 				new Thread(controlThread).start();
 				
 				infoThread=new InformationServerThread(infoSock);
 				new Thread(infoThread).start();
 				
 			} else {
 				// Multicast only works in a LAN, so it is useless to open a
 				// Multicast socket when only 3G is available
 				Toast.makeText(this, getString(R.string.errors_mcast_nowifi), Toast.LENGTH_SHORT).show();
 				return START_STICKY;
 			}
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 	
 
 	public void stopConnection() {
 		if (this.controlThread!=null) {
 			this.controlThread.sock=null;
 			this.controlThread=null;
 		}
 		if (this.infoThread!=null) {
 			this.infoThread.sock=null;
 			this.infoThread=null;
 		}
 		while (this.multicast_lock.isHeld())
 			this.multicast_lock.release();
 		NotificationManager mNM = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
 		mNM.cancel(NOTIFICATION);
 		stopSelf();
 	}
 	
 	public boolean getWLANState() {
 		if (this.controlThread==null || this.controlThread.sock==null)
 			return false;
 		else
 			return true;
 	}
 
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		FlokatiApp.connection=this;
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		this.stopConnection();		
 		FlokatiApp.connection=null;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
