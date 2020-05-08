 package com.ask.pws;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.util.EncodingUtils;
 
 import android.content.Context;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 
 /**
  * @author Raiden Awkward
  * @category sim card watcher
  */
 public class SimWatcher implements Watcher {
 	private List<String> targetNumbers;
 	private SmsManager smsManager;
 	static private String TARGET_PHONE_NUMBER = "13672077950";
 	static public String RECORD_FILE_NAME = "simrecord";
 
 	public SimWatcher() {
 		smsManager = SmsManager.getDefault();
 		targetNumbers = new ArrayList<String>();
 		targetNumbers.add(TARGET_PHONE_NUMBER);
 	}
 
	@Override
 	public boolean onWatch() {
 		String currentSim = getCurrentSimSeries();
 		String recordSim = getSimRecord();
 
 		if (recordSim == null) {
 			setSimRecord(currentSim);
 			String text = "sim changed with empty record: " + currentSim;
 			sendSMS(text);
 			return true;
 		}
 
 		if (!currentSim.equals(recordSim)) {
 			String text = "sim changed: " + currentSim;
 			setSimRecord(currentSim);
 			sendSMS(text);
 		} else {
 			//sendSMS("sim does not change");
 		}
 
 		return true;
 	}
 
 	public void sendSMS(String text) {
 		if (text == null)
 			return;
 
 		if (text.length() <= 0)
 			return;
 
 		for (int i = 0; i < targetNumbers.size(); i++) {
 			String number = targetNumbers.get(i);
 			if (number == null)
 				continue;
 			if (number.length() <= 0)
 				continue;
 			smsManager.sendTextMessage(number, null, text, null, null);
 		}
 	}
 
 	public String getSimRecord() {
 
 		String res = null;
 		try {
 			FileInputStream fin = BootCompleteReceiver.context.openFileInput(RECORD_FILE_NAME);
 			int length = fin.available();
 			if (length <= 0)
 				return res;
 
 			byte [] buffer = new byte[length];
 			fin.read(buffer);
 			res = EncodingUtils.getString(buffer, "UTF-8");
 			fin.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return res;
 	}
 
 	public void setSimRecord(String content) {
 		try {
 			FileOutputStream fout = BootCompleteReceiver.context.openFileOutput(RECORD_FILE_NAME, Context.MODE_PRIVATE);
 			byte [] bytes = content.getBytes();
 			fout.write(bytes);
 			fout.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getCurrentSimSeries() {
 		TelephonyManager tm = (TelephonyManager) BootCompleteReceiver.context.getSystemService(Context.TELEPHONY_SERVICE);
 		return tm.getSimSerialNumber();
 	}
 
 }
