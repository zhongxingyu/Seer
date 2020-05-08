 package com.putasticker.providers;
 
 import com.putasticker.R;
 import com.putasticker.SavedStickActivity;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TaskStackBuilder;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.support.v4.app.NotificationCompat;
 
 public class Sticker implements BaseColumns {
 
 	public static final Uri CONTENT_URI = Uri.parse("content://"
 			+ StickerContentProvider.Authority + "/sticker");
 	public static final String CONTENT_TYPE = "com.putasticker.providers.sticker";
 	public static final String ID = "_id";
 	public static final String SUBJECT = "subject";
 	public static final String TEXT = "text";
 	public static final String[] projection = { ID, SUBJECT, TEXT };
 	public static final long NotificationTime = 1000*60*60;
 
 	private long id;
 	private String subject;
 	private String text;
 	private int notification_id;
 	private boolean isChanged;
 	private ContentResolver resolver;
 	
 	public static int createStricker(String subject, String text, ContentResolver cr, Context context)
 	{
 		Uri newUri;
 		ContentValues newValues = new ContentValues();
 		newValues.put(Sticker.SUBJECT, subject);
 		newValues.put(Sticker.TEXT, text);
 	
 		newUri = cr.insert(Sticker.CONTENT_URI, newValues);
 		int id = Integer.parseInt(newUri.getLastPathSegment());
 		createNotification(subject, text, context, id);
 		return id;
 	}
 	
 	private static void createNotification(String subject, String text, Context context, int id)
 	{
 		NotificationCompat.Builder mBuilder =
 		        new NotificationCompat.Builder(context)
 		        .setSmallIcon(R.drawable.ic_launcher)
 		        .setContentTitle(subject)
		        .setContentText(text)
		        .setWhen(Sticker.NotificationTime);
 		// Creates an explicit intent for an Activity in your app
 		Intent resultIntent = new Intent(context, SavedStickActivity.class);
 		resultIntent.putExtra(Sticker.ID, Integer.toString(id));
 
 		// The stack builder object will contain an artificial back stack for the
 		// started Activity.
 		// This ensures that navigating backward from the Activity leads out of
 		// your application to the Home screen.
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
 		// Adds the back stack for the Intent (but not the Intent itself)
 		stackBuilder.addParentStack(SavedStickActivity.class);
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent =
 		        stackBuilder.getPendingIntent(
 		            0,
 		            PendingIntent.FLAG_UPDATE_CURRENT
 		        );
 		mBuilder.setContentIntent(resultPendingIntent);
 		NotificationManager mNotificationManager =
 		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		// mId allows you to update the notification later on.
 		mNotificationManager.notify(id, mBuilder.build());
 	}
 	public Sticker(long id, ContentResolver cr) {
 		super();
 		resolver = cr;
 		if (id > 0) {
 			Cursor c = resolver.query(CONTENT_URI, projection, ID + " = " + id, null,
 					ID + " ASC");
 			if (c.moveToFirst()) {
 				subject = c.getString(c.getColumnIndex(SUBJECT));
 				text = c.getString(c.getColumnIndex(TEXT));
 			}
 		}
 		this.id = id;
 	}
 	
 	public Sticker(Cursor c)
 	{
 		if (!c.isBeforeFirst() && !c.isAfterLast())
 		{
 			subject = c.getString(c.getColumnIndex(SUBJECT));
 			text = c.getString(c.getColumnIndex(TEXT));
 		}
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 	
 	public void setSubject(String sub) {
 		subject = sub;
 		isChanged =true;
 	}
 	
 	public String getText() {
 		return text;
 	}
 	
 	public void setText(String t)
 	{
 		text = t;
 		isChanged = true;
 	}
 	
 	public int getNotificationId()
 	{
 		return notification_id;
 	}
 
 	public boolean hasChanged()
 	{
 		return isChanged;
 	}
 	
 	public String toString()
 	{
 		return subject;
 	}
 
 	public void save()
 	{
 		ContentValues cv = new ContentValues();
 		cv.put(SUBJECT,subject);
 		cv.put(TEXT, text);
 		resolver.update(CONTENT_URI, cv, ID + " = " + id, null);
 	}
 }
