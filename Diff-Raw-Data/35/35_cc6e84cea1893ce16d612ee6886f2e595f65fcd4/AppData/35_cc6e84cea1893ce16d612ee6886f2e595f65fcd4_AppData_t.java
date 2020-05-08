 package com.bspif.app.mobilemechanic;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.provider.Settings;
 
 public class AppData {
 	
 	private static final String CAT_TITLE		= "title";
 	private static final String CAT_ICON		= "icon";
 	private static final String CAT_LESSONS 	= "content";
 	private static final String LESSON_TITLE	= "title";
 	private static final String LESSON_PAGES	= "content";
 	private static final String LESSON_LINK		= "link";
 	private static final String LESSON_INAPP	= "inapp";
 	private static final String LESSON_ICON		= "icon";
 	private static final String PAGE_IMAGE		= "image";
 	private static final String PAGE_TEXT		= "text";
 	private static final String LINK_CAT		= "Category";
 	private static final String LINK_LESSON		= "SubCategory";
 	private static final String LINK_PAGE		= "Page";
 	private static final String WEBSITE			= "website";
 	private static final String FACEBOOK		= "facebook";
 	private static final String TWITTER			= "twitter";
 
 	public static Context mContext = null;
 	public static CategoryData[] categories = null;
 	public static CarData[] cars = null;
 	public static String[] catNames = null;
 	public static boolean isInited = false;
 	public static boolean isPurchased = false;	// TODO
 	
 	public static final String JSON_DATA_FILE = "data.dat";
 	public static final String JSON_DATA_PURCHASE_STATE_KEY = "purchaseState";
 	public static final String JSON_DATA_FACEBOOK_SHARE_TEXT = "FACEBOOK_SHARE_TEXT";
 	public static final String JSON_DATA_TWITTER_SHARE_TEXT = "TWITTER_SHARE_TEXT";
 	public static final String JSON_DATA_SETTINGS = "settings";
 	public static final String JSON_DATA_CARS = "cars";
 	public static JSONObject mJsonData;
 	
 	private AppData() {
 	}
 	
 	public static boolean initialize(Context context) {
 		mContext = context;
 		String jsonStr = "";
 		try {
 			InputStreamReader ireader = new InputStreamReader(context.getAssets().open("AppData.json"));
 			BufferedReader br = new BufferedReader(ireader);
 			String str = null;
 			while((str = br.readLine()) != null) {
 				jsonStr += str;
 			}
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		try {
 			JSONArray jsonCats = new JSONArray(jsonStr);
 			categories = new CategoryData[jsonCats.length()];
 			for (int i = 0; i < jsonCats.length(); i ++) {
 				categories[i] = new CategoryData(jsonCats.getJSONObject(i));
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Log.d("appdata", jsonStr);
 			return false;
 		}
 		
		if (!loadData()) {
 			return false;
 		}
 		
 		// TODO load car data
 		cars = new CarData[0];
 		loadCarData();
 		
 		isInited = true;
 		return true;
 	}
 	
 
 	/////////////////////////////////////////////////////////////////////////
 	
 
	public static boolean loadData() {
		String jsonString = Util.readFromFile(mContext, JSON_DATA_FILE);
 		mJsonData = null;
 		if (null != jsonString) {
 			try {
 				mJsonData = new JSONObject(jsonString);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		if (null == mJsonData) {
 			mJsonData = new JSONObject();
 			return false;
 		}
 		if (mJsonData.has(JSON_DATA_PURCHASE_STATE_KEY)) {
 			try {
 				String state = mJsonData.getString(JSON_DATA_PURCHASE_STATE_KEY);
				if (state == getPurchasedHashKay()) {
 					AppData.isPurchased = true;
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	
 	public static boolean saveData() {
 		if (null == mJsonData) {
 			return false;
 		}
 		String jsonString = mJsonData.toString();
 		if (jsonString == null) {
 			return false;
 		}
 		return Util.writeToFile(mContext, jsonString, JSON_DATA_FILE);
 	}
 	
 	public static void put(String key, Object value) throws JSONException {
 		mJsonData.put(key, value);
 		saveData();
 	}
 	
 	public static boolean has(String key) {
 		return mJsonData.has(key);
 	}
 	
 	public static JSONObject getData() {
 		return mJsonData;
 	}
 	
 	public static int getInt(String key) throws JSONException {
 		return mJsonData.getInt(key);
 	}
 	
 	public static String getString(String key) throws JSONException {
 		return mJsonData.getString(key);
 	}
 	
 	public static boolean getBoolean(String key) throws JSONException {
 		return mJsonData.getBoolean(key);
 	}
 	
 	public static JSONObject getJson(String key) throws JSONException {
 		return mJsonData.getJSONObject(key);
 	}
 	
 	
 	/////////////////////////////////////////////////////////////////////////////
 	
 	public static void setPurchased(boolean purchased, Context context) {
 		String purchaseStatus = "False"; 
 		if (purchased) {
			purchaseStatus = getPurchasedHashKay();
 		}
 		try {
 			AppData.put(AppData.JSON_DATA_PURCHASE_STATE_KEY, purchaseStatus);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		AppData.isPurchased = purchased;
 	}
 	
	public static String getPurchasedHashKay() {
		String ANDROID_ID = Settings.System.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
 		if (null == ANDROID_ID) {
 			ANDROID_ID = "default_id";
 		}
 		return Util.hashMD5(ANDROID_ID + "__Purchased__");
 	}
 	
 	////////////////////////////////////////////////////////////////////////////
 	
 	public static int getCarCount() {
 		return cars.length;
 	}
 	
 	public static CarData getCarData(int index) {
 		return cars[index];
 	}
 	
 	public static void saveCarData() {
 		// TODO save car data
 	}
 	
 	public static void loadCarData() {
 		// TODO load car data
 	}
 	
 	////////////////////////////////////////////////////////////////////////////
 	
 	public static CategoryData getCategory(int catID) {
 		assert(isInited);
 		return categories[catID];
 	}
 	
 	public static String[] getTitles() {
 		assert(isInited);
 		if (catNames != null)
 			return catNames;
 		catNames = new String[categories.length];
 		for (int i = 0; i < categories.length; i++) {
 			if (null != categories[i]) {
 				catNames[i] = categories[i].title;
 			} else {
 				catNames[i] = i + ": ERROR";
 			}
 		}
 		return catNames;
 	}
 	
 	public static int findCategoryByTitle(String title) {
 		for (int i = 0; i < categories.length; i++) {
 			if (null != categories[i] && categories[i].title.equals(title)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static class CarData {
 		public String name = "";
 		// TODO 
 	}
 	
 	public static class CategoryData {
 		public String title = "";
 		public String icon = "";
 		public LessonData[] lessons = null;
 		public String[] lessonTitles = null;
 		public String website = null;
 		public String facebook = null;
 		public String twitter = null;
 		
 		CategoryData(JSONObject json) throws JSONException {
 			JSONArray jsonLessons = null;
 			title = json.getString(CAT_TITLE);
 			if (json.has(CAT_LESSONS)) {
 				jsonLessons = json.getJSONArray(CAT_LESSONS);
 				if (null != jsonLessons) {
 					lessons = new LessonData[jsonLessons.length()];
 					for (int i = 0; i < jsonLessons.length(); i++) {
 						lessons[i] = new LessonData(jsonLessons.getJSONObject(i));
 					}
 				}
 			} else if (json.has(WEBSITE)) {
 				website = json.getString(WEBSITE);
 			} else if (json.has(FACEBOOK)) {
 				facebook = json.getString(FACEBOOK);
 			} else if (json.has(TWITTER)) {
 				twitter = json.getString(TWITTER);
 			}
 			if (json.has(CAT_ICON)) {
 				icon = json.getString(CAT_ICON);
 			}
 		}
 		
 		public String[] getTitles() {
 			if (lessonTitles != null)
 				return lessonTitles;
 			lessonTitles = new String[lessons.length];
 			for (int i = 0; i < lessons.length; i ++) {
 				if (null != lessons[i]) {
 					lessonTitles[i] = lessons[i].title;
 				} else {
 					lessonTitles[i] = i + ": ERROR ";
 				}
 			}
 			return lessonTitles;
 		}
 
 		public LessonData getLesson(int index) {
 			return lessons[index];
 		}
 		
 		public int findLessonByTitle(String title) {
 			for (int i = 0; i < lessons.length; i++) {
 				if (null != lessons[i] && lessons[i].title.equals(title)) {
 					return i;
 				}
 			}
 			return -1;
 		}
 	}
 	
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static class LessonData {
 		public String title = "";
 		public PageData[] pages = null;
 		public LinkData link = null;
 		public String icon = null;
 		public boolean isInAppBilling = false;
 		
 		LessonData(JSONObject json) throws JSONException {
 			title = json.getString(LESSON_TITLE);
 			if (json.has(LESSON_PAGES)) {
 				JSONArray jsonPages = json.getJSONArray(LESSON_PAGES);
 				if (null != jsonPages) {
 					pages = new PageData[jsonPages.length()];
 					for (int i = 0; i < jsonPages.length(); i++) {
 						pages[i] = new PageData(jsonPages.getJSONObject(i));
 					}
 				}
 			} else if (json.has(LESSON_LINK)) {
 				link = new LinkData(json.getJSONObject(LESSON_LINK));
 			} else {
 				assert(false);
 			}
 			if (json.has(LESSON_INAPP)) {
 				isInAppBilling = json.getBoolean(LESSON_INAPP);
 			}
 			if (json.has(LESSON_ICON)) {
 				icon = json.getString(LESSON_ICON);
 			}
 		}
 		
 		public PageData getPage(int index) {
 			return pages[index];
 		}
 		
 		public boolean canAccess() {
 			return !isInAppBilling || AppData.isPurchased; 
 		}
 	}
 	
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static class PageData {
 		public String image = "";
 		public String text = "";
 		
 		PageData(JSONObject json) throws JSONException {
 			image = json.getString(PAGE_IMAGE);
 			text = json.getString(PAGE_TEXT);
 		}
 	}
 	
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static class LinkData {
 		public String category = null;
 		public String lesson = null;
 		public int page = 0;
 		
 		LinkData(JSONObject json) throws JSONException {
 			category = json.getString(LINK_CAT);
 			lesson = json.getString(LINK_LESSON);
 			page = json.getInt(LINK_PAGE);
 		}
 	}
 }
 
 
