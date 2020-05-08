 package org.javaopen.sms2gmail;
 
import java.io.IOException;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Data;
 import android.util.Log;
 
 public class ForwardService extends IntentService {
 	private static final String TAG = ForwardService.class.getName();
 	private static final String NEWLINE = System.getProperty("line.separator");
 	
 	public static final String FORWARD_PHONE = "org.javaopen.sms2gmail.PHONE";
 	public static final String FORWARD_SMS   = "org.javaopen.sms2gmail.SMS";
 	
 	String account = null;
 
 	public ForwardService() {
 		super(TAG);
 	}
 	
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		String action = intent.getAction();
 		Log.d(TAG, "wakeHandler: action="+action);
 		
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 
 		String key = getString(R.string.activated_key);
 		boolean activated = sp.getBoolean(key, false);
 		if (!activated) return;
 		
 		key = getString(R.string.account_key);
 		account = sp.getString(key, null);
 		
 		if (FORWARD_PHONE.equals(action)) {
 			forwardPhone(intent);
 		} else if (FORWARD_SMS.equals(action)) {
 			forwardSMS(intent);
 		}
 	}
 	
 	void forwardPhone(Intent intent) {
 		long ctime = intent.getLongExtra(PhoneReceiver.CTIME_KEY, System.currentTimeMillis());
 		String number = intent.getStringExtra(PhoneReceiver.INCOMING_NUMBER_KEY);
 		Log.d(TAG, "forwardPhone: ctime="+ctime+"number="+number);
 
 		StringBuffer message = new StringBuffer();
 		// ctime
 		message.append(new Date(ctime).toLocaleString());
 		message.append(NEWLINE);
 		// contact
 		Cursor c = null;
 		try {
 			c = getContentResolver().query(
 					Data.CONTENT_URI, 
					new String[]{ Phone.LABEL }, 
 					Phone.NUMBER + " = ? ", 
 					new String[]{number},
					Phone.LABEL);
 			if (c != null) {
 				message.append("name=");
 				while (c.moveToNext()) {
 					int i = 0;
 					message.append(c.getString(i++));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (c != null) c.close();
 		}
 		if (message.length() > 0) message.append(NEWLINE);
 
 		// number
 		message.append("tel:");
 		message.append(number);
 		message.append(NEWLINE);
 
 		if (account == null) {
 			Log.d(TAG, "onStart: account is null. ctime="+ctime+", number="+number+", message="+message);
 			return;
 		}
 		
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		String key = getString(R.string.phone_subject_key);
 		String def = getString(R.string.phone_subject_default);
 		String subject = sp.getString(key, def);
 		
 		// gmail
 		gmail(account, subject, message.toString());
 	}
 	
 	void forwardSMS(Intent intent) {
 		long ctime = intent.getLongExtra(SMSReceiver.TIMESTAMP_KEY, System.currentTimeMillis());
 		String from = intent.getStringExtra(SMSReceiver.FROM_KEY);
 		String body = intent.getStringExtra(SMSReceiver.BODY_KEY);
 		Log.d(TAG, "forwardSMS: ctime="+ctime+", from="+from+", body="+body);
 
 		if (account == null) {
 			Log.d(TAG, "onStart: account is null. ctime="+ctime+", from="+from+", body="+body);
 			return;
 		}
 		
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		String key = getString(R.string.sms_subject_key);
 		String def = getString(R.string.sms_subject_default);
 		String subject = sp.getString(key, def);
 		// TODO gmail
 		StringBuffer message = new StringBuffer();
 		// ctime
 		message.append(new Date(ctime).toLocaleString());
 		message.append(NEWLINE);
 		// from
 		message.append("from=");
 		message.append(from);
 		message.append(NEWLINE);
 		// body
 		message.append("body=");
 		message.append(body);
 		message.append(NEWLINE);
 		
 		gmail(account, subject, message.toString());
 	}
 	
 	public void gmail(String to, String subject, String body) {
 	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 	    String key = getString(R.string.account_key);
 	    String login = sp.getString(key, null);
 	    key = getString(R.string.password_key);
 	    String password = sp.getString(key, null);
 	    
 	    Properties props = new Properties();
 	    props.put("mail.smtp.host", "smtp.gmail.com"); // SMTPサーバ名
 	    props.put("mail.host", "smtp.gmail.com");      // 接続するホスト名
 	    props.put("mail.smtp.port", "587");       // SMTPサーバポート
 	    props.put("mail.smtp.auth", "true");    // smtp auth
 	    props.put("mail.smtp.starttls.enable", "true"); // STTLS
 	    
 	    Session session = Session.getDefaultInstance(props);
 	    session.setDebug(true);
 
 	    MimeMessage msg = new MimeMessage(session);
 	    try {
 	        msg.setSubject(subject, "utf-8");
 	        msg.setFrom(new InternetAddress(login));
 	        msg.setSender(new InternetAddress(login));
 	        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
 	        msg.setText(body,  "utf-8");
 
 	        Transport t = session.getTransport("smtp");
 	        t.connect(login, password);
 	        t.sendMessage(msg, msg.getAllRecipients());
 	    } catch (MessagingException e) {
 	        e.printStackTrace();
 	    }
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
