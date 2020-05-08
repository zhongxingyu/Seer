 
 package com.smilo.bullpen;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.widget.Toast;
 
 public final class Utils {
 
     private static final String TAG = "Utils";
     private static final boolean DEBUG = Constants.DEBUG_MODE;
 
     public static boolean isInternetConnected(Context context, boolean isPermitMobileConnection) {
         ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         
         // Check wifi.
         NetworkInfo niWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         if ((niWifi != null) && (niWifi.isConnected() == true)) {
                 return true;
         }
 
         // Check mobile.
         if (isPermitMobileConnection) {
             NetworkInfo mobileWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
             if ((mobileWifi != null) && (mobileWifi.isConnected() == true)) {
                 return true;
             }
         }
 /*
         // Check ethernet.
         NetworkInfo niEth = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
         if ((niEth != null) && (niEth.isConnected() == true)) {
                 return true;
         }
 */    
         Toast.makeText(context, R.string.internet_not_connected_msg, Toast.LENGTH_SHORT).show();
         return false;
     }
     
     public static boolean isTodayBestBoardType(int boardType) {
     	if ((boardType == Constants.BOARD_TYPE_MLB_TOWN_TODAY_BEST) ||
     		(boardType == Constants.BOARD_TYPE_KBO_TOWN_TODAY_BEST) ||
     		(boardType == Constants.BOARD_TYPE_BULLPEN_TODAY_BEST))
             return true;
         else
             return false;
     }
     
     public static int getRefreshTime(int refreshTimeType) {
         switch (refreshTimeType) {
             case Constants.REFRESH_TIME_TYPE_1_MIN :
             	return Constants.TIME_1_MIN;
             case Constants.REFRESH_TIME_TYPE_5_MIN :
                 return Constants.TIME_5_MIN;
             case Constants.REFRESH_TIME_TYPE_10_MIN :
                 return Constants.TIME_10_MIN;
             case Constants.REFRESH_TIME_TYPE_20_MIN :
                 return Constants.TIME_20_MIN;
             case Constants.REFRESH_TIME_TYPE_30_MIN :
                 return Constants.TIME_30_MIN;
             case Constants.REFRESH_TIME_TYPE_STOP :
                 return Constants.TIME_STOP;
             default:
                 return Constants.DEFAULT_INTERVAL;
         }
     }
     
     public static String getBoardTitle(Context context, int boardType) {
         Resources res = context.getResources();
         switch (boardType) {
             case Constants.BOARD_TYPE_MLB_TOWN :
                 return (res.getString(R.string.remoteViewTitle_MlbTown));
             case Constants.BOARD_TYPE_KBO_TOWN :
                 return (res.getString(R.string.remoteViewTitle_KboTown));
             case Constants.BOARD_TYPE_BULLPEN :
                 return (res.getString(R.string.remoteViewTitle_Bullpen));
             case Constants.BOARD_TYPE_MLB_TOWN_TODAY_BEST :
                 return (res.getString(R.string.remoteViewTitle_MlbTown_TodayBest));
             case Constants.BOARD_TYPE_KBO_TOWN_TODAY_BEST :
                 return (res.getString(R.string.remoteViewTitle_KboTown_TodayBest));
             case Constants.BOARD_TYPE_BULLPEN_TODAY_BEST :
                 return (res.getString(R.string.remoteViewTitle_Bullpen_TodayBest));
             case Constants.BOARD_TYPE_BULLPEN_1000 :
                 return (res.getString(R.string.remoteViewTitle_Bullpen1000));
             case Constants.BOARD_TYPE_BULLPEN_2000 :
                 return (res.getString(R.string.remoteViewTitle_Bullpen2000));
             default:
                 return (res.getString(R.string.remoteViewTitle_MlbTown));
         }
     }
     
     public static String getBoardUrl(int boardType) {
         switch (boardType) {
             case Constants.BOARD_TYPE_MLB_TOWN :
                 return Constants.URL_MLB_TOWN;
             case Constants.BOARD_TYPE_KBO_TOWN :
                 return Constants.URL_KBO_TOWN;
             case Constants.BOARD_TYPE_BULLPEN :
             	return Constants.URL_BULLPEN;
             case Constants.BOARD_TYPE_MLB_TOWN_TODAY_BEST :
             	return Constants.URL_MLB_TOWN_TODAY_BEST;
             case Constants.BOARD_TYPE_KBO_TOWN_TODAY_BEST :
             	return Constants.URL_KBO_TOWN_TODAY_BEST;
             case Constants.BOARD_TYPE_BULLPEN_TODAY_BEST :
             	return Constants.URL_BULLPEN_TODAY_BEST;
             case Constants.BOARD_TYPE_BULLPEN_1000 :
             	return Constants.URL_BULLPEN_1000;
             case Constants.BOARD_TYPE_BULLPEN_2000 :
             	return Constants.URL_BULLPEN_2000;
             default:
             	return Constants.URL_MLB_TOWN;
         }
     }
 }
