 package com.ipuweb.freezealarm;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.util.Log;
 
 public class FreezeAlarmService extends Service {
 	private AlarmManager alarmManager;
 	private static int INTERVAL_1DAY = 24 * 60 * 60 * 1000;
 	public void refreshAlarm(){
 		// 次回起動時刻を設定
 	    Intent intent = new Intent(this, AutoStartReceiver.class);
 	    intent.setAction("freezeAlarm");	// TODO 定数に変更        
 	    
 	    // すでにあるアラームをキャンセルして、再度登録
 	    // XXX:isseium もしかしたらこんなことしなくてもよいので誰かなおして
 		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		this.alarmManager = (AlarmManager)(this.getSystemService(ALARM_SERVICE));
 		this.alarmManager.cancel(sender);
 		sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		
 	    Calendar cal = Calendar.getInstance();
 	    
 	    // Sharedpreferencesから起動時刻を取得
 	    // TODO: 定数化
 	    SharedPreferences pref = getSharedPreferences("freezealarm", MODE_PRIVATE);
 	    String set_hour = pref.getString("hour", "0");
 	    String set_minute = pref.getString("minute", "0");
 	    cal.setTimeInMillis(System.currentTimeMillis());
 	    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(set_hour));
 	    cal.set(Calendar.MINUTE, Integer.parseInt(set_minute));
 	    cal.set(Calendar.SECOND, 0);
 //	    this.alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
 	    
 	    // 1日ごとに起動
 	    //this.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), FreezeAlarmService.INTERVAL_1DAY , sender);
	    //こっちのほうが電池効率がいいらしい
	    this.alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), FreezeAlarmService.INTERVAL_1DAY, sender);
 	    
 	    Log.d("FreezeAlram", "Set alarmmanager hour=" + set_hour + " minute=" + set_minute);
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		refreshAlarm();
 	}
 	
 }
