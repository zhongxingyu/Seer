 package com.adtworker.mail;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.adtworker.mail.constants.Constants;
 
 public class GoogleImage {
 	static String REQUEST_TEMPLATE = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q={0}&start={1}&rsz={2}&imgsz=medium&imgtype=photo";
 	private static String GOOGLE_AJAX_URL_TEMPLATE = "http://www.google.com.hk/search?q={0}&hl=zh-CN&newwindow=1&safe=strict&tbs=isz:ex,iszw:{1},iszh:{2}&biw=1399&bih=347&tbm=isch&ijn=2&ei=PyCKT8DJAavimAWUiJjgCQ&sprg=3&page={3}&start={4}";
 	// private static String GOOGLE_AJAX_URL_TEMPLATE =
 	// "http://www.google.com.hk/search?q={0}&hl=zh-CN&safe=strict&biw=1780&bih=638&gbv=2&tbs=isz:l&tbm=isch&ei=xN-PT-DeIZCZiQeyyviiBA&start={3}&sa=N";
 
 	static JSONObject json;
 
 	/**
 	 * @param keyword
 	 * @param start
 	 * @param resultSize
 	 *            (1~8)
 	 * @return
 	 */
 	public static ArrayList<String> getImgUrl(String keyword, int start,
 			int resultSize) {
 		ArrayList<String> listImages = new ArrayList<String>();
 		String requestUrl = MessageFormat.format(REQUEST_TEMPLATE, keyword,
 				start, resultSize);
 
 		try {
 			URL url = new URL(requestUrl);
 			URLConnection connection = url.openConnection();
 			connection.setConnectTimeout(5000);
 			connection.setReadTimeout(30000);
 			connection.addRequestProperty("Referer",
 					"http://technotalkative.com");
 
 			String line;
 			StringBuilder builder = new StringBuilder();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()));
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 			json = new JSONObject(builder.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		try {
 			JSONObject responseObject = json.getJSONObject("responseData");
 			JSONArray resultArray = responseObject.getJSONArray("results");
 
 			for (int i = 0; i < resultArray.length(); i++) {
 				JSONObject obj;
 				obj = resultArray.getJSONObject(i);
 
 				System.out.println("Image URL => " + obj.getString("url"));
 				System.out.println("Image tbURL => " + obj.getString("tbUrl"));
 
 				listImages.add(obj.getString("url"));
 			}
 
 			System.out
 					.println("Result array length => " + resultArray.length());
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return listImages;
 	}
 	public static String getHTML(String url) throws Exception {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(url);
 		httpGet.addHeader(
 				"User-Agent",
 				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.106 Safari/535.2");
 		httpGet.addHeader(
 				"Referer",
 				"http://www.google.com.hk/search?hl=zh-CN&newwindow=1&safe=strict&biw=1399&bih=725&tbs=isz%3Aex%2Ciszw%3A480%2Ciszh%3A800&tbm=isch&sa=1&q=MM+%E5%A3%81%E7%BA%B8&oq=MM+%E5%A3%81%E7%BA%B8&aq=f&aqi=&aql=&gs_l=img.3...680499l683087l0l683551l5l5l0l0l0l0l0l0ll0l0.frgbld.");
 		HttpResponse response = httpclient.execute(httpGet);
 		StatusLine statusLine = response.getStatusLine();
 		if (200 == statusLine.getStatusCode()) {
 			String result = EntityUtils.toString(response.getEntity());
 			return result;
 		} else {
 			return "";
 		}
 	}
 	private static Pattern googleScriptImgRegex = Pattern
 			.compile(".*imgurl=(.*.[png|jpg|jpeg])&amp.*data-src=\"(.*)\" height.*");
 
 	public static Map<String, String> getImgByAjaxUrl(String keyword,
 			Integer width, Integer height, Integer page, Integer start) {
 		String requestUrl = MessageFormat.format(GOOGLE_AJAX_URL_TEMPLATE,
 				keyword, width, height, page, start);
 
 		Map<String, String> imageMap = new HashMap<String, String>();
 		try {
 			String response = getHTML(requestUrl).trim();
 			String[] imageDivs = response.split("a href");
 			for (String imageDiv : imageDivs) {
 				try {
 					Matcher m = googleScriptImgRegex.matcher(imageDiv);
 					if (m.matches() && m.groupCount() == 2) {
 						String url = m.group(1).trim();
						url = url.substring(0, url.indexOf("&amp;"));
						imageMap.put(url, m.group(2).trim());
 					}
 				} catch (Exception e) {
 					Log.e(Constants.TAG, "get img error", e);
 					continue;
 				}
 			}
 		} catch (Exception e) {
 			Log.e(Constants.TAG, "get img error", e);
 			return imageMap;
 		}
 		return imageMap;
 	}
 
 	public static List<AdtImage> getImgListByAjax(String keyword,
 			Integer width, Integer height, Integer page, Integer start) {
 		Map<String, String> imgMaps = getImgByAjaxUrl(keyword, width, height,
 				page, start);
 		List<AdtImage> imgList = new ArrayList<AdtImage>();
 		if (imgMaps != null && imgMaps.size() > 0) {
 			for (String key : imgMaps.keySet()) {
 				AdtImage imgInfo = new AdtImage(key, imgMaps.get(key));
 				imgList.add(imgInfo);
 			}
 		}
 		return imgList;
 	}
 }
