 package carnero.csms;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.widget.RemoteViews;
 import java.util.ArrayList;
 import java.util.Locale;
 
 public class base {
 	public static int BLACK = 0;
 	public static int WHITE = 1;
 
 	private final Locale loc = Locale.getDefault();
 
 	public void refresh(int skin, Context context) {
 		refresh(skin, context, null, null, null);
 	}
 
 	public void refresh(int skin, Context context, String address, String text) {
 		refresh(skin, context, null, address, text);
 	}
 
 	public void refresh(int skin, Context context, int[] ids) {
 		refresh(skin, context, ids, null, null);
 	}
 
 	public void refresh(int skin, Context context, int[] ids, String intentAddress, String intentText) {
 		RemoteViews views;
 
 		String person = null;
 		String address = null;
 		String text = null;
 
 		if (context == null) {
 			return;
 		}
 
 		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
 		if (skin == WHITE) {
 			views = new RemoteViews("carnero.csms", R.layout.layout_white);
 		} else {
 			views = new RemoteViews("carnero.csms", R.layout.layout_black);
 		}
 
 		try {
 			if (intentAddress == null || intentText == null) {
 				ArrayList<message> msgs = new ArrayList<message>();
 				int count = 0;
 
 				Cursor cursor = context.getContentResolver().query(
 						Uri.parse("content://sms/inbox"),
 						new String[] {"_id", "thread_id", "address", "person", "date", "body"},
 						null,
 						null,
 						"date desc"
 				);
 
 				if (cursor != null) {
 					try {
 						count = cursor.getCount();
 
 						if (count > 0) {
 							cursor.moveToFirst();
 
 							do {
 								message msg = new message();
 
 								msg.id = cursor.getLong(cursor.getColumnIndex("_id"));
 								msg.thread = cursor.getLong(cursor.getColumnIndex("thread_id"));
 								msg.address = cursor.getString(cursor.getColumnIndex("address"));
 								msg.date = cursor.getLong(cursor.getColumnIndex("date"));
 								msg.text = cursor.getString(cursor.getColumnIndex("body"));
 
 								msgs.add(msg);
 							} while (cursor.moveToNext() != false);
 						}
 					} finally {
 						cursor.close();
 					}
 				}
 
 				if (msgs.isEmpty() == false) {
 					for (message msg : msgs) {
 						Cursor cursorContact = context.getContentResolver().query(
 								Uri.withAppendedPath(
 										Uri.parse("content://com.android.contacts/phone_lookup"),
 										Uri.encode(msg.address)
 								),
 								new String[] {"_id", "display_name"},
 								null,
 								null,
 								null
 						);
 
 						if (cursorContact != null) {
 							try {
 								if (cursorContact.moveToFirst()) {
 									msg.person = cursorContact.getString(cursorContact.getColumnIndex("display_name"));
 								}
 							} finally {
 								cursorContact.close();
 							}
 						}
 					}
 
 					final message msg = msgs.get(0);
 
 					address = msg.address.toLowerCase(loc);
 					person = msg.person.toLowerCase(loc);
 					text = msg.text.toLowerCase(loc);
 				}
 			} else {
 				address = intentAddress.toLowerCase(loc);
 				text = intentText.toLowerCase(loc);
 
 				Cursor cursorContact = context.getContentResolver().query(
 						Uri.withAppendedPath(
 								Uri.parse("content://com.android.contacts/phone_lookup"),
 								Uri.encode(intentAddress)
 						),
 						new String[] {"_id", "display_name"},
 						null,
 						null,
 						null
 				);
 
 				if (cursorContact != null) {
 					try {
 						if (cursorContact.moveToFirst()) {
 							person = cursorContact.getString(cursorContact.getColumnIndex("display_name"));
 						}
 					} finally {
 						cursorContact.close();
 					}
 				}
 			}
 
 			// display sms info
 			if ((person != null || address != null) && text != null) {
 				if (person != null && person.length() > 0) {
 					views.setTextViewText(R.id.sender, person.toLowerCase(loc));
 				} else {
 					views.setTextViewText(R.id.sender, address.toLowerCase(loc));
 				}
 				views.setTextViewText(R.id.message, text.toLowerCase(loc));
 			} else {
 				views.setTextViewText(R.id.sender, null);
 				views.setTextViewText(R.id.message, null);
 			}
 
 			// set pendingintent on click
 			if (skin == WHITE) {
 				final Intent intentWid = new Intent(context, csms_white.class);
 				intentWid.setAction("csmsTouch");
				final PendingIntent intentPending = PendingIntent.getBroadcast(context,  0, intentWid, 0);
 				views.setOnClickPendingIntent(R.id.widget, intentPending);
 			} else {
 				final Intent intentWid = new Intent(context, csms_black.class);
 				intentWid.setAction("csmsTouch");
				final PendingIntent intentPending = PendingIntent.getBroadcast(context,  0, intentWid, 0);
 				views.setOnClickPendingIntent(R.id.widget, intentPending);
 			}
 
 			if (ids != null && ids.length > 0) {
 				final int idsCnt = ids.length;
 
 				for (int i = 0; i < idsCnt; i++) {
 					manager.updateAppWidget(ids[i], views);
 				}
 			} else {
 				if (skin == WHITE) {
 					final ComponentName component = new ComponentName(context, csms_white.class);
 					manager.updateAppWidget(component, views);
 				} else {
 					final ComponentName component = new ComponentName(context, csms_black.class);
 					manager.updateAppWidget(component, views);
 				}
 			}
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 }
