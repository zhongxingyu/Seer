 package me.ibhh.CommandLogger;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 
 class Update implements Serializable {
 	/**
  * 
  */
 	private static final long serialVersionUID = 1L;
 	String s; // stored
 	transient int i; // transient: not stored
 
 	public static String readAll(Reader in) throws IOException {
 		if (in == null) {
 			throw new NullPointerException("in == null");
 		}
 		try {
 			StringBuilder sb = new StringBuilder();
 			char[] buf = new char[1024];
 			int charsRead;
 			while ((charsRead = in.read(buf)) != -1) {
 				sb.append(buf, 0, charsRead);
 			}
 			return sb.toString();
 		} finally {
 			in.close();
 		}
 	}
 
 	public static void autoDownload(String url, String path, String name)
 			throws Exception {
 		File dir = new File(path);
 		if (!dir.exists()) {
 			dir.mkdir();
 		}
 		File file = new File(path + name);
 
 		if (file.exists() && name.equals("CommandLogger.jar")) {
 			file.delete();
 			try {
 				URL newurl = new URL(url);
 				// Eingehender Stream wird "erzeugt"
 				BufferedInputStream buffin = new BufferedInputStream(
 						newurl.openStream());
 				BufferedOutputStream buffout = new BufferedOutputStream(
 						new FileOutputStream(file));
 				byte[] buffer = new byte[200000];
 				int len;
 				// Ausgelesene Daten in die Datei schreiben
 				while ((len = buffin.read(buffer)) != -1) {
 					buffout.write(buffer, 0, len);
 				}
 				buffout.flush();
 				buffout.close();
 				buffin.close();
 				System.out.println("[CommandLogger] New " + name
 						+ " downloaded, Look up under " + path);
 			} finally {
 			}
 			return;
 		}
 		if (!file.exists()) {
 			try {
 				URL newurl = new URL(url);
 				// Eingehender Stream wird "erzeugt"
 				BufferedInputStream buffin = new BufferedInputStream(
 						newurl.openStream());
 				BufferedOutputStream buffout = new BufferedOutputStream(
 						new FileOutputStream(file));
 				byte[] buffer = new byte[200000];
 				int len;
 				// Ausgelesene Daten in die Datei schreiben
 				while ((len = buffin.read(buffer)) != -1) {
 					buffout.write(buffer, 0, len);
 				}
 				buffout.flush();
 				buffout.close();
 				buffin.close();
 				System.out.println("[CommandLogger] New " + name
 						+ " downloaded, Look up under " + path);
 			} finally {
 			}
 		}
 	}
 
 	/**
 	 * Delete an download new version of AuctionTrade in the Update folder.
 	 * 
 	 * @param
 	 * @return
 	 */
 	public static void autoUpdate(String url, String path, String name) {
 		try {
			autoDownload(url, path, "CommandLogger.jar");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Compares Version to newVersion
 	 * 
 	 * @param url
 	 *            from newVersion file + currentVersion
 	 * @return true if newVersion recommend.
 	 */
 	public static boolean UpdateAvailable(String url, float currVersion) {
 		boolean a = false;
 		if (getNewVersion(url) > currVersion) {
 			a = true;
 		}
 		return a;
 	}
 
 	/**
 	 * Checks version with a http-connection
 	 * 
 	 * @param
 	 * @return float: latest recommend build.
 	 */
 	public static float getNewVersion(String url) {
 		float rt2 = 0;
 		String zeile;
 
 		try {
 			URL myConnection = new URL(url);
 			URLConnection connectMe = myConnection.openConnection();
 
 			InputStreamReader lineReader = new InputStreamReader(
 					connectMe.getInputStream());
 			// BufferedReader buffer = new BufferedReader(lineReader);
 			BufferedReader br = new BufferedReader(new BufferedReader(
 					lineReader));
 			zeile = br.readLine();
 			rt2 = Float.parseFloat(zeile);
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
			System.out.println("[CommandLogger]Exception: IOException!");
 			return -1;
 		} catch (Exception e) {
 			e.printStackTrace();
			System.out.println("[CommandLogger]Exception: Exception!");
 			return 0;
 		}
 		return rt2;
 	}
 
 }
