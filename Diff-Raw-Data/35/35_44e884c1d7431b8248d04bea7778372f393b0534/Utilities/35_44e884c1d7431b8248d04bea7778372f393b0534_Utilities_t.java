 package com.charlesmadere.android.classygames.utilities;
 
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Shader.TileMode;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Build;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.charlesmadere.android.classygames.R;
 import com.charlesmadere.android.classygames.models.Person;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 
 
 /**
  * Class filled with a bunch of miscellaneous utility methods and constants.
  */
 public final class Utilities
 {
 
 
 	public final static String LOG_TAG = "Classy Games";
 	public final static String SHARED_PREFERENCES_NAME = "CLASSY_PREFERENCES";
 
 
 	private static ImageLoader imageLoader;
 
 
 	private static Person whoAmI;
 	private final static String WHO_AM_I_ID = "WHO_AM_I_ID";
 	private final static String WHO_AM_I_NAME = "WHO_AM_I_NAME";
 
 
 
 
 	/**
 	 * Invalidates the options menu using the Android compatibility library. If
 	 * the force parameter is set to true then this method will even attempt to
 	 * invalidate a pre honeycomb device's menu.
 	 * 
 	 * @param fragmentActivity
 	 * getSherlockActivity()
 	 * 
 	 * @param force
 	 * True if you want to refresh the device's menu no matter what version of
 	 * Android it's running.
 	 */
 	public static void compatInvalidateOptionsMenu(final SherlockFragmentActivity fragmentActivity, final boolean force)
 	{
 		if (fragmentActivity != null)
 		{
 			if (force)
 			{
 				fragmentActivity.invalidateOptionsMenu();
 			}
 			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 			{
 				fragmentActivity.invalidateOptionsMenu();
 			}
 		}
 	}
 
 
 	/**
 	 * Invalidates the options menu using the Android compatibility library.
 	 * 
 	 * @param fragmentActivity
 	 * getSherlockActivity()
 	 */
 	public static void compatInvalidateOptionsMenu(final SherlockFragmentActivity fragmentActivity)
 	{
 		compatInvalidateOptionsMenu(fragmentActivity, false);
 	}
 
 
 	/**
 	 * Prints a Toast message to the screen.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToast(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToast(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message.
 	 */
 	public static void easyToast(final Context context, final String message)
 	{
 		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
 	}
 
 
 	/**
 	 * Prints a Toast message to the screen with a String taken from the
 	 * strings.xml file.
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param stringId
 	 * The int ID of the resource that you want to print.
 	 */
 	public static void easyToast(final Context context, final int stringId)
 	{
 		easyToast(context, context.getString(stringId));
 	}
 
 
 	/**
 	 * Prints a Toast message to the screen and prints that same message to the Log.d
 	 * console.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToastAndLog(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToastAndLog(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message. This exact String will also
 	 * be printed to the Log.d console.
 	 */
 	public static void easyToastAndLog(final Context context, final String message)
 	{
 		easyToast(context, message);
 		Log.d(LOG_TAG, message);
 	}
 
 
 	/**
 	 * Prints a Toast message to the screen and prints that same message to each and
 	 * every log console.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToastAndLogAll(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToastAndLogAll(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message. This exact String will also
 	 * be printed to the Log.d console.
 	 */
 	public static void easyToastAndLogAll(final Context context, final String message)
 	{
 		easyToast(context, message);
 		Log.d(LOG_TAG, message);
 		Log.e(LOG_TAG, message);
 		Log.i(LOG_TAG, message);
 		Log.v(LOG_TAG, message);
 		Log.wtf(LOG_TAG, message);
 	}
 
 
 	/**
 	 * Prints a Toast message to the screen and prints that same message to the
 	 * Log.e console.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.easyToastAndLogError(MainActivity.this, "Hello!");<br />
 	 * Utilities.easyToastAndLogError(getApplicationContext(), "Another message huh?");</p>
 	 * 
 	 * @param context
 	 * Just put the name of your class.this, or you can use getApplicationContext().
 	 * 
 	 * @param message
 	 * The String that you want to be shown as a toast message. This exact String will also
 	 * be printed to the Log.e console.
 	 */
 	public static void easyToastAndLogError(final Context context, final String message)
 	{
 		easyToast(context, message);
 		Log.e(LOG_TAG, message);
 	}
 
 
 	/**
	 * Prints a Toast message to the screen and prints that same message to the
	 * Log.e console.
	 * 
	 * <p><strong>Examples</strong><br />
	 * Utilities.easyToastAndLogError(MainActivity.this, "Hello!");<br />
	 * Utilities.easyToastAndLogError(getApplicationContext(), "Another message huh?");</p>
	 * 
	 * @param context
	 * Just put the name of your class.this, or you can use getApplicationContext().
	 * 
	 * @param stringId
	 * The int ID of the resource that you want to print.
	 */
	public static void easyToastAndLogError(final Context context, final int stringId)
	{
		easyToast(context, stringId);
		Log.e(LOG_TAG, context.getString(stringId));
	}


	/**
 	 * Initializes the ImageLoader library with some specific configuration
 	 * settings (if it has not already been initialized) and returns only what
 	 * you need - the portion that will actually load an image for ya!
 	 * 
 	 * @param context
 	 * The Context of the class you're currently working in.
 	 * 
 	 * @return
 	 * Returns an instance of the ImageLoader class that can load an image from
 	 * a website for ya!
 	 */
 	public static ImageLoader getImageLoader(final Context context)
 	{
 		if (imageLoader == null)
 		{
 			final DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
 				.cacheInMemory()
 				.cacheOnDisc()
 				.build();
 
 			final ImageLoaderConfiguration loaderConfiguration = new ImageLoaderConfiguration.Builder(context)
 				.defaultDisplayImageOptions(displayOptions)
 				.build();
 
 			imageLoader = ImageLoader.getInstance();
 			imageLoader.init(loaderConfiguration);
 		}
 
 		return imageLoader;
 	}
 
 
 	/**
 	 * If the user's Facebook identity is already stored in this class's static
 	 * whoAmI Person variable then that variable will be instantly returned. If
 	 * the whoAmI Person variable is currently null or is not valid, then we
 	 * will search the Android SharedPreferences data for the user's Facebook
 	 * identity.
 	 * 
 	 * @return
 	 * A Person object that represents the user's Facebook identity.
 	 */
 	public static Person getWhoAmI(final Context context)
 	{
 		if (whoAmI == null || !whoAmI.isValid())
 		// check to see if the whoAmI variable is null or if it's not valid. If
 		// it is either of these two conditions then we will pull the user's
 		// Facebook identity from the Android SharedPreferences data.
 		{
 			final SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
 
 			// find the user's Facebook ID. If the ID can't be found then the
 			// id variable will be set to 0.
 			final long id = sharedPreferences.getLong(WHO_AM_I_ID, 0);
 
 			// find the user's Facebook name. If the name can't be found then
 			// the name variable will be set to null.
 			final String name = sharedPreferences.getString(WHO_AM_I_NAME, null);
 
 			if (Person.isIdValid(id) && Person.isNameValid(name))
 			// check to see that we were actually able to find the user's
 			// Facebook ID and Facebook name. If we were able to find both
 			// then we will create a new Person object out of that data. That
 			// Person object will then be returned.
 			{
 				whoAmI = new Person(id, name);
 			}
 		}
 
 		return whoAmI;
 	}
 
 
 	/**
 	 * Stores the current user's Facebook identity into the Android
 	 * SharedPreferences storage system. The current user's Facebook identity
 	 * is frequently used throughout the app, and so doing this allows future
 	 * needings of this data to not require making a Facebook API call.
 	 * 
 	 * @param context
 	 * The context of the Activity that is calling this method.
 	 * 
 	 * @param facebookIdentity
 	 * A Person object representing the current user's Facebook identity.
 	 */
 	public static void setWhoAmI(final Context context, final Person facebookIdentity)
 	{
 		final SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
 		editor.putLong(WHO_AM_I_ID, facebookIdentity.getId());
 		editor.putString(WHO_AM_I_NAME, facebookIdentity.getName());
 		editor.commit();
 
 		whoAmI = facebookIdentity;
 	}
 
 
 	/**
 	 * This is a workaround for http://b.android.com/15340 from http://stackoverflow.com/a/5852198/132047.
 	 * This ensures that pre ice cream sandwich devices properly render our customized actionbar.
 	 * This method should always be run immediately after the setContentView() method is run.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Utilities.styleActionBar(getResources(), getSupportActionBar(), false);</p>
 	 * 
 	 * @param resources
 	 * getResources()
 	 * 
 	 * @param actionBar
 	 * getSupportActionBar()
 	 * 
 	 * @param backArrow
 	 * Whether or not you want to have a back arrow drawn next to the app icon in the actionbar.
 	 */
 	public static void styleActionBar(final Resources resources, final ActionBar actionBar, final boolean backArrow)
 	{
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 		// if the running version of Android is lower than API Level 14 (below Ice Cream Sandwich 4.0)
 		// https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
 		{
 			final BitmapDrawable bg = (BitmapDrawable) resources.getDrawable(R.drawable.bg_actionbar);
 			bg.setAntiAlias(true);
 			bg.setDither(true);
 			bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
 
 			actionBar.setBackgroundDrawable(bg);
 		}
 
 		if (backArrow)
 		{
 			actionBar.setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 
 	/**
 	 * Verifies a String object for validity.
 	 * 
 	 * @param string
 	 * The String to check.
 	 * 
 	 * @return
 	 * Returns true if the given String is valid.
 	 */
 	public static boolean verifyValidString(final String string)
 	{
 		return string != null && string.length() >= 1;
 	}
 
 
 	/**
 	 * Verifies a set of String objects for validity.
 	 * 
 	 * @param strings
 	 * The Strings to check.
 	 * 
 	 * @return
 	 * Returns true if all of the given Strings are valid.
 	 */
 	public static boolean verifyValidStrings(final String... strings)
 	{
 		for (int i = 0; i < strings.length; ++i)
 		{
 			if (!verifyValidString(strings[i]))
 			{
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 
 }
