 package com.licryle.veliby.BikeMap;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.Hashtable;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.licryle.veliby.Util;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 import android.util.Log;
 
 public class StationsInfoService extends IntentService {
   public static final int UPDATE_PROGRESS = 8344;
   public static final int SUCCESS = 8345;
   public static final int FAILURE_CONNECTION = 8346;
   public static final int FAILURE_GENERIC = 8347;
   public static final int FAILURE_PARSE = 8348;
   public static final int FINISHED = 8349;
 
   protected Hashtable<String, ResultReceiver> _mRequesters;
   protected Stations _mStations;
 
   public StationsInfoService() {
     super("StationsInfoService");
     Log.i("StationsInfoService", "Entering Constructor()");
 
     _mRequesters = new Hashtable<String, ResultReceiver>();
   }
 
   protected synchronized boolean _isConcurrent(ResultReceiver mReceiver,
       String sRequestor) {
     if (_mRequesters.containsKey(sRequestor)) return true;
 
     _mRequesters.put(sRequestor, mReceiver);
  
     // we do not queue the Intent, we just register the requester
     return (_mRequesters.size() > 1);
   }
 
   public int onStartCommand (Intent intent, int flags, int startId) {
    if (intent != null) {
      ResultReceiver mReceiver = (ResultReceiver) 
          intent.getParcelableExtra("receiver");
      String sRequestor = intent.getStringExtra("requestor");
  
      if (_isConcurrent(mReceiver, sRequestor)) return 0;
    }
 
     return super.onStartCommand(intent, flags, startId);
   }
 
   protected void _loadStations(File mStationsFile, int iDlDynamic) {
     if (_mStations == null) {
       _mStations = Stations.loadStationsInfo(mStationsFile, iDlDynamic);
     }
   }
 
   protected int _downloadStations(boolean bFullCycle, String sUrlFull,
       String sUrlDynamic, File mStationsFile) {
     Log.i("StationsInfoService", "Entering _downloadStations()");
     try {
       String sUrl = bFullCycle ? sUrlFull : sUrlDynamic;
 
       URL mUrl = new URL(sUrl);
       URLConnection mConnection = mUrl.openConnection();
       mConnection.connect();
 
       // For progress report
       int fileLength = mConnection.getContentLength();
 
       // download the file
       InputStream mInput = new BufferedInputStream(mUrl.openStream());
       ByteArrayOutputStream mOutput = new ByteArrayOutputStream();
 
       byte aData[] = new byte[1024];
       long lTotal = 0;
       int iCount;
       while ((iCount = mInput.read(aData)) != -1) {
         lTotal += iCount;
         // publishing the progress....
         Bundle mResultData = new Bundle();
 
         if (fileLength > 0) {
           mResultData.putInt("progress", (int) (lTotal * 100 / fileLength));
         }
 
         _dispatchResults(UPDATE_PROGRESS, mResultData);
         mOutput.write(aData, 0, iCount);
       }
 
       mOutput.flush();
       mInput.close();
 
       Bundle mResultData = new Bundle();
       if (bFullCycle) {
         _parseFullData(mOutput);
       } else {
         _parseDynamicData(mOutput);
       }
       mOutput.close();
 
       // Send result to process asking, write in background
       mResultData.putSerializable("stations", _mStations);
       _dispatchResults(SUCCESS, mResultData);
       _mStations.saveStationsInfo(mStationsFile);
     } catch (ConnectException e) {
       e.printStackTrace();
       return FAILURE_CONNECTION;
     } catch (IOException e) {
       e.printStackTrace();
       return FAILURE_GENERIC;
     } catch (Exception e) {
       e.printStackTrace();
       return FAILURE_PARSE;
     }
     return FINISHED;
   }
 
   @Override
   protected void onHandleIntent(Intent intent) {
     Log.i("StationsInfoService", "Entering onHandleIntent()");

    if (_mRequesters.size() == 0) return;

     String sUrlFull = intent.getStringExtra("url_full");
     String sUrlDynamic = intent.getStringExtra("url_dynamic");
     String sStationsFile = intent.getStringExtra("stations_file");
     File mStationsFile = new File(sStationsFile);
     int iDlStatic = intent.getIntExtra("dl_static", 1000);
     int iDlDynamic = intent.getIntExtra("dl_dynamic", 1000);
 
     _loadStations(mStationsFile, iDlDynamic);
 
     boolean bStaticExpired = _mStations.isStaticExpired(iDlStatic);
     boolean bDynamicExpired = _mStations.isDynamicExpired(iDlDynamic);
 
     int iResult;
     if (bStaticExpired || bDynamicExpired) {
       iResult = _downloadStations(bStaticExpired, sUrlFull, sUrlDynamic,
           mStationsFile);
     } else {
       iResult = SUCCESS;
     }
 
     if ( (iResult == FAILURE_CONNECTION || iResult == FAILURE_GENERIC ||
         iResult == FAILURE_PARSE) &&_mStations != null) {
       // We send static info for reference
       _mStations.removeDynamicData();
     }
 
     Bundle mBundle = new Bundle();
     mBundle.putSerializable("stations", _mStations);
     _dispatchResults(iResult, mBundle);
 
     _mRequesters.clear();
     Log.i("StationsInfoService", "Leaving onHandleIntent()");
   }
 
   protected void _parseFullData(ByteArrayOutputStream mInput)
       throws JSONException {
     String sInput = new String(mInput.toByteArray());
     JSONArray mJSon = new JSONArray(sInput);
   
     Stations mNewStations = new Stations();
     for (int i=0; i < mJSon.length(); i++) {
     	Station mStation = new Station(mJSon.getJSONObject(i));
     	mNewStations.put(mStation.getId(), mStation);
     }
     mNewStations.setLastUpdate(Calendar.getInstance().getTime());
 
     _mStations = mNewStations;
   }
 
   protected void _parseDynamicData(ByteArrayOutputStream mInput)
       throws Exception {
   	if (mInput.size() % 6 != 0) throw new Exception("Not rounded dynamic data");
 
   	byte aData[] = mInput.toByteArray();
   	int i = 0;
 
   	while (i < mInput.size()) {
   		int iId = Util.intToUInt((new Byte(aData[i++])).intValue(), 8) +
   							Util.intToUInt((new Byte(aData[i++])).intValue() << 8, 16) +
   							Util.intToUInt((new Byte(aData[i++])).intValue() << 16, 32);
 
       Station mStationToUp = _mStations.get(iId);
 
       if (mStationToUp != null) {
         int iAvBikes      = (new Byte(aData[i++])).intValue();
         int iAvBikeStands = (new Byte(aData[i++])).intValue();
         boolean bOpened   = (new Byte(aData[i++])).intValue() == 1;
         
         mStationToUp.update(bOpened, iAvBikes, iAvBikeStands);
       }
   	}
   	_mStations.setLastUpdate(Calendar.getInstance().getTime());
   }
 
   protected void _dispatchResults(int iSignal, Bundle mBundle) {
     for(ResultReceiver mReceiver : _mRequesters.values()) {
       mReceiver.send(iSignal, mBundle);
     }
   }
 }
