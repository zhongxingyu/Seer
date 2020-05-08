 package com.example.maple_android;
 
 import java.io.BufferedInputStream;
 import com.loopj.android.http.*;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 import com.nostra13.universalimageloader.core.assist.FailReason;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.StrictMode;
 import android.view.View;
 
 /**
  * This class assists with retrieving the list of
  * companies from the sever and then storing that list locally on the android
  * device.
  * <p>
  * The list is retrieved as a background task using AsyncTask
  * <p>
  * It is stored on the device's internal storage under MODE_PRIVATE so that the
  * file is private to the application.
  * <p>
  * Functionality is also provided to retrieve the local list, which parses the
  * local list and returns the list of companies as an ArrayList of Strings
  * <p>
  * This class also assists with retrieving available logos for a given company.
  */
 
 public class CompanyList {
 	// file name where the list is stored locally on device
 	private final static String FILE_NAME = "company_list";
 
 	// server url where list is stored
 	// this is relative to the base URL in MapleHttpClient
 	private final static String LIST_URL = "companies";
 
 	/**
 	 * Syncs the local company list with the current list on the server. If
 	 * there is an error contacting the server, no changes to the local copy are
 	 * made.
 	 * 
 	 * @param context The context of the application that will be accessing the list
 	 */
 	public static void syncListWithServer(Context context) {
 		// context must be declared final to be used in callback
 		final Context c = context;
 
 		// start an Async get request
 		MapleHttpClient.get(LIST_URL, null, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(String response) {
 				// open an output stream and write the file to disk
 				FileOutputStream fos = null;
 				try {
 					fos = c.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 				try {
 					fos.write(response.getBytes());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				try {
 					fos.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		// If there is a failure we don't have to do anything, just leave
 		// the local file alone.
 	}
 
 	/**
 	 * Retrieves the local company list from file, parses it, and returns the
 	 * contents as an ArrayList of strings. Each string is a company name.
 	 * 
 	 * @param context The context of the application
 	 * @return An ArrayList where each String is a company name
 	 */
 	public static ArrayList<String> getCompanyList(Context context) {
 		JSONArray jsonArray = getLocalCompaniesData(context);
 		
 		// init ArrayList
 		ArrayList<String> companyList = new ArrayList<String>();
 		
		// if no file found, return empty list
		if (jsonArray == null) return companyList;
		
 		// pull company names out of JSONArray
 		for(int i = 0; i < jsonArray.length(); i++){
 			JSONObject entry;
 			try {
 				entry = jsonArray.getJSONObject(i);
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return companyList;
 			}
 			try {
 				companyList.add(entry.getString("name"));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return companyList;
 			}
 		}
 
 		return companyList;
 	}
 
 	/**
 	 * Retrieves the companies JSON text string stored locally,
 	 * parses it into a JSONArray, and returns the result
 	 * @param context The application activity who has the locally stored list
 	 * @return JSONArray of companies. Null if there is an error
 	 */
 	private static JSONArray getLocalCompaniesData(Context context) {
 		// retrieve file from local storage
 		FileInputStream in = null;
 		try {
 			in = context.openFileInput(FILE_NAME);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}		
 
 		// transfer file stream to String
 		StringBuffer strBuff = new StringBuffer();
 		byte[] buffer = new byte[1024];
 		int length;
 
 		try {
 			while ((length = in.read(buffer)) != -1) {
 				strBuff.append(new String(buffer));
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		String file = strBuff.toString();
 		
 		// parse JSON string
 		JSONArray jsonData = null;
 		try {
 			jsonData = new JSONArray(file);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return jsonData;
 	}
 
 	/**
 	 * For a given company name, return an ArrayList of Bitmaps containing all
 	 * the logos available for that company. If no logos are available the list
 	 * will be empty.
 	 * <p>
 	 * The images are loaded and added to the ArrayLlist asynchronously in call
 	 * back methods so the list won't be immediately populated when it is
 	 * returned!
 	 * <p>
 	 * If you are loading large images or many images it may be a while before the
 	 * list is fully populated.
 	 * 
 	 * @param companyTag The company name
 	 * @return All available logos for the given company. Initially this list will be empty,
 	 * and will be populated over time as each url finished loading. (~1 sec)
 	 */
 	public static ArrayList<Bitmap> getCompanyLogosFromServer(Context context, String companyTag) {
 		// using Universal Image Loader library for easy loading of images from
 		// url
 		// https://github.com/nostra13/Android-Universal-Image-Loader
 		
 		// init ArrayList
 		final ArrayList<Bitmap> logos = new ArrayList<Bitmap>();
 
 		// get urls from JSON data
 		JSONArray arr = getLocalCompaniesData(context);
		// If file not found, return empty list of bitmaps 
		if (arr == null) return logos;
		
 		// find company in jsonarray
 		JSONObject company = null;
 		for(int i = 0; i < arr.length(); i++){
 			try {
 				company = arr.getJSONObject(i);
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return logos;
 			}
 			try {
 				if(company.getString("name").equals(companyTag)) break;
 				else company = null;
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return logos;
 			}
 		}
 		
 		// if company could not be found return empty list
 		if(company == null) return logos;
 		
 		
 		ArrayList<String> urls = new ArrayList<String>();
 		try {
 			urls.add(company.getString("company_url"));
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return logos;
 		}		
 
 		ImageLoader imageLoader = ImageLoader.getInstance();
 
 		// get each url started loading
 		for (String url : urls) {
 			imageLoader.loadImage(url, new SimpleImageLoadingListener() {
 				@Override
 				public void onLoadingComplete(String imageUri, View view,
 						Bitmap loadedImage) {
 					logos.add(loadedImage);
 				}
 			});
 
 		}
 
 		// return the arraylist. It will be populated in the background
 		// as each url loads
 		return logos;
 	}
 }
