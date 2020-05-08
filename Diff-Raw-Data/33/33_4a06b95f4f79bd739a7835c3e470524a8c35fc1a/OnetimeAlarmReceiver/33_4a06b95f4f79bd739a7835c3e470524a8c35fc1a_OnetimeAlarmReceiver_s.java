 /**
  * @date: 2009-12-27
  * @fileName: SS.java
  * @Description: Hocine
  */
 package net.rhatec.amtmobile.notifications;
 
 import net.rhatec.amtmobile.R;
 import net.rhatec.amtmobile.constants.TypeString;
 import net.rhatec.amtmobile.dialog.HoraireDlg;
 import net.rhatec.amtmobile.helpers.StringHelpers;
 import net.rhatec.amtmobile.types.DateHelpers;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 
 /**
  * @author Hocine
  * @description:
  */
 public class OnetimeAlarmReceiver extends BroadcastReceiver
 {
 
 	private NotificationManager	m_NotificationManager;
 	private String				nameService;
 
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
 
 		nameService = Context.NOTIFICATION_SERVICE; // Pas obliger d<etre privee
 		// Get reference to the NotificationManager
 		m_NotificationManager = (NotificationManager) context.getSystemService(nameService);
 
 		int icon = R.drawable.icon;
 
 		Bundle b = intent.getExtras();
 
 		Time m_heurePassage = DateHelpers.obtenirObjetTimeActuel();// new
 																	// Time();
 		int delai = b.getInt(TypeString.DELAI);
 		m_heurePassage.minute += delai;
 		m_heurePassage.normalize(true);
 
 		// Titre en haut de la barre de notification
 		CharSequence tickerText;
 
 		if (DateFormat.is24HourFormat(context))
 			tickerText = m_heurePassage.format("%H:%M");
 		else
 			tickerText = m_heurePassage.format("%I:%M%p");
 
 		Notification notification = new Notification(icon, tickerText, m_heurePassage.toMillis(false));
 
 		CharSequence contentTitle = context.getString(R.string.app_name) + " " + b.getString(TypeString.SOCIETECODE).toUpperCase() + " " + b.getString(TypeString.NOARRET);
 		CharSequence contentText = b.getString(TypeString.NOCIRCUIT) + " " + StringHelpers.charDirectionToViewableString(b.getString(TypeString.DIRECTION), context) + " " + b.getString(TypeString.INTERSECTIONSCONCAT);
 		
 		Intent notificationIntent = new Intent(context, HoraireDlg.class);
 
 
 		notificationIntent.putExtras(intent.getExtras());
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		// Sons
 		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags = Notification.FLAG_INSISTENT;
 
 		// Lumieres
 		notification.defaults |= Notification.DEFAULT_LIGHTS;
 
 		// Vibration
 		notification.defaults |= Notification.DEFAULT_VIBRATE;
 
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 
 		m_NotificationManager.notify(Integer.parseInt(b.getString(TypeString.NOARRET)), notification);
 
 	}
 }
