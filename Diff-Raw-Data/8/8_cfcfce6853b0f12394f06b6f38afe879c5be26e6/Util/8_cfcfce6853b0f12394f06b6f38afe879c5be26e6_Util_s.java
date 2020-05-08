 package com.twt.xtreme;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 
 import android.content.Context;
 import android.content.SharedPreferences;
import android.os.Build;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class Util {
 	private static String T = "UtilClass";
 	
 	// TODO
 	public static File exportDataToSD(int profid, String fromDate, String toDate) {
 		File exp_file = null;
 		
 		return exp_file;
 		
 	}
 	
 	public static String getSharedPrefStr(Context ctx, String key) {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		return pref.getString(key, "");
 	}
 	
 	public static boolean putSharedPrefStr(Context ctx, String key, String value) {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		SharedPreferences.Editor editor = pref.edit();
 		editor.putString(key, value);
 		return editor.commit();
 	}
 	
 	public static boolean clearSharedPrefStr(Context ctx, String key) {
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		SharedPreferences.Editor editor = pref.edit();
 		editor.remove(key);
 		return editor.commit();
 	}
 	
 	public static void setRentalRecordToSharedPref(Context ctx, RentalRecord rec) {
 		putSharedPrefStr(ctx, "rental_slot_id", rec.slot_id);
 		putSharedPrefStr(ctx, "rental_device_id", rec.device_id);
 	}
 	
 	public static RentalRecord getRentalRecordFromSharedPref(Context ctx) {
 		RentalRecord rec = new RentalRecord();
 		rec.setDeviceId(getSharedPrefStr(ctx, "rental_device_id"));
 		rec.setSlotId(getSharedPrefStr(ctx, "rental_slot_id"));
 		return rec;
 	}
 	
 	public static void clearRentalRecordFromSharedPref(Context ctx) {
 		clearSharedPrefStr(ctx, "rental_slot_id");
 		clearSharedPrefStr(ctx, "rental_device_id");
 	}
 
 	/* returns path to <root of SD card> + /healthstats */
 	public static File getExtFileStore(Context ctx) {
 		File path = new File(
 				Environment.getExternalStorageDirectory().getAbsolutePath() + 
 				"/healthstats" );
 		if (path != null) {
 			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 				if (!path.exists()) {
 					if (!path.mkdirs()) {
 						Log.e(T, "Unable to mkdir path: "+path.getAbsolutePath());
 						return null;
 					}
 				}
 			} else {
 				Log.e(T, String.format("External storage (%s) is not ready: %s.",
 						path.getAbsolutePath(), Environment.getExternalStorageState()));
 				return null;
 			}
 		}
 		return path;
 	} 
 	
 	public static byte[] hexStringToByteArray(String s) {
 		int len = s.length();
 		byte[] data = new byte[len / 2];
 		for (int i = 0; i < len; i += 2) {
 			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
 					+ Character.digit(s.charAt(i+1), 16));
 		}
 		return data;
 	}
 
 	public static String byteArrayToString(byte[] b) {
 		String s = "";
 		int len = b.length;
 		for (int i = 0; i < len; i++) {
 			s = s + Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
 		}
 		return s;
 	}
 	
 	public static int getIntFromByteArray(byte[] b) {
 		int i = 0;
 		if (b.length != 2) {
 			Log.w(T, "Non 2-byte int conversion not implemented");
 			return 0;
 		}
 		i += ((b[0] & 0xff) << 8);
 		i += ((b[1] & 0xff) << 0);
 		return i;
 	}
 	
 	public static byte[] appendChecksum(byte[] b) {
 		byte BYTE_END_BLOCK = (byte)0xfd;
 		short chksum = 0;
 		for (byte x : b) {
 			chksum += (int)(x & 0xff);
 		}
 		// add the EOF char to the byte array before compute
 		chksum += (int) (BYTE_END_BLOCK & 0xff);
 		byte[] r = new byte[b.length+3];
 
 		byte[] bchksum = ByteBuffer.allocate(2).putShort(chksum).array();
 		byte[] beof = { BYTE_END_BLOCK };
 		System.arraycopy(b, 0, r, 0, b.length);
 		System.arraycopy(bchksum, 0, r, b.length, bchksum.length);
 		System.arraycopy(beof, 0, r, b.length+bchksum.length, beof.length);
 		return r;
 	}
 
 	private static byte[] getMsgLengthBytes(int length) {
 		byte[] b = ByteBuffer.allocate(4).putInt(length).array();
 		byte[] b_strip = { b[2], b[3] };
 		return b_strip;
 	}
 
 	public static boolean verifyChecksum(byte[] b) {
 		byte[] bchksum = { b[b.length-3], b[b.length-2] };
 		int chksum = Util.getIntFromByteArray(bchksum);
 		byte[] tmp = new byte[b.length-2];
 		System.arraycopy(b, 0, tmp, 0, b.length-3);
 		System.arraycopy(b, b.length-1, tmp, b.length-3, 1);
 		int calc_chksum = 0;
 		for (int i=0; i < tmp.length; i++) {
 			calc_chksum += (int)(tmp[i] & 0xff);
 		}
 		// strip the first 2-bytes of integer to get the correct checksum
 		byte[] bcc = ByteBuffer.allocate(4).putInt(calc_chksum).array();
 		byte[] b_calc_chksum = { bcc[2], bcc[3] };
 		calc_chksum = Util.getIntFromByteArray(b_calc_chksum);
 		Log.d(T, "verifyChecksum: chksum="+chksum+" calc_sum="+calc_chksum);
 		return (calc_chksum == chksum);
 	}
 
 	public static String timestampBytes2String(byte[] tsb) {
 		String ts = "";
 		if (tsb.length != 5) { // this method only decode 5-bytes date stamp
 			Log.d(T, String.format("Timestamp byte length (%i) invalid.", tsb.length));
 			return ts;
 		}
 		for (int i = 0; i < tsb.length; i++) {
 			int d = ((tsb[i] & 0xff) << 0);
 			ts += String.format("%02d", d);
 		}
 		return ts;
 	}
 }
