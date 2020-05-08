 ﻿package de.tum.in.tumcampus.models;
 
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
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
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
 
 /**
  * Class for helper functions
  */
 public class Utils {
 
 	/**
 	 * Counter for unfinished downloads
 	 */
 	public static int openDownloads = 0;
 
 	/**
 	 * Download a JSON stream from a URL
 	 * 
 	 * <pre>
 	 * @param url Valid URL
 	 * @return JSONObject
 	 * @throws Exception
 	 * </pre>
 	 */
 	public static JSONObject downloadJson(String url) throws Exception {
 		Utils.log(url);
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpEntity entity = httpclient.execute(new HttpGet(url)).getEntity();
 
 		String data = "";
 		if (entity != null) {
 
 			// JSON Response Read
 			InputStream instream = entity.getContent();
 			data = convertStreamToString(instream);
 
 			Utils.log(data);
 			instream.close();
 		}
 		return new JSONObject(data);
 	}
 
 	/**
 	 * Download a file in a new thread
 	 * 
 	 * <pre>
 	 * @param url Download location
 	 * @param target Target filename in local file system
 	 * </pre>
 	 */
 	public static void downloadFileThread(final String url, final String target) {
 		openDownloads++;
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Utils.log(url);
 					downloadFile(url, target);
 					openDownloads--;
 				} catch (Exception e) {
 					log(e, url);
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Download a file in the same thread
 	 * 
 	 * <pre>
 	 * @param url Download location
 	 * @param target Target filename in local file system
 	 * @throws Exception
 	 * </pre>
 	 */
 	private static void downloadFile(String url, String target)
 			throws Exception {
 		File f = new File(target);
 		if (f.exists()) {
 			return;
 		}
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpEntity entity = httpclient.execute(new HttpGet(url)).getEntity();
 
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
 
 	/**
 	 * Download an icon in a new thread
 	 * 
 	 * <pre>
 	 * @param url Download location
 	 * @param target Target filename in local file system
 	 * </pre>
 	 */
 	public static void downloadIconFileThread(final String url,
 			final String target) {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Utils.log(url);
 					downloadIconFile(url, target);
 				} catch (Exception e) {
 					log(e, url);
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Download an icon in the same thread
 	 * 
 	 * <pre>
 	 * @param url Download location
 	 * @param target Target filename in local file system
 	 * @throws Exception
 	 * </pre>
 	 */
 	private static void downloadIconFile(String url, String target)
 			throws Exception {
 		File f = new File(target);
 		if (f.exists()) {
 			return;
 		}
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 
 		// force mobile version of a web page
 		httpget.addHeader("User-Agent",
 				"Mozilla/5.0 (iPhone; de-de) AppleWebKit/528.18 Safari/528.16");
 
 		HttpEntity entity = httpclient.execute(httpget).getEntity();
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
 
 	/**
 	 * Convert an input stream to a string
 	 * 
 	 * <pre>
 	 * @param is input stream from file, download
 	 * @return output string
 	 * </pre>
 	 */
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
 
 	/**
 	 * Returns the full path of a cache directory and checks if it is readable
 	 * and writable
 	 * 
 	 * <pre>
 	 * @param directory directory postfix (e.g. feeds/cache)
 	 * @return full path of the cache directory
 	 * @throws Exception
 	 * </pre>
 	 */
 	public static String getCacheDir(String directory) throws Exception {
 		File f = new File(Environment.getExternalStorageDirectory().getPath()
 				+ "/tumcampus/" + directory);
 		if (!f.exists()) {
 			f.mkdirs();
 		}
 		if (!f.canRead()) {
 			throw new Exception("Von der SD-Karte kann nicht gelesen werden: "
 					+ "<sd>/tumcampus/" + directory);
 		}
 		if (!f.canWrite()) {
 			throw new Exception("Auf die SD-Karte kann nicht geschrieben "
 					+ "werden: <sd>/tumcampus/" + directory);
 		}
 		return f.getPath() + "/";
 	}
 
 	/**
 	 * Deletes all contents of a cache directory
 	 * 
 	 * <pre>
 	 * @param directory directory postfix (e.g. feeds/cache)
 	 * </pre>
 	 */
 	public static void emptyCacheDir(String directory) {
 		try {
 			File dir = new File(getCacheDir(directory));
 			if (dir.isDirectory() && dir.canWrite()) {
 				for (String child : dir.list()) {
 					new File(dir, child).delete();
 				}
 			}
 		} catch (Exception e) {
 			log(e, directory);
 		}
 	}
 
 	/**
 	 * Returns a URL from an internet shortcut file (.url)
 	 * 
 	 * <pre>
 	 * @param file Internet shortcut file (.url)
 	 * @return URL
 	 * </pre>
 	 */
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
 			log(e, file.toString());
 		}
 		return "";
 	}
 
 	/**
 	 * Returns a RSS-URL from a web page URL
 	 * 
 	 * e.g. http://www.spiegel.de returns http://www.spiegel.de/index.rss
 	 * 
 	 * <pre>
 	 * @param url Web page URL
 	 * @return RSS-URL
 	 * </pre>
 	 */
 	public static String getRssLinkFromUrl(String url) {
 		Utils.log(url);
 
 		String result = url;
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpEntity entity = httpclient.execute(new HttpGet(url))
 					.getEntity();
 			if (entity == null) {
 				return result;
 			}
 			InputStream instream = entity.getContent();
 			String data = convertStreamToString(instream);
 
 			if (data.startsWith("<?xml")) {
 				return result;
 			}
 			Pattern link = Pattern.compile("<link[^>]+>");
 			Pattern href = Pattern.compile("href=[\"'](.+?)[\"']");
 
 			Matcher matcher = link.matcher(data);
 			while (matcher.find()) {
 				String match = matcher.group(0);
 
 				Matcher href_match = href.matcher(match);
 				if (href_match.find()
 						&& (match.contains("application/rss+xml") || match
 								.contains("application/atom+xml"))) {
 					result = href_match.group(1);
 				}
 			}
 
 			// relative url
 			Uri uri = Uri.parse(url);
 			if (!result.contains("://")) {
 				result = "http://" + uri.getHost() + "/" + result;
 			}
 		} catch (Exception e) {
 			log(e, url);
 		}
 		return result;
 	}
 
 	/**
 	 * Get md5 hash from string
 	 * 
 	 * <pre>
 	 * @param str String to hash
 	 * @return md5 hash as string
 	 * </pre>
 	 */
 	public static String md5(String str) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.reset();
 			md.update(str.getBytes());
 			BigInteger bigInt = new BigInteger(1, md.digest());
 			return bigInt.toString(16);
 		} catch (Exception e) {
 			log(e, str);
 		}
 		return "";
 	}
 
 	/**
 	 * Converts a date-string to Date
 	 * 
 	 * <pre>
 	 * @param str String with ISO-Date (yyyy-mm-dd)
 	 * @return Date
 	 * </pre>
 	 */
 	public static Date getDate(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			log(e, str);
 		}
 		return new Date();
 	}
 
 	/**
 	 * Converts a datetime-string to Date
 	 * 
 	 * <pre>
 	 * @param str String with ISO-DateTime (yyyy-mm-ddThh:mm:ss)
 	 * @return Date
 	 * </pre>
 	 */
 	public static Date getDateTime(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyyy-MM-dd'T'HH:mm:ss");
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			log(e, str);
 		}
 		return new Date();
 	}
 
 	/**
 	 * Converts a German datetime-string to Date
 	 * 
 	 * <pre>
 	 * @param str String with German-DateTime (dd.mm.yyyy hh:mm)
 	 * @return Date
 	 * </pre>
 	 */
 	public static Date getDateTimeDe(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"dd.MM.yyyy HH:mm");
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			log(e, str);
 		}
 		return new Date();
 	}
 
 	/**
 	 * Converts a rfc822 datetime-string to Date
 	 * 
 	 * <pre>
 	 * @param str String with RFC822-Date (e.g. Tue, 12 Jul 2011 14:30:00)
 	 * @return Date
 	 * </pre>
 	 */
 	public static Date getDateTimeRfc822(String str) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"EEE, dd MMM yyyy HH:mm:ss", Locale.US);
 			return dateFormat.parse(str);
 		} catch (Exception e) {
 			log(e, str);
 		}
 		return new Date();
 	}
 
 	/**
 	 * Converts Date to an ISO date-string
 	 * 
 	 * <pre>
 	 * @param d Date
 	 * @return String (yyyy-mm-dd)
 	 * </pre>
 	 */
 	public static String getDateString(Date d) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		return dateFormat.format(d);
 	}
 
 	/**
 	 * Converts Date to a German date-string
 	 * 
 	 * <pre>
 	 * @param d Date
 	 * @return String (dd.mm.yyyy)
 	 * </pre>
 	 */
 	public static String getDateStringDe(Date d) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 		return dateFormat.format(d);
 	}
 
 	/**
 	 * Converts Date to an ISO datetime-string
 	 * 
 	 * <pre>
 	 * @param d Date
 	 * @return String (yyyy-mm-dd hh:mm:ss)
 	 * </pre>
 	 */
 	public static String getDateTimeString(Date d) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(
 				"yyyy-MM-dd HH:mm:ss");
 		return dateFormat.format(d);
 	}
 
 	/**
 	 * Return the value of a setting
 	 * 
 	 * <pre>
 	 * @param c Context
 	 * @param name setting name
 	 * @return setting value, "" if undefined
 	 * </pre>
 	 */
 	public static String getSetting(Context c, String name) {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
 		return sp.getString(name, "");
 	}
 
 	/**
 	 * Return the boolean value of a setting
 	 * 
 	 * <pre>
 	 * @param c Context
 	 * @param name setting name
 	 * @return true if setting was checked, else value
 	 * </pre>
 	 */
 	public static boolean getSettingBool(Context c, String name) {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
 		return sp.getBoolean(name, false);
 	}
 
 	/**
 	 * Truncates a string to a specified length and appends ...
 	 * 
 	 * <pre>
 	 * @param str String
 	 * @param limit maximum length
 	 * @return truncated String
 	 * </pre>
 	 */
 	public static String trunc(String str, int limit) {
 		String result = str;
 		if (str.length() > limit) {
 			result = str.substring(0, limit) + " ...";
 		}
 		return result;
 	}
 
 	/**
 	 * Splits a line from a CSV file into column values
 	 * 
	 * e.g. "aaa;aaa";"bbb";1 gets aaa,aaa;bbb;1;
 	 * 
 	 * <pre>
 	 * @param str CSV line
 	 * @return String[] with CSV column values
 	 * </pre>
 	 */
 	private static String[] splitCsvLine(String str) {
 		StringBuilder result = new StringBuilder();
 		boolean open = false;
 		for (int i = 0; i < str.length(); i++) {
 			char c = str.charAt(i);
 			if (c == '"') {
 				open = !open;
 				continue;
 			}
 			if (open && c == ';') {
 				result.append(",");
 			} else {
 				result.append(c);
 			}
 		}
 		// fix trailing ";", e.g. ";;;".split().length = 0
		result.append("; ");
 		return result.toString().split(";");
 	}
 
 	/**
 	 * Returns a String[]-List from a CSV input stream
 	 * 
 	 * <pre>
 	 * @param fin CSV input stream
 	 * @param charset Encoding, e.g. ISO-8859-1
 	 * @return String[]-List with Columns matched to array values
 	 * </pre>
 	 */
 	public static List<String[]> readCsv(InputStream fin, String charset) {
 		List<String[]> list = new ArrayList<String[]>();
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(fin,
 					charset));
 			String reader = "";
 			while ((reader = in.readLine()) != null) {
 				list.add(splitCsvLine(reader));
 			}
 			in.close();
 		} catch (Exception e) {
 			log(e, "");
 		}
 		return list;
 	}
 
 	/**
 	 * Returns the number of datasets in a table
 	 * 
 	 * <pre>
 	 * @param db Database connection
 	 * @param table Table name
 	 * @return number of datasets in a table
 	 * </pre>
 	 */
 	public static int dbGetTableCount(SQLiteDatabase db, String table) {
 		Cursor c = db.rawQuery("SELECT count(*) FROM " + table, null);
 		if (c.moveToNext()) {
 			return c.getInt(0);
 		}
 		return 0;
 	}
 
 	/**
 	 * Checks if a database table exists
 	 * 
 	 * <pre>
 	 * @param db Database connection
 	 * @param table Table name
 	 * @return true if table exists, else false
 	 * </pre>
 	 */
 	public static boolean dbTableExists(SQLiteDatabase db, String table) {
 		try {
 			Cursor c = db.rawQuery("SELECT 1 FROM " + table + " LIMIT 1", null);
 			if (c.moveToNext()) {
 				return true;
 			}
 		} catch (Exception e) {
 			log(e, "");
 		}
 		return false;
 	}
 
 	/**
 	 * Logs an exception and additional information
 	 * 
 	 * <pre>
 	 * @param e Exception (source for message and stacktrace)
 	 * @param message Additional information for exception message
 	 * </pre>
 	 */
 	public static void log(Exception e, String message) {
 		StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		Log.e("TumCampus", e + " " + message + "\n" + sw.toString());
 	}
 
 	/**
 	 * Logs a message
 	 * 
 	 * <pre>
 	 * @param message Information or Debug message
 	 * </pre>
 	 */
 	public static void log(String message) {
 		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
 		Log.d("TumCampus", s.toString() + " " + message);
 	}
 }
