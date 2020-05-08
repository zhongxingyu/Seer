 /*
  *  Copyright (C) 2012  Andrea Arcangeli
  *
  *  This work is licensed under the terms of the GNU GPL, version 2. See
  *  the COPYING file in the top-level directory.
  */
 
 package com.aa.smsfailfix;
 
 import android.content.Context;
import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.util.Log;
 import android.net.Uri;
import android.os.SystemClock;
import android.database.sqlite.SQLiteException;
 
 public class SmsFixup {
 	private static final String reply_path_present = "reply_path_present";
 	private final static String TAG = "SmsFailFix";
 
 	static int smsFixup(Context context) {
		int nr = 0;
 		ContentValues values = new ContentValues(1);
 		Log.d(TAG, "start fixup");
 		values.put(reply_path_present, 0);

		ContentResolver contentResolver = context.getContentResolver();
		int retries = 3;
		while (retries > 0) {
			try {
				//throw new SQLiteException("failed");
				nr = contentResolver.update(Uri.parse("content://sms/inbox"),
							    values,
							    reply_path_present + "!=" + "0",
							    null);
				retries = -1;
			} catch (SQLiteException e) {
				Log.d(TAG, "SQLiteException, retrying...");
				SystemClock.sleep(1000);
				retries--;
			}
		}
		if (retries == -1)
			Log.d(TAG, "end fixup, updated " + nr + " SMS");
		else
			Log.d(TAG, "end fixup, failed with SQLiteException");
 		return nr;
 	}
 }
