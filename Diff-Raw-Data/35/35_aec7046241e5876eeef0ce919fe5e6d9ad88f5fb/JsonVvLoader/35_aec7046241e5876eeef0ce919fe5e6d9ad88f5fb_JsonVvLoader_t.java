 package ch.unibas.urz.android.vv.access;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderOperation.Builder;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import ch.unibas.urz.android.vv.helper.Logger;
 import ch.unibas.urz.android.vv.provider.VvContentProvider;
 import ch.unibas.urz.android.vv.provider.db.DB;
 
 public class JsonVvLoader {
 
	//private static final String JSON_URL = "http://urz-cfaa.urz.unibas.ch/dominik/vv_online/json.cfm";

	private static final String JSON_URL = "http://nikt.unibas.ch/mobileapp/vv/json.cfm";
 
 	private static String loadData(String url) throws Exception {
		try {
			URL aUrl = new URL(url);
			URLConnection conn = aUrl.openConnection();
			conn.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			Logger.w("Cannot load data from network", e);
			return "";
 		}
 	}
 
 	public static void loadEntries(Context ctx, long periodId, long parentId) {
 		long now = System.currentTimeMillis();
 		ContentResolver contentResolver = ctx.getContentResolver();
 		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
 		try {
 			StringBuilder url = new StringBuilder(JSON_URL);
 			url.append("?parent_id=").append(parentId);
 			if (periodId > 0) {
 				url.append("&period_id=").append(periodId);
 			}
 			JSONArray data = new JSONArray(loadData(url.toString()));
 			for (int i = 0; i < data.length(); i++) {
 				JSONObject object = data.getJSONObject(i);
 				long acsId = object.getLong(DB.VvEntity.NAME_ACS_ID);
 				String[] acsIdSelectionArgs = new String[] { Long.toString(acsId) };
 				boolean exists = false;
 				Cursor c = contentResolver.query(DB.VvEntity.CONTENT_URI, DB.VvEntity.PROJECTION_ACS_ID, DB.VvEntity.SELECTION_BY_ACSID, acsIdSelectionArgs, null);
 				if (c != null) {
 					exists = c.moveToFirst();
 					c.close();
 				}
 
 				Builder insertOrUpdate;
 				if (exists) {
 					insertOrUpdate = ContentProviderOperation.newUpdate(DB.VvEntity.CONTENT_URI);
 					insertOrUpdate.withSelection(DB.VvEntity.SELECTION_BY_ACSID, acsIdSelectionArgs);
 				} else {
 					insertOrUpdate = ContentProviderOperation.newInsert(DB.VvEntity.CONTENT_URI);
 				}
 
 				ContentValues values = new ContentValues();
 				values.put(DB.VvEntity.NAME_PERIOD_ID, periodId);
 				values.put(DB.VvEntity.NAME_UPDATE_TIMESTAMP, now);
 				values.put(DB.VvEntity.NAME_ACS_ID, acsId);
 				values.put(DB.VvEntity.NAME_ACS_NUMBER, object.getString(DB.VvEntity.NAME_ACS_NUMBER));
 				values.put(DB.VvEntity.NAME_ACS_CATEGORY, object.getString(DB.VvEntity.NAME_ACS_CATEGORY));
 				values.put(DB.VvEntity.NAME_ACS_TITLE, object.getString(DB.VvEntity.NAME_ACS_TITLE));
 				values.put(DB.VvEntity.NAME_ACS_CREDITPOINTS, object.getInt(DB.VvEntity.NAME_ACS_CREDITPOINTS));
 				values.put(DB.VvEntity.NAME_ACS_OTYPE, object.getString(DB.VvEntity.NAME_ACS_OTYPE));
 				values.put(DB.VvEntity.NAME_ACS_OBJID, object.getLong(DB.VvEntity.NAME_ACS_OBJID));
 				values.put(DB.VvEntity.NAME_ACS_SORT, object.getLong(DB.VvEntity.NAME_ACS_SORT));
 				values.put(DB.VvEntity.NAME_ACS_PARENT, object.getLong(DB.VvEntity.NAME_ACS_PARENT));
 
 				insertOrUpdate.withValues(values);
 				operations.add(insertOrUpdate.build());
 				Logger.i("Found entity: " + object.getString(DB.VvEntity.NAME_ACS_TITLE));
 			}
 
 			Builder delete = ContentProviderOperation.newDelete(DB.VvEntity.CONTENT_URI);
 			delete.withSelection(DB.VvEntity.SELECTION_BY_PARENT_PERIOD_NOT_UPDATE, new String[] { Long.toString(periodId), Long.toString(parentId), Long.toString(now) });
 			operations.add(delete.build());
 			contentResolver.applyBatch(VvContentProvider.AUTHORITY, operations);
 		} catch (Exception e) {
 			Logger.e("Cannot get VV entity info from the network", e);
 		}
 
 	}
 
 	public static void loadDetails(Context ctx, long periodId, long acsObjId) {
 		long now = System.currentTimeMillis();
 		ContentResolver contentResolver = ctx.getContentResolver();
 		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
 		try {
 			StringBuilder url = new StringBuilder(JSON_URL);
 			url.append("?acs_objid=").append(acsObjId);
 			if (periodId > 0) {
 				url.append("&period_id=").append(periodId);
 			}
 			JSONObject baseObject = new JSONObject(loadData(url.toString()));
 			JSONObject object = baseObject.getJSONArray("event").getJSONObject(0);
 
 			long acsId = object.getLong(DB.VvDetails.NAME_ACS_OBJ_ID);
 			String[] acsIdSelectionArgs = new String[] { Long.toString(acsId) };
 			boolean exists = false;
 			Cursor c = contentResolver.query(DB.VvDetails.CONTENT_URI, DB.VvDetails.PROJECTION_ACS_ID, DB.VvDetails.SELECTION_BY_ACSID, acsIdSelectionArgs, null);
 			if (c != null) {
 				exists = c.moveToFirst();
 				c.close();
 			}
 
 			Builder insertOrUpdate;
 			if (exists) {
 				insertOrUpdate = ContentProviderOperation.newUpdate(DB.VvDetails.CONTENT_URI);
 				insertOrUpdate.withSelection(DB.VvDetails.SELECTION_BY_ACSID, acsIdSelectionArgs);
 			} else {
 				insertOrUpdate = ContentProviderOperation.newInsert(DB.VvDetails.CONTENT_URI);
 			}
 
 			ContentValues values = new ContentValues();
 			values.put(DB.VvDetails.NAME_PERIOD_ID, periodId);
 			values.put(DB.VvDetails.NAME_UPDATE_TIMESTAMP, now);
 			values.put(DB.VvDetails.NAME_ACS_OBJ_ID, acsId);
 			values.put(DB.VvDetails.NAME_TITEL, object.getString(DB.VvDetails.NAME_TITEL));
 			values.put(DB.VvDetails.NAME_INHALT, object.getString(DB.VvDetails.NAME_INHALT));
 			values.put(DB.VvDetails.NAME_AMUSTER, object.getString(DB.VvDetails.NAME_AMUSTER));
 			values.put(DB.VvDetails.NAME_OEINHEIT, object.getString(DB.VvDetails.NAME_OEINHEIT));
 			values.put(DB.VvDetails.NAME_NOTETIME, object.getString(DB.VvDetails.NAME_NOTETIME));
 			values.put(DB.VvDetails.NAME_INTERVAL, object.getString(DB.VvDetails.NAME_INTERVAL));
 			values.put(DB.VvDetails.NAME_STARTDATE, object.getLong(DB.VvDetails.NAME_STARTDATE));
 			values.put(DB.VvDetails.NAME_ENDDATE, object.getLong(DB.VvDetails.NAME_ENDDATE));
 			values.put(DB.VvDetails.NAME_FAKULTAT, object.getString(DB.VvDetails.NAME_FAKULTAT));
 			values.put(DB.VvDetails.NAME_USPRACHE, object.getString(DB.VvDetails.NAME_USPRACHE));
 			values.put(DB.VvDetails.NAME_SKALA, object.getString(DB.VvDetails.NAME_SKALA));
 			values.put(DB.VvDetails.NAME_TVORAUSSETZUNG, object.getString(DB.VvDetails.NAME_TVORAUSSETZUNG));
 			values.put(DB.VvDetails.NAME_WBELEGEN, object.getString(DB.VvDetails.NAME_WBELEGEN));
 			values.put(DB.VvDetails.NAME_ANABMELDUNG, object.getString(DB.VvDetails.NAME_ANABMELDUNG));
 			values.put(DB.VvDetails.NAME_CREDITP, object.getString(DB.VvDetails.NAME_CREDITP));
 			values.put(DB.VvDetails.NAME_LPRUEF, object.getString(DB.VvDetails.NAME_LPRUEF));
 			values.put(DB.VvDetails.NAME_WIEDERHOLUNGPRUEF, object.getString(DB.VvDetails.NAME_WIEDERHOLUNGPRUEF));
 			values.put(DB.VvDetails.NAME_PRAESENZ, object.getString(DB.VvDetails.NAME_PRAESENZ));
 			values.put(DB.VvDetails.NAME_BEMERKUNG, object.getString(DB.VvDetails.NAME_BEMERKUNG));
 			values.put(DB.VvDetails.NAME_TYPE, object.getString(DB.VvDetails.NAME_TYPE));
 			values.put(DB.VvDetails.NAME_LERNZIELE, object.getString(DB.VvDetails.NAME_LERNZIELE));
			values.put(DB.VvDetails.NAME_LITERATUR, object.getString(DB.VvDetails.NAME_LITERATUR));
 			values.put(DB.VvDetails.NAME_HNOTE, object.getString(DB.VvDetails.NAME_HNOTE));
 			values.put(DB.VvDetails.NAME_LINK, object.getString(DB.VvDetails.NAME_LINK));
 			values.put(DB.VvDetails.NAME_LINKDESC, object.getString(DB.VvDetails.NAME_LINKDESC));
 			values.put(DB.VvDetails.NAME_VNR, object.getString(DB.VvDetails.NAME_VNR));
			values.put(DB.VvDetails.NAME_ANMELDUNGL, object.getString(DB.VvDetails.NAME_ANMELDUNGL));
 
 			// special fields
 			values.put(DB.VvDetails.NAME_MODULE, baseObject.getJSONArray("module").toString());
 			values.put(DB.VvDetails.NAME_LECTURER, baseObject.getJSONArray("lecturer").toString());
 			values.put(DB.VvDetails.NAME_TIME_PLACE, baseObject.getJSONArray("time").toString());
 
 			insertOrUpdate.withValues(values);
 			operations.add(insertOrUpdate.build());
 			Logger.i("Found detail: " + object.getString(DB.VvDetails.NAME_TITEL));
 
 			contentResolver.applyBatch(VvContentProvider.AUTHORITY, operations);
 		} catch (Exception e) {
 			Logger.e("Cannot get VV details info from the network", e);
 		}
 
 	}
 
 }
