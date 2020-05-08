 package com.mridang.callstats;
 
 import java.util.Calendar;
 import java.util.Random;
 
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.provider.CallLog.Calls;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class CallstatsWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
 	 */
 	@Override
 	protected void onInitialize(boolean isReconnect) {
 
 		super.onInitialize(isReconnect);
 
 		if (!isReconnect) {
 
 			addWatchContentUris(new String[]{"content://call_log/calls"});
 
 		}
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("CallstatsWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "a83e054b");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
	protected void onUpdateData(int arg0) {
 
 		Log.d("CallstatsWidget", "Calculating call statistics");
 		ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(true);
 
 		try {
 
 			Log.d("CallstatsWidget", "Checking period that user has selected");
 			Calendar calCalendar = Calendar.getInstance();
 			calCalendar.set(Calendar.MINUTE, 0);
 			calCalendar.set(Calendar.HOUR, 0);
 			calCalendar.set(Calendar.SECOND, 0);
 			calCalendar.set(Calendar.MILLISECOND, 0);
 			calCalendar.set(Calendar.HOUR_OF_DAY, 0);
 
 			switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("period", "4"))) {
 
 			case 0: //Day
 				Log.d("CallstatsWidget", "Fetch calls for the day");
 				calCalendar.set(Calendar.HOUR_OF_DAY, 0);
 				break;
 
 			case 1: //Week
 				Log.d("CallstatsWidget", "Fetch calls for the week");
 				calCalendar.set(Calendar.DAY_OF_WEEK, calCalendar.getFirstDayOfWeek());
 				break;
 
 			case 2: //Month
 				Log.d("CallstatsWidget", "Fetch calls for the month");
 				calCalendar.set(Calendar.DAY_OF_MONTH, 1);
 				break;
 
 			case 3: //Year
 				Log.d("CallstatsWidget", "Fetch calls for the year");
 				calCalendar.set(Calendar.DAY_OF_YEAR, 1);
 				break;
 
 			default:
 				Log.d("CallstatsWidget", "Fetch all calls");
 				calCalendar.clear(); 
 				break;
 
 			}
 
 			Log.d("CallstatsWidget", "Querying the database to get the phonecalls since " + calCalendar.getTime());
 			String strClause = android.provider.CallLog.Calls.DATE + " >= ?";
 			String[] strValues = {String.valueOf(calCalendar.getTimeInMillis())};
 
 			Cursor curCalls = getContentResolver().query(Uri.parse("content://call_log/calls"), null, strClause, strValues, null);
 
 			Integer intIncoming = 0;
 			Integer intTotal = 0;
 			Integer intOutgoing = 0;
 
 			while (curCalls != null && curCalls.moveToNext()) {
 
 				switch (curCalls.getInt(curCalls.getColumnIndex(Calls.TYPE))) {
 
 				case Calls.INCOMING_TYPE:
 					intIncoming = intIncoming + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 					intTotal = intTotal + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 					break;
 
 				case Calls.OUTGOING_TYPE:
 					intOutgoing = intOutgoing + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 					intTotal = intTotal + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 					break;
 
 				}
 
 			}
 
 			String strIncoming = "";
 			String strTotal = "";
 			String strOutgoing = "";
 
 			intIncoming = intIncoming / 60;
 			if (intIncoming < 60) {
 				strIncoming = getResources().getQuantityString(
 						R.plurals.minutes, intIncoming, intIncoming);
 			} else {
 				strIncoming = getResources().getQuantityString(R.plurals.hours,
 						intIncoming / 60, intIncoming / 60);
 				if (intIncoming % 60 > 0) {
 					strIncoming = String.format(
 							getString(R.string.and),
 							strIncoming,
 							getResources().getQuantityString(R.plurals.minutes,
 									intIncoming % 60, intIncoming % 60));
 				}
 			}
 			Log.v("CallstatsWidget", "Incoming : " + intIncoming);
 			Log.d("CallstatsWidget", "Incoming : " + strIncoming);
 
 			intOutgoing = intOutgoing / 60;
 			if (intOutgoing < 60) {
 				strOutgoing = getResources().getQuantityString(
 						R.plurals.minutes, intOutgoing, intOutgoing);
 			} else {
 				strOutgoing = getResources().getQuantityString(R.plurals.hours,
 						intOutgoing / 60, intOutgoing / 60);
 				if (intOutgoing % 60 > 0) {
 					strOutgoing = String.format(
 							getString(R.string.and),
 							strOutgoing,
 							getResources().getQuantityString(R.plurals.minutes,
 									intOutgoing % 60, intOutgoing % 60));
 				}
 			}
 			Log.v("CallstatsWidget", "Outgoing : " + intOutgoing);
 			Log.d("CallstatsWidget", "Outgoing : " + strOutgoing);
 
 			intTotal = (intTotal - ((intIncoming % 60) + (intOutgoing % 60))) / 60;
 			if (intTotal < 60) {
 				strTotal = getResources().getQuantityString(
 						R.plurals.minutes, intTotal, intTotal);
 			} else {
 				strTotal = getResources().getQuantityString(R.plurals.hours,
 						intTotal / 60, intTotal / 60);
 				if (intTotal % 60 > 0) {
 					strTotal = String.format(
 							getString(R.string.and),
 							strTotal,
 							getResources().getQuantityString(R.plurals.minutes,
 									intTotal % 60, intTotal % 60));
 				}
 			}
 			Log.v("CallstatsWidget", "Total : " + intTotal);
 			Log.d("CallstatsWidget", "Total : " + strTotal);
 
 			edtInformation
 			.expandedBody((edtInformation.expandedBody() == null ? ""
 					: edtInformation.expandedBody() + "\n")
 					+ String.format(getString(R.string.incoming),
 							strIncoming));
 			edtInformation.status(String.format(
 					getString(R.string.total_calls), strTotal));
 			edtInformation
 			.expandedBody((edtInformation.expandedBody() == null ? ""
 					: edtInformation.expandedBody() + "\n")
 					+ String.format(getString(R.string.outgoing),
 							strOutgoing));
 
 			if (new Random().nextInt(5) == 0) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 
 					for (PackageInfo pkgPackage : mgrPackages.getInstalledPackages(0)) {
 
 						intExtensions = intExtensions + (pkgPackage.applicationInfo.packageName.startsWith("com.mridang.") ? 1 : 0); 
 
 					}
 
 					if (intExtensions > 1) {
 
 						edtInformation.visible(true);
 						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
 						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
 						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
 						setUpdateWhenScreenOn(true);
 
 					}
 
 				}
 
 			} else {
 				setUpdateWhenScreenOn(false);
 			}
 
 		} catch (Exception e) {
 			Log.e("CallstatsWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("CallstatsWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("CallstatsWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
