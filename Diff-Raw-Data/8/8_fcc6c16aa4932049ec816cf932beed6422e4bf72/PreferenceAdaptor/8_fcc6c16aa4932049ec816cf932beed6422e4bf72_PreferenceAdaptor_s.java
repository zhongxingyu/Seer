 package net.trajano.gasprices;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.text.format.Time;
 import android.util.Log;
 
 /**
  * <p>
  * This is a adaptor that is used to manage the {@link SharedPreferences} used
  * by the application so that it shields the clients from knowing what the
  * actual properties are. This pattern was used for readability at the expense
  * of performance loss for creating the object.
  * </p>
  * <p>
  * A non-private {@link SharedPreferences} was used because we need it to be
  * accessed by a widget which is not running in the same user space as the
  * application therefore won't be able to get it by using
  * {@link Activity#getPreferences(int)}.
  * </p>
  * 
  * @author Archimedes Trajano (developer@trajano.net)
  * 
  */
 public final class PreferenceAdaptor implements SharedPreferences {
 	/**
 	 * City data key prefix.
 	 */
 	static final String CITY_DATA_KEY_PREFIX = "city_";
 
 	/**
 	 * Default city ID. Uses Toronto.
 	 */
 	private static final long DEFAULT_CITY_ID = 133;
 
 	/**
 	 * This is the actual data, but it is only for error situations.
 	 */
 	static final String FEED_DATA_KEY = "data";
 
 	/**
 	 * JSON data key.
 	 */
 	static final String JSON_DATA_KEY = "json_data";
 
 	/**
 	 * Last error key.
 	 */
 	static final String LAST_ERROR_KEY = "last_error";
 
 	/**
 	 * Last updated in seconds since epoch.
 	 */
 	static final String LAST_UPDATED_KEY = "last_updated"; // $NON-NLS-1$
 
 	/**
 	 * This is the selected city ID.
 	 */
 	static final String SELECTED_CITY_ID_KEY = "selected_city_id";
 
 	/**
 	 * This is the selected city name.
 	 */
 	static final String SELECTED_CITY_NAME_KEY = "selected_city_name";
 
 	/**
 	 * {@link SharedPreferences} file name.
 	 */
 	private static final String SHARED_PREFERENCES_NAME = "gasprices.properties"; // $NON-NLS-1$
 
 	/**
 	 * Widget preference key prefix.
 	 */
 	static final String WIDGET_CITY_ID_PREFERENCE_KEY_PREFIX = "widget_city_id_"; // $NON-NLS-1$
 
 	/**
 	 * This returns true if the Gas Prices view should change if the key was
 	 * updated.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public static boolean isKeyAffectGasPricesView(final String key) {
 		return LAST_UPDATED_KEY.equals(key) || LAST_ERROR_KEY.equals(key)
 				|| SELECTED_CITY_ID_KEY.equals("key");
 	}
 
 	/**
 	 * This returns the next update date given the last update data. There are
 	 * three times for updates: 5pm, 8pm and midnight.
 	 * 
 	 * @param lastUpdate
 	 *            last update time
 	 * @return the next update time.
 	 */
 	public static Date nextUpdateDate(final Date lastUpdate) {
 		return nextUpdateDate(lastUpdate.getTime());
 	}
 
 	/**
 	 * This returns the next update date given the last update data. There are
 	 * three times for updates: 5pm, 8pm and midnight.
 	 * 
 	 * @param lastUpdateTime
 	 *            last update time as seconds since epoch.
 	 * @return the next update time.
 	 */
 	public static Date nextUpdateDate(final long lastUpdateTime) {
 		final Time t = new Time();
 		t.set(lastUpdateTime);
 		if (t.hour < 17) {
 			t.hour = 17;
 			t.minute = 0;
 			t.second = 0;
 		} else if (t.hour < 20) {
 			t.hour = 20;
 			t.minute = 0;
 			t.second = 0;
 		} else {
 			t.monthDay += 1;
 			t.hour = 0;
 			t.minute = 0;
 			t.second = 0;
 		}
 		return new Date(t.normalize(false));
 	}
 
 	/**
 	 * This is the {@link SharedPreferences} object that is being wrapped.
 	 */
 	private final SharedPreferences preferences;
 
 	/**
 	 * Gets the {@link SharedPreferences} object used by the application as
 	 * specified by {@link #SHARED_PREFERENCES_NAME}.
 	 * 
 	 * @param context
 	 *            context.
 	 */
 	public PreferenceAdaptor(final Context context) {
 		preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
 				Context.MODE_PRIVATE);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean contains(final String key) {
 		return preferences.contains(key);
 	}
 
 	/**
 	 * This creates a new editor that wraps the original
 	 * {@link android.content.SharedPreferences.Editor}.
 	 */
 	@Override
 	public PreferenceAdaptorEditor edit() {
 		return new PreferenceAdaptorEditor(preferences.edit());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Map<String, ?> getAll() {
 		return preferences.getAll();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean getBoolean(final String key, final boolean defValue) {
 		return preferences.getBoolean(key, defValue);
 	}
 
 	public CityInfo getCityInfo(final long cityId) {
 		try {
 			return new CityInfo(new JSONObject(preferences.getString(
 					CITY_DATA_KEY_PREFIX + cityId, "")));
 		} catch (final JSONException e) {
 			Log.e("GasPrices", e.getMessage());
 			throw new RuntimeException(e);
 		}
 	}
 
 	public String getFeedData() {
 		return preferences.getString(FEED_DATA_KEY, "");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public float getFloat(final String key, final float defValue) {
 		return preferences.getFloat(key, defValue);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getInt(final String key, final int defValue) {
 		return preferences.getInt(key, defValue);
 	}
 
 	/**
 	 * This gets the preference data pointed to by {@link #JSON_DATA_KEY}.
 	 * 
 	 * @return JSON data as string.
 	 */
 	public String getJsonDataString() {
 		return preferences.getString(JSON_DATA_KEY, null);
 	}
 
 	public String getLastError() {
 		return preferences.getString(LAST_ERROR_KEY, "");
 	}
 
 	/**
 	 * This returns the {@link Date} represening the last updated timestamp.
 	 * 
 	 * @return
 	 */
 	public Date getLastUpdated() {
 		return new Date(preferences.getLong(LAST_UPDATED_KEY, 0));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public long getLong(final String key, final long defValue) {
 		return preferences.getLong(key, defValue);
 	}
 
 	/**
 	 * This returns the next update date based on the {@link #LAST_UPDATED_KEY}.
 	 * 
 	 * @return
 	 */
 	public Date getNextUpdateDate() {
		return nextUpdateDate(preferences.getLong(LAST_UPDATED_KEY, 0));
 	}
 
 	/**
 	 * This provides the next update time as seconds since epoch.
 	 * 
 	 * @return
 	 */
 	public long getNextUpdateTime() {
		return preferences.getLong(LAST_UPDATED_KEY, 0);
 	}
 
 	/**
 	 * This will return the city info the currently selected city or Toronto if
 	 * not found.
 	 * 
 	 * @return
 	 */
 	public CityInfo getSelectedCityInfo() {
 		return getCityInfo(preferences.getLong(SELECTED_CITY_ID_KEY,
 				DEFAULT_CITY_ID));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getString(final String key, final String defValue) {
 		return preferences.getString(key, defValue);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Set<String> getStringSet(final String arg0, final Set<String> arg1) {
 		return preferences.getStringSet(arg0, arg1);
 	}
 
 	/**
 	 * This returns the city id associated with the app widget.
 	 * 
 	 * @param appWidgetId
 	 * @return
 	 */
 	public long getWidgetCityId(final long appWidgetId) {
 		return preferences.getLong(WIDGET_CITY_ID_PREFERENCE_KEY_PREFIX
 				+ appWidgetId, DEFAULT_CITY_ID);
 	}
 
 	/**
 	 * Gets the {@link CityInfo} associated with the widget.
 	 * 
 	 * @param appWidgetId
 	 * @return
 	 */
 	public CityInfo getWidgetCityInfo(final int appWidgetId) {
 		return getCityInfo(getWidgetCityId(appWidgetId));
 	}
 
 	/**
 	 * This checks if the key data is present.
 	 * 
 	 * @return
 	 */
 	public boolean isDataPresent() {
 		return preferences.contains(LAST_UPDATED_KEY)
 				&& preferences.contains(JSON_DATA_KEY);
 	}
 
 	/**
 	 * Checks if there is an error recorded.
 	 * 
 	 * @return
 	 */
 	public boolean isError() {
 		return preferences.contains(LAST_ERROR_KEY);
 	}
 
 	/**
 	 * An updated is needed if the next update time occurs in the past.
 	 * 
 	 * @return
 	 */
 	public boolean isUpdateNeeded() {
 		return getNextUpdateTime() < System.currentTimeMillis();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void registerOnSharedPreferenceChangeListener(
 			final OnSharedPreferenceChangeListener listener) {
 		preferences.registerOnSharedPreferenceChangeListener(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void unregisterOnSharedPreferenceChangeListener(
 			final OnSharedPreferenceChangeListener listener) {
 		preferences.unregisterOnSharedPreferenceChangeListener(listener);
 	}
 }
