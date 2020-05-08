 package de.tum.in.tumcampus.models;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.text.SimpleDateFormat;
 import java.util.Date;
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
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 
 public class Utils {
 
 	// TODO optimize
 
 	/*
 	 * } catch (ClientProtocolException e) { // TODO Auto-generated catch block
 	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
 	 * catch block e.printStackTrace(); } catch (JSONException e) { // TODO
 	 * Auto-generated catch block e.printStackTrace(); } catch (Exception e) {
 	 * e.printStackTrace(); }
 	 */
 	public static JSONObject downloadJson(String url) throws Exception {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 		String data = "";
 
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 
 		if (entity != null) {
 
 			// JSON Response Read
 			InputStream instream = entity.getContent();
 			data = convertStreamToString(instream);
 
 			Log.d("TumCampus Download", "TumCampus Download " + data);
 			instream.close();
 		}
 		return new JSONObject(data);
 	}
 
 	public static void downloadFile(String url, String target) throws Exception {
 		// TODO add download queue + extra thread?
 		// TODO set timeouts
 
 		Log.d("TumCampus Download", "TumCampus Download " + url);
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 
 		if (entity == null) {
 			// TODO implement
 			throw new Exception("error");
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
 
 	public static void downloadIconFile(String url, String target)
 			throws Exception {
 		Log.d("TumCampus Download Icon", "TumCampus Download Icon " + url);
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(url);
 		httpget.addHeader("User-Agent",
 				"Mozilla/5.0 (iPhone; de-de) AppleWebKit/528.18 Safari/528.16");
 
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 
 		if (entity == null) {
 			// TODO implement
 			throw new Exception("error");
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
 				if (match.indexOf("shortcut icon") != -1 && icon.length() == 0) {
 					icon = href_match.group(1);
 				}
 				if (match.indexOf("apple-touch-icon") != -1) {
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
 		if (icon.indexOf("://") == -1) {
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
 			throw new Exception("Cannot read from sd-card: " + f.getPath());
 		}
 		if (!f.canWrite()) {
 			throw new Exception("Cannot write to sd-card: " + f.getPath());
 		}
 		return f.getPath() + "/";
 	}
 
 	public static void emptyCacheDir(String directory) {
 		try {
 			File dir = new File(getCacheDir(directory));
 			if (dir.isDirectory() && dir.canWrite()) {
 				String[] children = dir.list();
 				for (int i = 0; i < children.length; i++) {
 					new File(dir, children[i]).delete();
 				}
 			}
 		} catch (Exception e) {
 			// TODO implement
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
 			// TODO implement
 		}
 		return "";
 	}
 
 	public static String md5(String s) {
 		try {
 			MessageDigest m = MessageDigest.getInstance("MD5");
 			m.reset();
 			m.update(s.getBytes());
 			BigInteger bigInt = new BigInteger(1, m.digest());
 			return bigInt.toString(16);
 		} catch (Exception e) {
 			// TODO implement
 		}
 		return "";
 	}
 
 	public static Date getDate(String s) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			return dateFormat.parse(s);
 		} catch (Exception e) {
 			// TODO implement
 		}
 		return new Date();
 	}
 
 	public static Date getDateTime(String s) {
 		try {
 			SimpleDateFormat dateFormat = new SimpleDateFormat(
 					"yyyy-MM-dd'T'HH:mm:ss");
 			return dateFormat.parse(s);
 		} catch (Exception e) {
 			// TODO implement
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
 
 	public static String loadSetting(Context c, String name) {
 		SharedPreferences prefs = c.getSharedPreferences("prefs",
 				Context.MODE_PRIVATE);
 		return prefs.getString(name, null);
 	}
 
 	public static void saveSetting(Context c, String name, String value) {
 		SharedPreferences prefs = c.getSharedPreferences("prefs",
 				Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString(name, value);
 		editor.commit();
 	}
 }
