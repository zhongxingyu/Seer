 package ch.amana.android.cputuner.helper;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.widget.Toast;
 import ch.amana.android.cputuner.hw.CpuHandler;
 import ch.amana.android.cputuner.provider.db.DB;
 
 public class InstallHelper {
 
 	private static final String SORT_ORDER = DB.NAME_ID + " DESC";
 	private static final String ONDEMAND = "ondemand";
 	private static final String POWERSAVE = "powersave";
 
 	public static void populateDb(Context ctx) {
 		Toast.makeText(ctx, "Loading default profiles", Toast.LENGTH_SHORT).show();
 		ContentResolver resolver = ctx.getContentResolver();
 
 		Cursor cP = resolver.query(DB.CpuProfile.CONTENT_URI, new String[] { DB.NAME_ID }, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
 		Cursor cT = resolver.query(DB.Trigger.CONTENT_URI, new String[] { DB.NAME_ID }, null, null, SORT_ORDER);
 
 		if (cP == null || cT == null || (cP.getCount() < 1 && cT.getCount() < 1)) {
 			int freqMax = CpuHandler.getInstance().getMaxCpuFreq();
 			int freqMin = CpuHandler.getInstance().getMinCpuFreq();
 			String gov = CpuHandler.getInstance().getCurCpuGov();
 
 			List<String> availGov = Arrays.asList(CpuHandler.getInstance().getAvailCpuGov());
 
 			long profilePerformance = createCpuProfile(resolver, "Performance", getPowerGov(availGov, gov), freqMax, freqMin);
 			long profileNormal = createCpuProfile(resolver, "Normal", getPowerGov(availGov, gov), freqMax, freqMin);
 			long profilePowersave = createCpuProfile(resolver, "Powersave", getSaveGov(availGov, gov), freqMax, freqMin);
 			long profileExtremPowersave = createCpuProfile(resolver, "Extrem powersave", getSaveGov(availGov, gov), freqMax, freqMin);
 
 			createTrigger(resolver, "Battery full", 100, profilePowersave, profileNormal, profilePerformance);
			createTrigger(resolver, "Battery used", 75, profilePowersave, profilePowersave, profileNormal);
			createTrigger(resolver, "Battery empty", 50, profileExtremPowersave, profilePowersave, profilePowersave);
 			createTrigger(resolver, "Battery critical", 25, profileExtremPowersave, profileExtremPowersave, profilePowersave);
 
 		}
 
 		if (cP != null && !cP.isClosed()) {
 			cP.close();
 		}
 		if (cT != null && !cT.isClosed()) {
 			cT.close();
 		}
 	}
 
 	private static void createTrigger(ContentResolver resolver, String name, int batLevel, long screenOff, long battery, long power) {
 
 		ContentValues values = new ContentValues();
 		values.put(DB.Trigger.NAME_TRIGGER_NAME, name);
 		values.put(DB.Trigger.NAME_BATTERY_LEVEL, batLevel);
 		values.put(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, screenOff);
 		values.put(DB.Trigger.NAME_BATTERY_PROFILE_ID, battery);
 		values.put(DB.Trigger.NAME_POWER_PROFILE_ID, power);
 		insertOrUpdate(resolver, DB.Trigger.CONTENT_URI, values);
 	}
 
 	private static long createCpuProfile(ContentResolver resolver, String name, String gov, int freqMax, int freqMin) {
 
 		ContentValues values = new ContentValues();
 		values.put(DB.CpuProfile.NAME_PROFILE_NAME, name);
 		values.put(DB.CpuProfile.NAME_GOVERNOR, gov);
 		values.put(DB.CpuProfile.NAME_FREQUENCY_MAX, freqMax);
 		values.put(DB.CpuProfile.NAME_FREQUENCY_MIN, freqMin);
 		return insertOrUpdate(resolver, DB.CpuProfile.CONTENT_URI, values);
 	}
 
 	private static String getSaveGov(List<String> list, String gov) {
 		if (list == null || list.size() < 1) {
 			return "";
 		}
 		if (list.contains(POWERSAVE)) {
 			return POWERSAVE;
 		} else if (list.contains(ONDEMAND)) {
 			return ONDEMAND;
 		}
 		return gov;
 	}
 
 	private static String getPowerGov(List<String> list, String gov) {
 		if (list == null || list.size() < 1) {
 			return "";
 		}
 		if (list.contains(ONDEMAND)) {
 			return ONDEMAND;
 		} else if (list.contains(POWERSAVE)) {
 			return POWERSAVE;
 		}
 		return gov;
 	}
 
 	public static long insertOrUpdate(ContentResolver resolver, Uri contentUri, ContentValues values) {
 		String selection = DB.NAME_ID + "=" + values.getAsString(DB.NAME_ID);
 		Cursor c = resolver.query(contentUri, new String[] { DB.NAME_ID }, selection, null, SORT_ORDER);
 		long id;
 		if (c != null && c.moveToFirst()) {
 			id = resolver.update(contentUri, values, selection, null);
 		} else {
 			values.remove(DB.NAME_ID);
 			Uri uri = resolver.insert(contentUri, values);
 			id = ContentUris.parseId(uri);
 		}
 		if (c != null && !c.isClosed()) {
 			c.close();
 		}
 		return id;
 	}
 
 }
