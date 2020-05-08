 package com.shakshin.metar;
 
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.IBinder;
 
 public class UpdateService extends Service {
 	private String lastTemp, lastCond;
 	
 	private NotificationManager nm;
 	private Boolean started = false; 
 	private Notification ntf;
 	private Timer timer;
 	
 	public String stationName;
 	
 	public UpdateService service;
 	
 	public Hashtable<String, Integer> wx;
 	
 	@Override
 	public void onCreate() {
 		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		wx = new Hashtable<String, Integer>();
 		wx.put("VCFG", R.string.fog);
 		wx.put("FZFG", R.string.fog);
 		wx.put("MIFG", R.string.fog);
 		wx.put("PRFG", R.string.fog);
 		wx.put("FG", R.string.fog);
 		wx.put("BR", R.string.mist);
 		wx.put("HZ", R.string.mist);
 		wx.put("FU", R.string.smoke);
 		wx.put("DS", R.string.dstorm);
 		wx.put("SS", R.string.dstorm);
 		wx.put("DRSN", R.string.blsn);
 		wx.put("BLSN", R.string.blsn);
 		wx.put("RASN", R.string.rasn);
 		wx.put("SNRA", R.string.rasn);
 		wx.put("SHSN", R.string.shsn);
 		wx.put("SHRA", R.string.shra);
 		wx.put("DZ", R.string.rain);
 		wx.put("SG", R.string.snow);
 		wx.put("RA", R.string.rain);
 		wx.put("SN", R.string.snow);
 		wx.put("IC", R.string.snow);
 		wx.put("PL", R.string.rain);
 		wx.put("GS", R.string.snow);
 		wx.put("FZRA", R.string.rain);
 		wx.put("FZDZ", R.string.rain);
 		wx.put("TSRA", R.string.tsra);
 		wx.put("TSGR", R.string.tsgr);
 		wx.put("TSGS", R.string.tsgr);
 		wx.put("TSSN", R.string.tssn);
 		wx.put("TS", R.string.ts);
 		wx.put("GR", R.string.gr);
 	}
 	
 	public void onDestroy() {
 		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.cancel(123321);
 	}
 	
 	
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (started) {
 			return START_STICKY;
 		}
 		started = true;
 		
 		lastTemp = getString(R.string.no_data);
 		lastCond = getString(R.string.no_data);
 		
 		startForeground(123321, notification(getString(R.string.app_name), getString(R.string.no_data)));
 		
 		SharedPreferences settings = getSharedPreferences("com.shakshin.metar", 0);
 		stationName = settings.getString("station_name", "");
 
 		timer = new Timer();
 		timer.schedule(new UpdateTask(), 1000, settings.getInt("update_interval", 30) * 1000 * 60);
 		service = this;
 		return START_STICKY;
 	}
 		
 	
 	private class UpdateTask extends TimerTask {
 		
 		@Override
 		public void run() {
 			HttpClient http = new DefaultHttpClient();
 			String url = "http://www.jetplan.com/jeppesen/jsp/weather/aocTextWeather.jsp?icao=" + stationName + "&rt=METAR";
 			String html;
 			
 			HttpGet get = new HttpGet(url);
 			try {
 				html = http.execute(get, new BasicResponseHandler());				
 			}
 			catch (Exception e) {
 				notification(lastTemp, lastCond + " (" + getString(R.string.fetch_error) + ")");
 				return;
 			}
 			
 			Pattern p = Pattern.compile("<pre>METAR.+</pre>");
 			Matcher m = p.matcher(html.replaceAll("\n", "").replaceAll("<pre>METAR</pre>", ""));
 			
 			String[] metar;
 
 			if (m.find()) {
 				metar = m.group(0).replaceAll("<.?pre>", "").split(" ");
 			} else {
 				notification(lastTemp, lastCond + " (" + getString(R.string.wrong_data) + ")");
 				return;
 			}
 			
 			Integer i = 0;
 			Integer startPos = -1;
 			while (i < metar.length){
 				if (metar[i].equals(stationName)) {
 					startPos = i+1;
 					i = metar.length;
 				}
 				i++;
 			} 
 			if (startPos < 0) {
 				notification(lastTemp, lastCond + " (" + getString(R.string.wrong_data) + ")");
 				return;
 			}
 			
 			
 			i = startPos + 3;
 			String condition = "";
 			
 			
 			while (i < metar.length && !metar[i].matches("M?\\d{2}/M?\\d{2}.*")) {
 				String v = metar[i];
 				String c = "";
 				String c2 = "";
 				if (v.substring(0, 1).equals("-")) {
 					c = getString(R.string.light);
 					v = v.substring(1);
 				};
 				if (v.substring(0, 1).equals("+")) {
 					c = getString(R.string.heavy);
 					v = v.substring(1);
 				};
 				if (wx.containsKey(v)) {
 					c2 = getString(wx.get(v));
 				}
 				if (!c2.equals("")) {
 					if (condition.equals("")) {
 						condition = condition + c + " " + c2;
 					} else {
 						condition = condition + ", " + c + " " + c2;
 					}
 				}
 				i++;
 			}
 			
 			if (condition.equals("")) {
 				condition = getString(R.string.noprec);
 			}
 			
 			if (i >= metar.length) {
 				notification(lastTemp, lastCond + " (" + getString(R.string.wrong_data) + ")");
 				return;
 			}  
 			
 			String temp;
 			String sign = "";
 			if (metar[i].substring(0, 1).equals("M")) {
 				sign = "-";
 				temp = metar[i].substring(1, 3);
 			} else {
 				sign = "+";
 				temp = metar[i].substring(0, 2);
 			}
 			if (temp.substring(0, 1).equals("0")) {
 				temp = temp.substring(1, 2);
 			}
 				temp = sign + temp;
 			
 			lastTemp = temp;
 			lastCond = condition;
 			notification(temp, condition);
 		}
 	}
 	
 	public Notification notification(String temp, String condition) {
 		Notification nt = new Notification(R.drawable.ic_launcher, getString(R.string.app_name), System.currentTimeMillis());
		
 		nt.setLatestEventInfo(
 				this, 
 				getString(R.string.temperature) + ": " + temp, 
 				condition, 
 				PendingIntent.getActivity(
 						getApplicationContext(), 0, 
 						new Intent(this, SettingsActivity.class), 0
 				)
 		);
 		
 		this.nm.notify(123321, nt);
 		return nt;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return mBinder;
 	}
 	
 	public class LocalBinder extends Binder {
         UpdateService getService() {
             return UpdateService.this;
         }
     }
 	
 	private final IBinder mBinder = new LocalBinder();
 
 }
