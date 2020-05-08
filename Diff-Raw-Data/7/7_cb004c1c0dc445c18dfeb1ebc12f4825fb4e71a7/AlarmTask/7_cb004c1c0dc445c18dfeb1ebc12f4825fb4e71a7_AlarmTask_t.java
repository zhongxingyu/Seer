 /**
 *	AlarmTask.java
 *
 *	@author Johan
 */
 
 package se.chalmers.watchme.notifications;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 public class AlarmTask implements Runnable {
 	
 	private final Context ctx;
 	private final Calendar date;
 	private final AlarmManager manager;
 	
 	public AlarmTask(Context context, Calendar date) {
 		this.ctx = context;
 		this.date = date;
		
		// Get the Android alarm service
		this.manager = (AlarmManager) this.ctx.getSystemService(Context.ALARM_SERVICE);
 	}
 	
 	public void run() {
 		Intent intent = new Intent(this.ctx, NotifyService.class);
 		intent.putExtra(NotifyService.INTENT_NOTIFY, true);
 		
 		PendingIntent pending = PendingIntent.getService(this.ctx, 0, intent, 0);
 		
 		this.manager.set(AlarmManager.RTC, this.date.getTimeInMillis(), pending);
 	}
 
 }
