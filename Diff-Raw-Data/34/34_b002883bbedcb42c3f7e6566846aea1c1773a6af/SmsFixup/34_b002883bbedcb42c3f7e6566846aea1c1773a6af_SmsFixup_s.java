 /*
  *  Copyright (C) 2012  Andrea Arcangeli
  *
  *  This work is licensed under the terms of the GNU GPL, version 2. See
  *  the COPYING file in the top-level directory.
  */
 
 package com.aa.smsfailfix;
 
 import android.content.Context;
 import android.content.ContentValues;
 import android.util.Log;
 import android.net.Uri;
 
 public class SmsFixup {
 	private static final String reply_path_present = "reply_path_present";
 	private final static String TAG = "SmsFailFix";
 
 	static int smsFixup(Context context) {
		int nr;
 		ContentValues values = new ContentValues(1);
 		Log.d(TAG, "start fixup");
 		values.put(reply_path_present, 0);
		nr = context.getContentResolver().update(Uri.parse("content://sms/inbox"),
							 values,
							 reply_path_present + "!=" + "0",
							 null);
		Log.d(TAG, "end fixup, updated " + nr + " SMS");
 		return nr;
 	}
 }
