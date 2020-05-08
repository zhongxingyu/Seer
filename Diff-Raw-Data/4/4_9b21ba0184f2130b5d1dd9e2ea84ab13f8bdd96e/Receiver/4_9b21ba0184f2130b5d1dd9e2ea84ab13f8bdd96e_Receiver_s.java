 package vnd.blueararat.smssieve;
 
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsMessage;
 import android.text.format.Time;
 
 public class Receiver extends BroadcastReceiver {
 
 	static final String KEY_ENABLED = "enabled";
 	static final String KEY_SKIPPED = "skipped";
 	static final String FILTERS = "filters";
 	static final String REGEX_FILTERS = "regex";
 	static final String EXCEPTIONS = "exceptions";
 	static final String REGEX_EXCEPTIONS = "regex_exceptions";
 	static final String KEY_MATCHES = "matches";
 	static final String KEY_LOG = "log";
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		try {
 			SharedPreferences preferences = PreferenceManager
 					.getDefaultSharedPreferences(context);
 			boolean b = preferences.getBoolean(KEY_ENABLED, true);
 			if (!b)
 				return;
 			SharedPreferences exceptions = context.getSharedPreferences(
 					EXCEPTIONS, Context.MODE_PRIVATE);
 			SharedPreferences regex_exceptions = context.getSharedPreferences(
 					REGEX_EXCEPTIONS, Context.MODE_PRIVATE);
 			SharedPreferences filters = context.getSharedPreferences(FILTERS,
 					Context.MODE_PRIVATE);
 			SharedPreferences regex_filters = context.getSharedPreferences(
 					REGEX_FILTERS, Context.MODE_PRIVATE);
 
 			if (filters.getAll().isEmpty() && regex_filters.getAll().isEmpty())
 				return;
 			Set<String> set2 = regex_exceptions.getAll().keySet();
 			Set<String> set4 = regex_filters.getAll().keySet();
 
 			Bundle bundle = intent.getExtras();
 			if (bundle != null) {
 				Object[] pdus = (Object[]) bundle.get("pdus");
 				SmsMessage[] msgs = new SmsMessage[pdus.length];
 				for (int i = 0; i < msgs.length; i++) {
 					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
 				}
 				String adr = msgs[0].getOriginatingAddress();
 
 				if (exceptions.contains(adr)) {
 					int j = exceptions.getInt(adr, 0) + 1;
 					Editor et2 = exceptions.edit();
 					et2.putInt(adr, j);
 					et2.commit();
 					return;
 				} else {
 					for (String exp : set2) {
 						Pattern p = null;
 						try {
 							p = Pattern.compile(exp);
 						} catch (PatternSyntaxException e) {
 							continue;
 						}
 						Matcher m = p.matcher(adr);
 						if (m.matches()) {
							int j = regex_filters.getInt(exp, 0) + 1;
							Editor et2 = regex_filters.edit();
 							et2.putInt(exp, j);
 							et2.commit();
 							return;
 						}
 					}
 				}
 
 				final String font1 = "<font color='#ff7f7f'>";
 				final String font2 = "<br /><font color='#ffff7f'>";
 				final String font3 = "</font><br />";
 				final String br = "<br />";
 				final String search = "<font";
 				final int max_log_length = 4000;
 				final int max_length = 50;
 				final String df = "%Y-%m-%d %H-%M";
 
 				if (filters.contains(adr)) {
 					Time today = new Time(Time.getCurrentTimezone());
 					today.setToNow();
 					String log = font1 + adr + font3 + msgs[0].getMessageBody()
 							+ font2 + today.format(df) + font3 + br
 							+ preferences.getString(KEY_LOG, "");
 					if (log.length() > max_log_length) {
 						int l = log.lastIndexOf(search);
 						log = log.substring(0, l);
 					}
 					String matches = " " + adr
 							+ preferences.getString(KEY_MATCHES, "");
 					if (matches.length() > max_length) {
 						matches = matches
 								.substring(0, matches.lastIndexOf(" "));
 					}
 					int i = preferences.getInt(KEY_SKIPPED, 0) + 1;
 					int j = filters.getInt(adr, 0) + 1;
 					Editor et = preferences.edit();
 					et.putInt(KEY_SKIPPED, i);
 					et.putString(KEY_MATCHES, matches);
 					et.putString(KEY_LOG, log);
 					et.commit();
 					Editor et2 = filters.edit();
 					et2.putInt(adr, j);
 					et2.commit();
 					abortBroadcast();
 				} else {
 					for (String exp : set4) {
 						Pattern p = null;
 						try {
 							p = Pattern.compile(exp);
 						} catch (PatternSyntaxException e) {
 							continue;
 						}
 						Matcher m = p.matcher(adr);
 						if (m.matches()) {
 							Time today = new Time(Time.getCurrentTimezone());
 							today.setToNow();
 							String log = font1 + adr + font3
 									+ msgs[0].getMessageBody() + font2
 									+ today.format(df) + font3 + br
 									+ preferences.getString(KEY_LOG, "");
 							if (log.length() > max_log_length) {
 								int l = log.lastIndexOf(search);
 								log = log.substring(0, l);
 							}
 							String matches = " " + adr
 									+ preferences.getString(KEY_MATCHES, "");
 							if (matches.length() > max_length) {
 								matches = matches.substring(0,
 										matches.lastIndexOf(" "));
 							}
 							int i = preferences.getInt(KEY_SKIPPED, 0) + 1;
 							int j = regex_filters.getInt(exp, 0) + 1;
 							Editor et = preferences.edit();
 							et.putInt(KEY_SKIPPED, i);
 							et.putString(KEY_MATCHES, matches);
 							et.putString(KEY_LOG, log);
 							et.commit();
 
 							Editor et2 = regex_filters.edit();
 							et2.putInt(exp, j);
 							et2.commit();
 							abortBroadcast();
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 }
