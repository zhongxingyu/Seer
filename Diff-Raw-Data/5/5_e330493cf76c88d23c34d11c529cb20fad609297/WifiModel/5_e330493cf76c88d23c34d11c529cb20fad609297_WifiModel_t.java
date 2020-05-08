 package kajman.ssid.model;
 
 import kajman.ssid.model.entity.LogEntry;
 import kajman.ssid.model.entity.Wifi;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 public class WifiModel extends DbModel {
 
 	public WifiModel(Context context) {
 		super(context);
 	}
 	
 	public long save(Wifi input){
 		ContentValues values = new ContentValues();
         values.put(Wifi.Columns.BSSID,input.getBssid());
         values.put(Wifi.Columns.CAPABILITIES,input.getCapabilities());
         values.put(Wifi.Columns.CHANNEL,input.getChannel());       
         values.put(Wifi.Columns.SSID,input.getSsid());
         if(getDb().update(Wifi.TABLE_NAME, values, 
            String.format("%s = '%s' AND %s = '%s' AND %s = '%s'",
         		   		  Wifi.Columns.BSSID, input.getBssid(), Wifi.Columns.CAPABILITIES,input.getCapabilities(),
         		   		  Wifi.Columns.SSID,input.getSsid()),null)==0){
         	Log.d("SSID","Inserting new wifi");
         	values.put(Wifi.Columns.DATE,input.getDate());
         	values.put(Wifi.Columns.SCAN_NUMBER,input.getScanNumber());
         	return getDb().insert(Wifi.TABLE_NAME, "", values);
         }else{
         	return 0;
         }
 	}
 	
 	public long save(Iterable<Wifi> wifis){
 		long saved=0;
 		getDb().beginTransaction();
 		for(Wifi w: wifis){
 			if(save(w)>0){
 				saved++;
 			}
 		}
 		getDb().setTransactionSuccessful();
 		getDb().endTransaction();
 		return saved;
 	}
 	
 	public String toString(){
 		Cursor cursor = getDb().query(Wifi.TABLE_NAME, 
 				null, null, null, null, null, Wifi.Columns.BSSID + " ASC", null);
 		StringBuilder stringBuilder = new StringBuilder();
 		stringBuilder.append("Total in db: "+cursor.getCount()+"\n");
 		while(cursor.moveToNext()){
 			stringBuilder.append(getWifi(cursor).toString()+"\n");
 		}
 		cursor.close();
 		return stringBuilder.toString();
 	}
 	
 	public Wifi getWifi(Cursor cursor){
 		Wifi wifi = new Wifi();
 		wifi.setBssid(cursor.getString(cursor.getColumnIndex(Wifi.Columns.BSSID)));
 		wifi.setSsid(cursor.getString(cursor.getColumnIndex(Wifi.Columns.SSID)));
 		wifi.setCapabilities(cursor.getString(cursor.getColumnIndex(Wifi.Columns.CAPABILITIES)));
 		wifi.setChannel(cursor.getInt(cursor.getColumnIndex(Wifi.Columns.CHANNEL)));
 		wifi.setDate(cursor.getLong(cursor.getColumnIndex(Wifi.Columns.DATE)));
 		wifi.setScanNumber(cursor.getLong(cursor.getColumnIndex(Wifi.Columns.SCAN_NUMBER)));
 		return wifi;
 	}
 	
 	public long fetchLastScanTime(){
 		Cursor cursor = getDb().rawQuery("select max("+Wifi.Columns.DATE+")"+
 										 " from "+Wifi.TABLE_NAME, null);
 		if(cursor.moveToNext()){
			return cursor.getLong(cursor.getColumnIndex("max("+Wifi.Columns.DATE+")"));
 		}else{
 			return 0;
 		}
 	}
 	
 	public long fetchLastScanNumber(){
 		long result = 0;
 		Cursor cursor = getDb().rawQuery("select max("+Wifi.Columns.SCAN_NUMBER+")"+
 				 " from "+Wifi.TABLE_NAME, null);
 		if(cursor.moveToNext()){
			result = cursor.getLong(cursor.getColumnIndex("max("+Wifi.Columns.SCAN_NUMBER+")"));
 			Log.d("DEBUG","lastScanNumber is "+result);
 		}
 		cursor.close();
 		return result;
 	}
 
 }
