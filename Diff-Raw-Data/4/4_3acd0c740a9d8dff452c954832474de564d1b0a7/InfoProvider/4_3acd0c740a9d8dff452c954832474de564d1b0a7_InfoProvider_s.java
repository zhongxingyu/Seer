 package de.boomboxbeilstein.android2;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.apache.http.client.ClientProtocolException;
 import org.joda.time.Duration;
 import org.joda.time.Instant;
 
 import android.util.Log;
 
 import com.google.gson.JsonParseException;
 
 import de.boomboxbeilstein.android2.utils.AppInfo;
 import de.boomboxbeilstein.android2.utils.GsonFactory;
 import de.boomboxbeilstein.android2.utils.Web;
 
 public class InfoProvider {
 	//public static final String URL = "http://quick/~jan/bbb/";
 	// public static final String URL = "http://yogu.square7.net/bbb/";
	//public static final String URL = "http://www.boomboxbeilstein.de/player/";
	public static final String URL = "http://quick-ubuntu:8090/";
 	public static final Duration UPDATE_INTERVAL = Duration.standardSeconds(5);
 	public static final Duration INFO_EXPIRE = Duration.standardSeconds(30);
 	public static final Duration GENERAL_EXPIRE = Duration.standardMinutes(5);
 	
 	private static final String TAG = "InfoProvider";
 	
 	private static GeneralInfo generalInfo = null;
 	private static Instant generalTime = null;
 	private static String lastHash = "";
 	private static boolean isRunning = false;
 	private static PlayerInfo lastInfo;
 	private static PlayerInfo currentInfo;
 	private static Instant infoTime = null;
 	private static Runnable updatedHandler;
 	private static Runnable serviceUpdatedHandler;
 
 	public static PlayerInfo getInfo() throws ClientProtocolException, IOException,
 			UnexcpectedResponseException {
 		try {
 			String json = Web.get(getCurrentURL());
 			PlayerInfo overview = GsonFactory.createGson().fromJson(json, PlayerInfo.class);
 			if (overview == null)
 				throw new UnexcpectedResponseException("Unable to parse the response as PlayerInfo json");
 			return overview;
 		} catch (JsonParseException e) {
 			throw new UnexcpectedResponseException(e);
 		}
 	}
 
 	private static void startUpdating() {
 		if (!isRunning) {
 			Thread thread = new Thread(new Runnable() {
 				public void run() {
 					long lastRun = System.currentTimeMillis();
 					while (isRunning) {
 						try {
 							PlayerInfo newInfo = getInfo();
 							if (newInfo != null) {
 								if (newInfo.getPlays() == null && currentInfo != null)
 									newInfo.setPlays(currentInfo.getPlays());
 								lastInfo = currentInfo;
 								infoTime = Instant.now();
 								currentInfo = newInfo;
 								if (currentInfo.getPlays() != null && currentInfo.getPlays().size() > 0) {
 									Play lastPlay = currentInfo.getPlays().get(currentInfo.getPlays().size() - 1);
 									if (lastPlay != null && lastPlay.getTrack() != null)
 										lastHash = lastPlay.getTrack().getHash();
 								}
 								if (updatedHandler != null)
 									updatedHandler.run();
 								if (serviceUpdatedHandler != null)
 									serviceUpdatedHandler.run();
 							} else
 								Log.e(TAG, "Invalid player info");
 
 							long sleepTime = UPDATE_INTERVAL.getMillis() - (System.currentTimeMillis() - lastRun);
 							if (sleepTime > 0)
 								Thread.sleep(sleepTime);
 							lastRun = System.currentTimeMillis();
 						} catch (InterruptedException e) {
 							break;
 						} catch (Exception e) {
 							Log.e(TAG, "Error fetching player info");
 							e.printStackTrace();
 						}
 					}
 				}
 			});
 			isRunning = true;
 			thread.start();
 		}
 	}
 
 	private static void stopUpdating() {
 		isRunning = false;
 	}
 
 	public static PlayerInfo getCurrentInfo() {
 		if (infoTime != null && Instant.now().isBefore(infoTime.plus(INFO_EXPIRE)))
 			return currentInfo;
 		else
 			return null;
 	}
 
 	public static PlayerInfo getLastInfo() {
 		return lastInfo;
 	}
 
 	public static void setUpdatedHandler(Runnable handler) {
 		updatedHandler = handler;
 		startUpdating();
 	}
 
 	public static void clearUpdatedHandler() {
 		updatedHandler = null;
 		if (serviceUpdatedHandler == null)
 			stopUpdating();
 	}
 
 	public static void setServiceUpdatedHandler(Runnable handler) {
 		serviceUpdatedHandler = handler;
 		startUpdating();
 	}
 
 	public static void clearServiceUpdatedHandler() {
 		updatedHandler = null;
 		if (updatedHandler == null)
 			stopUpdating();
 	}
 
 	private static String getCurrentURL() {
 		Random random = new Random();
 
 		return URL + "ajax.php?action=current&hash=" + lastHash + "&foo0=" + random.nextInt(1000000)
 			+ "&app=android&app-version=" + AppInfo.getVersion();
 	}
 
 	public static GeneralInfo getGeneralInfo() {
 		if (generalInfo == null || generalTime == null
 				|| Instant.now().isAfter(generalTime.plus(GENERAL_EXPIRE))) {
 			try {
 				String json = Web.get(URL + "ajax.php?action=info");
 				generalInfo = GsonFactory.createGson().fromJson(json, GeneralInfo.class);
 				generalTime = Instant.now();
 			} catch (ClientProtocolException e) {
 				Log.e(TAG, "Unable to fetch general info");
 				e.printStackTrace();
 			} catch (IOException e) {
 				Log.e(TAG, "Unable to fetch general info");
 				e.printStackTrace();
 			} catch (JsonParseException e) {
 				Log.e(TAG, "Unable to fetch general info");
 				e.printStackTrace();
 			}
 		}
 		return generalInfo;
 	}
 
 	public static boolean hasReceivedGeneralInfo() {
 		return generalInfo != null;
 	}
 }
