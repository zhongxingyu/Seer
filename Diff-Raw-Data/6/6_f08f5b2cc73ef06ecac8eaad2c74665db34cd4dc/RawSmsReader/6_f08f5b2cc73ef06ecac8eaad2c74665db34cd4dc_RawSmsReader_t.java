 package com.michalmazur.orphanedtexts;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.telephony.SmsMessage;
 import android.util.Log;
 
 public class RawSmsReader {
 	private ArrayList<Orphan> orphans = new ArrayList<Orphan>();
 	private Context context;
 
 	public RawSmsReader() {
 
 	}
 
 	public RawSmsReader(Context c) {
 		this.context = c;
 	}
 
 	public ArrayList<Orphan> getOrphans() {
 		if (this.context == null) {
 			Log.d("ORPHAN", "No context. Will return fake data.");
 			return getFakeOrphans();
 		}
 
 		String uriString = "content://sms/raw";
 		Uri uri = Uri.parse(uriString);
 		Cursor c = context.getContentResolver()
 				.query(uri, null, null, null, null /* "_id limit 10" */);
 
 		while (c.moveToNext()) {
 			Orphan newOrphan = new Orphan();
 			newOrphan.setId(c.getInt(c.getColumnIndex("_id")));
 			newOrphan.setDate(new Date(c.getLong(c.getColumnIndex("date"))));
 			newOrphan.setReferenceNumber(c.getInt(c.getColumnIndex("reference_number")));
 			newOrphan.setCount(c.getInt(c.getColumnIndex("count")));
 			newOrphan.setSequence(c.getInt(c.getColumnIndex("sequence")));
 			newOrphan.setDestinationPort(c.getInt(c.getColumnIndex("destination_port")));
 			newOrphan.setAddress(c.getString(c.getColumnIndex("address")));
 			SmsMessage message = SmsMessage.createFromPdu(this.hexStringToByteArray(c.getString(c.getColumnIndex("pdu"))));
			try {
				newOrphan.setMessageBody(message.getMessageBody());
			} catch (NullPointerException e) {
				newOrphan.setMessageBody("<cannot read message body>");
			}
 			orphans.add(newOrphan);
 		}
 
 		return orphans;
 	}
 	
 	private ArrayList<Orphan> getFakeOrphans() {
 		ArrayList<Orphan> orphans = new ArrayList<Orphan>();
 		for (int i = 0; i < 10; i++) {
 			Orphan o = new Orphan();
 			o.setId(i);
 			o.setDate(new Date());
 			o.setReferenceNumber(0);
 			o.setCount(1);
 			o.setSequence(2);
 			o.setDestinationPort(5);
 			o.setAddress("+1 123 456 7890");
 			o.setMessageBody("this is my message");
 			orphans.add(o);
 		}
 		return orphans;
 	}
 
 	// http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
 	public byte[] hexStringToByteArray(String s) {
 		int len = s.length();
 		byte[] data = new byte[len / 2];
 		for (int i = 0; i < len; i += 2) {
 			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(
 					s.charAt(i + 1), 16));
 		}
 		return data;
 	}
 }
