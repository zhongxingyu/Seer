 package android.adhoc;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.util.Log;
 
 
 public class NativeHelper {
 	public static final String TAG = "NativeHelper";
 	public static File app_bin;
 	public static String missedFileFormat;
 	public static String SU_C;
 	public static String RUN;
 	public static String WIFI;
 
 	private static File SU_C_FILE;
 	private static File RUN_FILE;
 	private static File WIFI_FILE;
 
 
 	public static void setup(Context context, String format) {
 		missedFileFormat = format;
 		app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
 		// run need log dir
 		context.getDir("log", Context.MODE_PRIVATE).getAbsoluteFile();
 		SU_C_FILE = new File(app_bin, "su_c");
 		SU_C = SU_C_FILE.getAbsolutePath();
 		RUN_FILE = new File(app_bin, "run");
 		RUN = RUN_FILE.getAbsolutePath();
 		WIFI_FILE = new File(app_bin, "wifi");
 		WIFI = WIFI_FILE.getAbsolutePath();
 	}
 
 	public static boolean unzipAssets(Context context) {
 		boolean result = true;
 		try {
 			AssetManager am = context.getAssets();
 			final String[] assetList = am.list("");
 
 			for (String asset : assetList) {
				if (asset.equals("images") || asset.equals("sounds") || asset.equals("webkit")) {
 					continue;
 				}
 
 				int BUFFER = 2048;
 				final File file = new File(NativeHelper.app_bin, asset);
 				final InputStream assetIS = am.open(asset);
 
 				if (file.exists()) {
 					file.delete();
 //					Log.d(TAG, "rm " + file.getAbsolutePath());
 				}
 
 				FileOutputStream fos = new FileOutputStream(file);
 				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
 
 				int count;
 				byte[] data = new byte[BUFFER];
 
 				while ((count = assetIS.read(data, 0, BUFFER)) != -1) {
 					dest.write(data, 0, count);
 				}
 
 				dest.flush();
 				dest.close();
 
 				assetIS.close();
 			}
 		} catch (IOException e) {
 			result = false;
 			Log.e(NativeHelper.TAG, "Can't unzip", e);
 		}
 		chmod("0750", new File(SU_C));
 		chmod("0750", new File(RUN));
 		chmod("0750", new File(WIFI));
 		chmod("0750", new File(app_bin, "script_aria"));
 		chmod("0750", new File(app_bin, "script_hero"));
 		chmod("0750", new File(app_bin, "script_samsung"));
 		return result;
 	}
 
 	public static void chmod(String modestr, File path) {
 		String absolute_path = path.getAbsolutePath();
//		Log.d(TAG, "chmod " + modestr + " " + absolute_path);
 		try {
 			Class<?> fileUtils = Class.forName("android.os.FileUtils");
 			Method setPermissions = fileUtils.getMethod("setPermissions", String.class,
 					int.class, int.class, int.class);
 			int mode = Integer.parseInt(modestr, 8);
 			int a = (Integer) setPermissions.invoke(null, absolute_path, mode, -1, -1);
 			if (a != 0) {
 				Log.e(TAG, "setPermissions() returned " + a + " for '" + path + "'");
 			}
 		} catch (ClassNotFoundException e) {
 			Log.e(TAG, "android.os.FileUtils.setPermissions() failed:", e);
 		} catch (IllegalAccessException e) {
 			Log.e(TAG, "android.os.FileUtils.setPermissions() failed:", e);
 		} catch (InvocationTargetException e) {
 			Log.e(TAG, "android.os.FileUtils.setPermissions() failed:", e);
 		} catch (NoSuchMethodException e) {
 			Log.e(TAG, "android.os.FileUtils.setPermissions() failed:", e);
 		}
 	}
 
 	public static boolean existAssets(AdHocService adHocService) {
 		boolean state = true;
 		if (!app_bin.exists()) {
 			Log.e(TAG, String.format(missedFileFormat, app_bin.getAbsolutePath()));
 			state = false;
 		}
 		if (!SU_C_FILE.exists()) {
 			Log.e(TAG, String.format(missedFileFormat, SU_C));
 			state = false;
 		}
 		if (!RUN_FILE.exists()) {
 			Log.e(TAG, String.format(missedFileFormat, RUN));
 			state = false;
 		}
 		if (!WIFI_FILE.exists()) {
 			Log.e(TAG, String.format(missedFileFormat, WIFI));
 			state = false;
 		}
 		return state;
 	}
 
 
 	public static boolean isSupplicantError(String msg) {
 		return msg.contains("supplicant");
 	}
 
 	public static boolean isRootError(String msg) {
 		return msg.contains("ermission") || msg.contains("su: not found");
 	}
 
 	public static boolean isWifiOK(String line) {
 		return line.startsWith("WIFI: OK");
 	}
 }
