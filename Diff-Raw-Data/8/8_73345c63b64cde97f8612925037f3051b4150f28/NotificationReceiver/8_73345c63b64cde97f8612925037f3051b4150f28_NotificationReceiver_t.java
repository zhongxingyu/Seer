 /**
  * Copyright (C) 2011 Joseph Lehner <joseph.c.lehner@gmail.com>
  *
  * This file is part of RxDroid.
  *
  * RxDroid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * RxDroid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with RxDroid.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  */
 
 package at.jclehner.rxdroid;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import at.caspase.rxdroid.Fraction;
 import at.jclehner.androidutils.EventDispatcher;
 import at.jclehner.rxdroid.Settings.DoseTimeInfo;
 import at.jclehner.rxdroid.db.Database;
 import at.jclehner.rxdroid.db.Drug;
 import at.jclehner.rxdroid.db.Entries;
 import at.jclehner.rxdroid.db.Schedule;
 import at.jclehner.rxdroid.util.DateTime;
 import at.jclehner.rxdroid.util.Util;
 
 public class NotificationReceiver extends BroadcastReceiver
 {
 	private static final String TAG = NotificationReceiver.class.getName();
 	private static final boolean LOGV = false;
 
 	private static final Class<?>[] EVENT_HANDLER_ARG_TYPES = { Date.class, Integer.TYPE };
 
 	public interface OnDoseTimeChangeListener
 	{
 		void onDoseTimeBegin(Date date, int doseTime);
 		void onDoseTimeEnd(Date date, int doseTime);
 	}
 
 	static final String EXTRA_SILENT = "at.jclehner.rxdroid.extra.SILENT";
 	static final String EXTRA_DATE = "at.jclehner.rxdroid.extra.DATE";
 	static final String EXTRA_DOSE_TIME = "at.jclehner.rxdroid.extra.DOSE_TIME";
 	static final String EXTRA_IS_DOSE_TIME_END = "at.jclehner.rxdroid.extra.IS_DOSE_TIME_END";
 	static final String EXTRA_IS_ALARM_REPETITION = "at.jclehner.rxdroid.extra.IS_ALARM_REPETITION";
 	static final String EXTRA_FORCE_UPDATE = "at.jclehner.rxdroid.extra.FORCE_UPDATE";
 
 	private static int NOTIFICATION_NORMAL = 0;
 	private static int NOTIFICATION_FORCE_UPDATE = 1;
 	private static int NOTIFICATION_FORCE_SILENT = 2;
 
 	private Context mContext;
 	private AlarmManager mAlarmMgr;
 
 	private List<Drug> mAllDrugs;
 
 	private boolean mDoPostSilent = false;
 	private boolean mForceUpdate = false;
 
 	private static final EventDispatcher<OnDoseTimeChangeListener> sEventMgr =
 			new EventDispatcher<OnDoseTimeChangeListener>();
 
 	public static void registerOnDoseTimeChangeListener(OnDoseTimeChangeListener l) {
 		sEventMgr.register(l);
 	}
 
 	public static void unregisterOnDoseTimeChangeListener(OnDoseTimeChangeListener l) {
 		sEventMgr.unregister(l);
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
 		if(intent == null)
 			return;
 
 		Database.init();
 
		final boolean isAlarmRepetition = intent.getBooleanExtra(EXTRA_IS_ALARM_REPETITION, false);

 		final int doseTime = intent.getIntExtra(EXTRA_DOSE_TIME, Schedule.TIME_INVALID);
 		if(doseTime != Schedule.TIME_INVALID)
 		{
			if(!isAlarmRepetition)
 			{
 				final Date date = (Date) intent.getSerializableExtra(EXTRA_DATE);
 				final boolean isDoseTimeEnd = intent.getBooleanExtra(EXTRA_IS_DOSE_TIME_END, false);
 				final String eventName = isDoseTimeEnd ? "onDoseTimeEnd" : "onDoseTimeBegin";
 
 				sEventMgr.post(eventName, EVENT_HANDLER_ARG_TYPES, date, doseTime);
 			}
 		}
 
 		mContext = context;
 		mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		mDoPostSilent = intent.getBooleanExtra(EXTRA_SILENT, false);
		mForceUpdate = isAlarmRepetition ? true : intent.getBooleanExtra(EXTRA_FORCE_UPDATE, false);
 		mAllDrugs = Database.getAll(Drug.class);
 
 		rescheduleAlarms();
 		updateCurrentNotifications();
 	}
 
 	private void rescheduleAlarms()
 	{
 		cancelAllAlarms();
 		scheduleNextAlarms();
 	}
 
 	private void scheduleNextAlarms()
 	{
 		if(Settings.getDoseTimeBegin(Drug.TIME_MORNING) == null)
 		{
 			Log.w(TAG, "No dose-time settings available. Not scheduling alarms.");
 			return;
 		}
 
 		if(LOGV) Log.i(TAG, "Scheduling next alarms...");
 
 		final DoseTimeInfo dtInfo = Settings.getDoseTimeInfo();
 
 		if(dtInfo.activeDoseTime() != Schedule.TIME_INVALID)
 			scheduleNextBeginOrEndAlarm(dtInfo, true);
 		else
 			scheduleNextBeginOrEndAlarm(dtInfo, false);
 	}
 
 	private void updateCurrentNotifications()
 	{
 		final DoseTimeInfo dtInfo = Settings.getDoseTimeInfo();
 		final boolean isActiveDoseTime;
 
 		Date date = dtInfo.activeDate();
 		int doseTime = dtInfo.activeDoseTime();
 
 		if(doseTime == Schedule.TIME_INVALID)
 		{
 			isActiveDoseTime = false;
 			doseTime = dtInfo.nextDoseTime();
 			date = dtInfo.nextDoseTimeDate();
 		}
 		else
 			isActiveDoseTime = true;
 
 		final int mode;
 
 		if(mForceUpdate)
 			mode = NOTIFICATION_FORCE_UPDATE;
 		else if(mDoPostSilent)
 			mode = NOTIFICATION_FORCE_SILENT;
 		else
 			mode = NOTIFICATION_NORMAL;
 
 		updateNotification(date, doseTime, isActiveDoseTime, mode);
 	}
 
 	private void scheduleNextBeginOrEndAlarm(DoseTimeInfo dtInfo, boolean scheduleEnd)
 	{
 		final int doseTime = scheduleEnd ? dtInfo.activeDoseTime() : dtInfo.nextDoseTime();
 		final Calendar time = dtInfo.currentTime();
 		final Date doseTimeDate = scheduleEnd ? dtInfo.activeDate() : dtInfo.nextDoseTimeDate();
 
 		final Bundle alarmExtras = new Bundle();
 		alarmExtras.putSerializable(EXTRA_DATE, doseTimeDate);
 		alarmExtras.putInt(EXTRA_DOSE_TIME, doseTime);
 		alarmExtras.putBoolean(EXTRA_IS_DOSE_TIME_END, scheduleEnd);
 		alarmExtras.putBoolean(EXTRA_SILENT, false);
 
 		long offset;
 
 		if(scheduleEnd)
 			offset = Settings.getMillisUntilDoseTimeEnd(time, doseTime);
 		else
 			offset = Settings.getMillisUntilDoseTimeBegin(time, doseTime);
 
 		long triggerAtMillis = time.getTimeInMillis() + offset;
 
 		final long alarmRepeatMins = Settings.getStringAsInt(Settings.Keys.ALARM_REPEAT, 0);
 		final long alarmRepeatMillis = alarmRepeatMins == -1 ? 10000 : alarmRepeatMins * 60000;
 
 		if(alarmRepeatMillis > 0)
 		{
 			alarmExtras.putBoolean(EXTRA_FORCE_UPDATE, true);
 
 			final long base = dtInfo.activeDate().getTime();
 			int i = 0;
 
 			while(base + (i * alarmRepeatMillis) < time.getTimeInMillis())
 				++i;
 
 			// We must tell the receiver whether the alarm is an actual dose time's
 			// end or begin, or merely a repetition.
 
 			final long triggerAtMillisWithRepeatedAlarm = base + i * alarmRepeatMillis;
 			if(triggerAtMillisWithRepeatedAlarm < triggerAtMillis)
 			{
 				triggerAtMillis = triggerAtMillisWithRepeatedAlarm;
 				alarmExtras.putBoolean(EXTRA_IS_ALARM_REPETITION, true);
 			}
 
 			triggerAtMillis = base + (i * alarmRepeatMillis);
 		}
 
 		final long triggerDiffFromNow = triggerAtMillis - System.currentTimeMillis();
 		if(triggerDiffFromNow < 0)
 		{
 			// 5 seconds should be more than enough to prevent FCs if this function is run
 			// just milliseconds before a dose time's begin or end.
 
 			if(triggerDiffFromNow < -50000)
 				throw new IllegalStateException("Alarm time is in the past: " + DateTime.toString(time));
 
 			Log.w(TAG, "Alarm time is in the past by less than 5 seconds. Ignoring...");
 		}
 
 		if(alarmExtras.getBoolean(EXTRA_IS_ALARM_REPETITION))
 			Log.i(TAG, "Scheduling next alarm for " + DateTime.toString(triggerAtMillis));
 		else
 		{
 			Log.i(TAG, "Scheduling " + (scheduleEnd ? "end" : "begin") + " of doseTime " +
 					doseTime + " on date " + DateTime.toDateString(doseTimeDate) + " for " +
 					DateTime.toString(triggerAtMillis));
 		}
 
 		Log.i(TAG, "Alarm will go off in " + Util.millis(triggerDiffFromNow));
 
 		mAlarmMgr.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, createOperation(alarmExtras));
 	}
 
 	private void cancelAllAlarms()
 	{
 		if(LOGV) Log.i(TAG, "Cancelling all alarms...");
 		mAlarmMgr.cancel(createOperation(null));
 	}
 
 	private PendingIntent createOperation(Bundle extras)
 	{
 		Intent intent = new Intent(mContext, NotificationReceiver.class);
 		intent.setAction(Intent.ACTION_MAIN);
 
 		if(extras != null)
 			intent.putExtras(extras);
 
 		return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 	}
 
 	private PendingIntent createDrugListIntent(Date date)
 	{
 		final Intent intent = new Intent(mContext, DrugListActivity.class);
 		intent.putExtra(DrugListActivity.EXTRA_STARTED_FROM_NOTIFICATION, true);
 		intent.putExtra(DrugListActivity.EXTRA_DATE, date);
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 
 		return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 	}
 
 	public void updateNotification(Date date, int doseTime, boolean isActiveDoseTime, int mode)
 	{
 		final List<Drug> drugsWithLowSupplies = new ArrayList<Drug>();
 		final int lowSupplyDrugCount = getDrugsWithLowSupplies(date, doseTime, drugsWithLowSupplies);
 		final int missedDoseCount = getDrugsWithMissedDoses(date, doseTime, isActiveDoseTime, null);
 		final int dueDoseCount = isActiveDoseTime ? getDrugsWithDueDoses(date, doseTime, null) : 0;
 
 		int titleResId = R.string._title_notification_doses;
 		int icon = R.drawable.ic_stat_normal;
 
 		final StringBuilder sb = new StringBuilder();
 
 		if(missedDoseCount != 0 || dueDoseCount != 0)
 		{
 			if(dueDoseCount != 0)
 				sb.append(RxDroid.getQuantityString(R.plurals._qmsg_due, dueDoseCount));
 
 			if(missedDoseCount != 0)
 			{
 				if(sb.length() != 0)
 					sb.append(", ");
 
 				sb.append(RxDroid.getQuantityString(R.plurals._qmsg_missed, missedDoseCount));
 			}
 		}
 
 		if(lowSupplyDrugCount != 0)
 		{
 			icon = R.drawable.ic_stat_exclamation;
 
 			if(sb.length() == 0)
 			{
 				titleResId = R.string._title_notification_low_supplies;
 
 				final String first = drugsWithLowSupplies.get(0).getName();
 
 				if(lowSupplyDrugCount == 1)
 					sb.append(getString(R.string._qmsg_low_supply_single, first));
 				else
 				{
 					final String second = drugsWithLowSupplies.get(1).getName();
 					sb.append(RxDroid.getQuantityString(R.plurals._qmsg_low_supply_multiple, lowSupplyDrugCount, first, second));
 				}
 			}
 		}
 
 		final String message = sb.toString();
 		final int currentHash = message.hashCode();
 		final int lastHash = Settings.getInt(Settings.Keys.LAST_MSG_HASH);
 
 		final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
 		builder.setContentTitle(getString(titleResId));
 		builder.setContentIntent(createDrugListIntent(date));
 		builder.setContentText(message);
 		builder.setTicker(getString(R.string._msg_new_notification));
 		builder.setSmallIcon(icon);
 		builder.setOngoing(true);
 		builder.setUsesChronometer(false);
 		builder.setWhen(0);
 
 //		final long offset;
 //
 //		if(isActiveDoseTime)
 //			offset = Settings.getDoseTimeBeginOffset(doseTime);
 //		else
 //			offset = Settings.getTrueDoseTimeEndOffset(doseTime);
 //
 //		builder.setWhen(date.getTime() + offset);
 
 		if(mode == NOTIFICATION_FORCE_UPDATE || currentHash != lastHash)
 		{
 			builder.setOnlyAlertOnce(false);
 			Settings.putInt(Settings.Keys.LAST_MSG_HASH, currentHash);
 		}
 		else
 			builder.setOnlyAlertOnce(true);
 
 		int defaults = 0;
 
 		if(Settings.getBoolean(Settings.Keys.USE_LED, true))
 		{
 //			builder.setLights(0xff0000ff, 200, 800);
 			defaults |= Notification.DEFAULT_LIGHTS;
 		}
 
 		if(mode != NOTIFICATION_FORCE_SILENT && Settings.getBoolean(Settings.Keys.USE_SOUND, true))
 		{
 			final String ringtone = Settings.getString(Settings.Keys.NOTIFICATION_SOUND);
 			if(ringtone != null)
 				builder.setSound(Uri.parse(ringtone));
 			else
 				defaults |= Notification.DEFAULT_SOUND;
 		}
 
 		if(mode != NOTIFICATION_FORCE_SILENT && Settings.getBoolean(Settings.Keys.USE_VIBRATOR, true))
 			defaults |= Notification.DEFAULT_VIBRATE;
 
 		builder.setDefaults(defaults);
 
 		final NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
 
 		if(message.length() != 0)
 			nm.notify(R.id.notification, builder.build());
 		else
 			nm.cancel(R.id.notification);
 	}
 
 	private int getDrugsWithDueDoses(Date date, int doseTime, List<Drug> outDrugs)
 	{
 		int count = 0;
 
 		for(Drug drug: mAllDrugs)
 		{
 			final Fraction dose = drug.getDose(doseTime, date);
 
 			if(!drug.isActive() || dose.isZero() || drug.isAutoAddIntakesEnabled() || drug.getRepeatMode() == Drug.REPEAT_ON_DEMAND)
 				continue;
 
 			if(Entries.countIntakes(drug, date, doseTime) == 0)
 			{
 				++count;
 
 				if(outDrugs != null)
 					outDrugs.add(drug);
 			}
 		}
 
 		return count;
 	}
 
 	private int getDrugsWithMissedDoses(Date date, int activeOrNextDoseTime, boolean isActiveDoseTime, List<Drug> outDrugs)
 	{
 		if(!isActiveDoseTime && activeOrNextDoseTime == Drug.TIME_MORNING)
 		{
 			Log.d(TAG, "Next dose time is morning on " + DateTime.toDateString(date));
 
 			date = DateTime.add(date, Calendar.DAY_OF_MONTH, -1);
 			return getDrugsWithDueDoses(date, Drug.TIME_NIGHT, outDrugs);
 		}
 
 		int count = 0;
 
 		for(int doseTime = Schedule.TIME_MORNING; doseTime != activeOrNextDoseTime; ++doseTime)
 			count += getDrugsWithDueDoses(date, doseTime, outDrugs);
 
 		return count;
 	}
 
 	private int getDrugsWithLowSupplies(Date date, int doseTime, List<Drug> outDrugs)
 	{
 		int count = 0;
 
 		for(Drug drug : mAllDrugs)
 		{
 			if(Settings.hasLowSupplies(drug))
 			{
 				++count;
 
 				if(outDrugs != null)
 					outDrugs.add(drug);
 			}
 		}
 
 		return count;
 	}
 
 	private String getString(int resId, Object... formatArgs) {
 		return mContext.getString(resId, formatArgs);
 	}
 
 	/* package */ static void rescheduleAlarmsAndUpdateNotification(boolean silent) {
 		rescheduleAlarmsAndUpdateNotification(null, silent);
 	}
 
 	/* package */ static void rescheduleAlarmsAndUpdateNotification(Context context, boolean silent)
 	{
 		if(context == null)
 			context = RxDroid.getContext();
 		final Intent intent = new Intent(context, NotificationReceiver.class);
 		intent.setAction(Intent.ACTION_MAIN);
 		intent.putExtra(NotificationReceiver.EXTRA_SILENT, silent);
 		context.sendBroadcast(intent);
 	}
 }
