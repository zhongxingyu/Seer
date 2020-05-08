 package de.tum.in.tumcampus.models;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class Utils {
 
 	public static int openDownloads = 0;
 
 	public static JSONObject downloadJson(String url) throws Exception {
 		Utils.Log(url);
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 		String data = "";
 
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 
 		if (entity != null) {
 
 			// JSON Response Read
 			InputStream instream = entity.getContent();
 			data = convertStreamToString(instream);
 
 			Utils.Log(data);
 			instream.close();
 		}
 		return new JSONObject(data);
 	}
 
 	public static void downloadFileThread(final String url, final String target) {
 		openDownloads++;
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					Utils.Log(url);
 					downloadFile(url, target);
 					openDownloads--;
 				} catch (Exception e) {
 					Log(e, url);
 				}
 			}
 		}).start();
 	}
 
 	private static void downloadFile(String url, String target)
 			throws Exception {
 		File f = new File(target);
 		if (f.exists()) {
 			return;
 		}
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 
 		if (entity == null) {
 			return;
 		}
 		File file = new File(target);
 		InputStream in = entity.getContent();
 
 		FileOutputStream out = new FileOutputStream(file);
 		byte[] buffer = new byte[8192];
 		int count = -1;
 		while ((count = in.read(buffer)) != -1) {
 			out.write(buffer, 0, count);
 		}
 		out.flush();
 		out.close();
 		in.close();
 	}
 
 	public static void downloadIconFileThread(final String url,
 			final String target) {
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					Utils.Log(url);
 					downloadIconFile(url, target);
 				} catch (Exception e) {
 					Log(e, url);
 				}
 			}
 		}).start();
 	}
 
 	private static void downloadIconFile(String url, String target)
 			throws Exception {
 		File f = new File(target);
 		if (f.exists()) {
 			return;
 		}
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 		httpget.addHeader("User-Agent",
 				"Mozilla/5.0 (iPhone; de-de) AppleWebKit/528.18 Safari/528.16");
 
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 
 		if (entity == null) {
 			return;
 		}
 		InputStream in = entity.getContent();
 		String data = convertStreamToString(in);
 
 		String icon = "";
 		Pattern link = Pattern.compile("<link[^>]+>");
 		Pattern href = Pattern.compile("href=[\"'](.+?)[\"']");
 
 		Matcher matcher = link.matcher(data);
 		while (matcher.find()) {
 			String match = matcher.group(0);
 
 			Matcher href_match = href.matcher(match);
 			if (href_match.find()) {
 				if (match.contains("shortcut icon") && icon.length() == 0) {
 					icon = href_match.group(1);
 				}
 				if (match.contains("apple-touch-icon")) {
 					icon = href_match.group(1);
 				}
 			}
 		}
 
 		Uri uri = Uri.parse(url);
 		// icon not found
 		if (icon.length() == 0) {
 			icon = "http://" + uri.getHost() + "/favicon.ico";
 		}
 		// relative url
 		if (!icon.contains("://")) {
 			icon = "http://" + uri.getHost() + "/" + icon;
 		}
 		// download icon
 		downloadFile(icon, target);
 	}
 
 	private static String convertStreamToString(InputStream is) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 
 	public static String getCacheDir(String dir) throws Exception {
 		File f = new File(Environment.getExternalStorageDirectory().getPath()
 				+ "/tumcampus/" + dir);
 		if (!f.exists()) {
 			f.mkdirs();
 		}
 		if (!f.canRead()) {
 			throw new Exception("Von der SD-Karte kann nicht gelesen werden: "
 					+ "<sd>/tumcampus/" + dir);
 		}
 		if (!f.canWrite()) {
 			throw new Exception("Auf die SD-Karte kann nicht geschrieben "
 					+ "werden: <sd>/tumcampus/" + dir);
 		}
 		return f.getPath() + "/";
 	}
 
 	public static void emptyCacheDir(String directory) {
 		try {
 			File dir = new File(getCacheDir(directory));
 			if (dir.isDirectory() && dir.canWrite()) {
 				for (String child : dir.list()) {
 					new File(dir, child).delete();
 				}
 			}
 		} catch (Exception e) {
 			Log(e, directory);
 		}
 	}
 
 	public static String getLinkFromUrlFile(File file) {
 		try {
 			byte[] buffer = new byte[(int) file.length()];
 			FileInputStream in = new FileInputStream(file.getAbsolutePath());
 			in.read(buffer);
 			in.close();
 			Pattern pattern = Pattern.compile("URL=(.*?)$");
 			Matcher matcher = pattern.matcher(new String(buffer));
 			matcher.find();
 			return matcher.group(1);
 		} catch (Exception e) {
 			Log(e, file.toString());
 		}
 		return "";
 	}
 
 	public static String getRssLinkFromUrl(String url) {
 		Utils.Log(url);
 
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpGet httpget = new HttpGet(url);
 
 			HttpResponse response = httpclient.execute(httpget);
 			HttpEntity entity = response.getEntity();
 
 			if (entity == null) {
 				return url;
 			}
 			InputStream instream = entity.getContent();
 			String data = convertStreamToString(instream);
 
 			if (data.startsWith("<?xml")) {
 				return url;
 			}
 			Pattern link = Pattern.compile("<link[^>]+>");
 			Pattern href = Pattern.compile("href=[\"'](.+?)[\"']");
 
 			String feedUrl = url;
 			Matcher matcher = link.matcher(data);
 			while (matcher.find()) {
 				String match = matcher.group(0);
 
 				Matcher href_match = href.matcher(match);
 				if (href_match.find()
 						&& (match.contains("application/rss+xml") || match
 								.contains("application/atom+xml"))) {
 					feedUrl = href_match.group(1);
 				}
 			}
 
 			// relative url
 			Uri uri = Uri.parse(url);
 			if (!feedUrl.contains("://")) {
 				feedUrl = "http://" + uri.getHost() + "/" + feedUrl;
 			}
 			url = feedUrl;
 		} catch (Exception e) {
 			Log(e, url);
 		}
 		return url;
 	}
 
 	public static String md5(String str) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.reset();
 			md.update(str.getBytes());
 			BigInteger bigInt = new BigInteger(1, md.digest());
 			return bigInt.toString(16);
 		} catch (Exception e) {
 			Log(e, str);
 		}
 		return "";
 	}
 
 	public static Date getDate(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			Log(e, str);
 		}
 		return new Date();
 	}
 
 	public static Date getDateTime(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyyy-MM-dd'T'HH:mm:ss");
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			Log(e, str);
 		}
 		return new Date();
 	}
 
 	public static Date getDateTimeDe(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"dd.MM.yyyy HH:mm");
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			Log(e, str);
 		}
 		return new Date();
 	}
 
 	public static Date getDateTimeRfc822(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"d MMM yy HH:mm:ss");
			return dateFormat.parse(str.substring(5));
 		} catch (Exception e) {
 			Log(e, str);
 		}
 		return new Date();
 	}
 
 	public static String getDateString(Date d) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		return dateFormat.format(d);
 	}
 
 	public static String getDateStringDe(Date d) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 		return dateFormat.format(d);
 	}
 
 	public static String getDateTimeString(Date d) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(
 				"yyyy-MM-dd HH:mm:ss");
 		return dateFormat.format(d);
 	}
 
 	public static String getSetting(Context c, String name) {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
 		return sp.getString(name, "");
 	}
 
 	public static boolean getSettingBool(Context c, String name) {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
 		return sp.getBoolean(name, false);
 	}
 
 	public static String trunc(String str, int limit) {
 		if (str.length() > limit) {
 			str = str.substring(0, limit) + " ...";
 		}
 		return str;
 	}
 
 	public static List<String[]> readCsv(InputStream fin, String encoding) {
 		List<String[]> list = new ArrayList<String[]>();
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(fin,
 					encoding));
 			String reader = "";
 			while ((reader = in.readLine()) != null) {
 				// TODO fix splitting
 				list.add(reader.replaceAll("\"", "").split(";"));
 			}
 			in.close();
 		} catch (Exception e) {
 			Log(e, "");
 		}
 		return list;
 	}
 
 	public static int getCount(SQLiteDatabase db, String table) {
 		Cursor c = db.rawQuery("SELECT count(*) FROM " + table, null);
 		if (c.moveToNext()) {
 			return c.getInt(0);
 		}
 		return 0;
 	}
 
 	public static void Log(Exception e, String message) {
 		StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		Log.e("TumCampus", e + " " + message + "\n" + sw.toString());
 	}
 
 	public static void Log(String message) {
 		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
 		Log.d("TumCampus", s.toString() + " " + message);
 	}
 }
