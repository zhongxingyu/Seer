 package com.mridang.messstats;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class MessstatsWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("MessstatsWidget", "Created");
		BugSenseHandler.initAndStartSession(this, "bc2d4ec1");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		Log.d("MessstatsWidget", "Calculating message statistics");
 		ExtensionData edtInformation = new ExtensionData();
 		edtInformation.visible(false);
 
 		try {
 
 			Log.d("MessstatsWidget", "Calculating SMS statistics");
 			Cursor curSmses = getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
 
 			Integer intSentSms = 0;
 			Integer intRecdSms = 0;
 			Integer intTotalSms = 0;
 
			while (curSmses != null && curSmses.moveToNext()) {
 
 				intTotalSms = intTotalSms + 1;
 
 				switch (curSmses.getInt(curSmses.getColumnIndex("type"))) {
 
 				case 1:
 					intRecdSms = intRecdSms + 1;
 					break;
 
 				case 2:
 					intSentSms = intSentSms + 1;
 					break;
 
 				}
 
 			}
 			
 			String strSentSms = getResources().getQuantityString(R.plurals.sms, intSentSms, intSentSms);
 			String strRecdSms = getResources().getQuantityString(R.plurals.sms, intRecdSms, intRecdSms);
 			String strTotalSms = getResources().getQuantityString(R.plurals.sms, intTotalSms, intTotalSms);
 
 			Log.d("MessstatsWidget", "Send SMSes: " + intSentSms);
 			Log.d("MessstatsWidget", "Received SMSs: " + intRecdSms);
 			Log.d("MessstatsWidget", "Total SMSes: " + intTotalSms);
 
 			Log.d("MessstatsWidget", "Calculating MMS statistics");
 			Cursor curMmses = getContentResolver().query(Uri.parse("content://mms/"), null, null, null, null);
 
 			Integer intSentMms = 0;
 			Integer intRecdMms = 0;
 			Integer intTotalMms = 0;
 
			while (curMmses != null && curMmses.moveToNext()) {
 
 				intTotalMms = intTotalMms + 1;
 
 				switch (curMmses.getInt(curMmses.getColumnIndex("msg_box"))) {
 
 				case 2:
 					intRecdMms = intRecdMms + 1;
 					break;
 
 				case 1:
 					intSentMms = intSentMms + 1;
 					break;
 
 				}
 
 			}
 			
 			String strSentMms = getResources().getQuantityString(R.plurals.mms, intSentMms, intSentMms);
 			String strRecdMms = getResources().getQuantityString(R.plurals.mms, intRecdMms, intRecdMms);
 			String strTotalMms = getResources().getQuantityString(R.plurals.mms, intTotalMms, intTotalMms);
 			
 			Log.d("MessstatsWidget", "Send MMSes: " + intSentMms);
 			Log.d("MessstatsWidget", "Received MMSes: " + intRecdMms);
 			Log.d("MessstatsWidget", "Total MMSes: " + intTotalMms);
 
 			edtInformation
 					.expandedBody((edtInformation.expandedBody() == null ? ""
 							: edtInformation.expandedBody() + "\n")
 							+ String.format(getString(R.string.sent), String
 									.format(getString(R.string.and),
 											strSentSms, strSentMms)));
 
 			edtInformation.status(String.format(getString(R.string.messages),
 					String.format(getString(R.string.and), strTotalSms,
 							strTotalMms)));
 
 			edtInformation
 			.expandedBody((edtInformation.expandedBody() == null ? ""
 					: edtInformation.expandedBody() + "\n")
 					+ String.format(getString(R.string.recd), String
 							.format(getString(R.string.and),
 									strRecdSms, strRecdMms)));
 			
 			edtInformation.visible(true);
 
 		} catch (Exception e) {
 			Log.e("MessstatsWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}	
 		
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("MessstatsWidget", "Done");
 
 	}
 	
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("MessstatsWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
