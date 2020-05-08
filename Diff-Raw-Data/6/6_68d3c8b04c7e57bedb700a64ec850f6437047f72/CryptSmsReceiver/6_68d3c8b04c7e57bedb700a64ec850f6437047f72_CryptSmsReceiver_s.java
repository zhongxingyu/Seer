 package de.pentabarf.cryptmessaging;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.SmsMessage;
 import android.text.SpannableString;
 import android.text.SpannableStringBuilder;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.style.StyleSpan;
 import android.text.style.TextAppearanceSpan;
 import android.util.Log;
 
 public class CryptSmsReceiver extends BroadcastReceiver {
     // TODO specify protocol
 
     static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
 
     private static final int NOTIFICATION_ID = 123;
     private static final String SMS_EXTRA_NAME = "pdus";
     private static final String TAG = "CryptSmsReceiver";
 
     /*
      * build notification ticker message for sms
      * 
      * based on com.android.mms.transaction.MessagingNotification
      */
     private static CharSequence buildTickerMessage(String displayAddress,
 	    String subject, String body) {
 	StringBuilder buf = new StringBuilder(displayAddress == null ? ""
 		: displayAddress.replace('\n', ' ').replace('\r', ' '));
 
 	int offset = buf.length();
 	if (!TextUtils.isEmpty(subject) || !TextUtils.isEmpty(body))
 	    buf.append(':').append(' ');
 	if (!TextUtils.isEmpty(subject)) {
 	    subject = subject.replace('\n', ' ').replace('\r', ' ');
 	    buf.append(subject);
 	    buf.append(' ');
 	}
 
 	if (!TextUtils.isEmpty(body)) {
 	    body = body.replace('\n', ' ').replace('\r', ' ');
 	    buf.append(body);
 	}
 
 	SpannableString spanText = new SpannableString(buf.toString());
 	spanText.setSpan(new StyleSpan(Typeface.BOLD), 0, offset,
 		Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 	return spanText;
     }
 
     /*
      * format a message for BigText notifications
      * 
      * based on com.android.mms.transaction.MessagingNotification
      */
     public static CharSequence formatBigMessage(Context context,
 	    String mMessage, String mSubject) {
 	final TextAppearanceSpan notificationSubjectSpan = new TextAppearanceSpan(
 		context, R.style.NotificationPrimaryText);
 
 	SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
 	if (!TextUtils.isEmpty(mSubject)) {
 	    spannableStringBuilder.append(mSubject);
 	    spannableStringBuilder.setSpan(notificationSubjectSpan, 0,
 		    mSubject.length(), 0);
 	}
 	if (mMessage != null) {
 	    if (spannableStringBuilder.length() > 0) {
 		spannableStringBuilder.append('\n');
 	    }
 	    spannableStringBuilder.append(mMessage);
 	}
 	return spannableStringBuilder;
     }
 
     /**
      * retrieve contact for phone number, check if contact has key assigned.
      * @param ctx
      * @param address phone number used to look up contact
      * @return contact's display name if he has a key assigned, null otherwise
      */
     private static String lookupContact(Context ctx, String address) {
 	// prepare lookup URI
 	Uri uri = Uri.withAppendedPath(
 		ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
 		Uri.encode(address));
 
 	// find contact
 	Cursor c = ctx.getContentResolver().query(
 		uri,
 		new String[] { ContactsContract.Contacts.LOOKUP_KEY,
 			ContactsContract.Contacts.DISPLAY_NAME }, null, null,
 		null);
 
 	String displayName = address;
 	String contactId = null;
 
 	try {
 	    if (c != null && c.moveToNext()) {
 		// contact found
 		contactId = c.getString(0);
 		displayName = c.getString(1);
 	    }
 	} finally {
 	    if (c != null) {
 		c.close();
 	    }
 	}
 
 	if (contactId == null) {
 	    Log.w(TAG, "got text from unknown contact");
 	    return null;
 	}
 	
 	// retrieve contact's notes 
 	c = ctx.getContentResolver()
 		.query(ContactsContract.Data.CONTENT_URI,
 			new String[] { ContactsContract.CommonDataKinds.Note.NOTE },
 			ContactsContract.Contacts.LOOKUP_KEY + " = ? AND "
 				+ ContactsContract.Data.MIMETYPE + " = ?",
 			new String[] {
 				contactId,
 				ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE },
 			null);
 	if (c != null && c.moveToNext()) {
 	    String note = c.getString(0);
 	    if (!TextUtils.isEmpty(note)) {
 		Log.w(TAG, "got a note result:" + note);
 		return displayName;
 	    }
 	} else {
 	    Log.w(TAG, "contact " + contactId + " has no note");
 	}
 	return null;
     }
 
     /**
      * stores incoming text message in system message storage
      * useful for modifying (eg. decrypting) incoming message text 
      * 
      * @param ctx
      * @param msg single parsed message for metadata
      * @param pdus unparsed message fragments
      * @return assembled message body
      */
     private static String restoreMessage(Context ctx, SmsMessage msg,
 	    Object[] pdus) {
 	ContentValues values = new ContentValues();
 	values.put("address", msg.getDisplayOriginatingAddress());
	if (msg.getTimestampMillis() < 1)
	    values.put("date", System.currentTimeMillis());
	else
	    values.put("date", msg.getTimestampMillis());
 	values.put("read", Integer.valueOf(0));
 	if (msg.getPseudoSubject().length() > 0) {
 	    values.put("subject", msg.getPseudoSubject());
 	}
 	values.put("reply_path_present", msg.isReplyPathPresent() ? 1 : 0);
 	values.put("service_center", msg.getServiceCenterAddress());
 
 	StringBuilder body = new StringBuilder();
 	for (Object pdu : pdus) {
 	    body.append(SmsMessage.createFromPdu((byte[]) pdu)
 		    .getDisplayMessageBody());
 	}
 	// TODO decrypt body
 
 	values.put("body", body.toString());
 
 	ctx.getContentResolver().insert(Uri.parse("content://sms/inbox"),
 		values);
 
 	return body.toString();
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
 	if (ACTION.equals(intent.getAction())) {
 	    Log.w(TAG, "Intent received: " + intent.getAction());
 	    Bundle bundle = intent.getExtras();
 	    if (bundle == null)
 		return;
 
 	    // parse incoming message
 	    Object[] pdus = (Object[]) bundle.get(SMS_EXTRA_NAME);
 
 	    // use first message part for metadata
 	    byte[] mainPdu = (byte[]) pdus[0];
 	    SmsMessage msg = SmsMessage.createFromPdu(mainPdu);
 
 	    String source = msg.getDisplayOriginatingAddress();
 
 	    // retrieve contact info / check key assignment
 	    String contactTitle = lookupContact(context, source);
 
 	    if (contactTitle != null) {
 		// TODO check for encrypted message
 		
 		// if the sender is a known crypto contact, don't handle as normal message 
 		abortBroadcast();
 		
 		// assemble, decrypt and store message 
 		String body = restoreMessage(context, msg, pdus);
 		
 		// collect notification info
 		String subject = msg.getPseudoSubject();
 		CharSequence formatted = formatBigMessage(context, body,
 			subject);
 
 		Intent contentIntent = new Intent(Intent.ACTION_VIEW,
 			Uri.fromParts("sms",
 				msg.getDisplayOriginatingAddress(), null));
 
 		contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 		// based on com.android.mms.transaction.MessagingNotification
 		// prepare notification
 		Notification.Builder builder = new Notification.Builder(context);
 		builder.setTicker(buildTickerMessage(contactTitle, subject,
 			msg.getDisplayMessageBody()));
 		builder.setContentTitle(buildTickerMessage(contactTitle, null,
 			null));
 		builder.setContentIntent(PendingIntent.getActivity(context, 0,
 			contentIntent, 0));
 		builder.setSmallIcon(android.R.drawable.stat_notify_chat);
 		builder.setDefaults(Notification.DEFAULT_ALL);
 		builder.setPriority(Notification.PRIORITY_DEFAULT);
 		builder.setContentText(formatted);
 		builder.setAutoCancel(true);
 		// TODO avatar
 
 		Notification notification = new Notification.BigTextStyle(
 			builder).bigText(formatted).build();
 
 		// show notification
 		((NotificationManager) context
 			.getSystemService(Context.NOTIFICATION_SERVICE))
 			.notify(NOTIFICATION_ID, notification);
 
 	    }
 
 	}
     }
 }
