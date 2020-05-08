 package com.slideme.sam.manager;
 import com.slideme.sam.manager.model.domain.*;
 import com.slideme.sam.manager.model.installer.*;
 import android.content.pm.*;
 import android.content.res.*;
 import android.content.*
 import com.slideme.sam.manager.view.adapters.*;
 import com.slideme.sam.manager.model.android.services.*
 import android.os.*;
 
 public final class SAM extends android.app.Application
 {
 	public static final int DEFAULT_APPS_PER_PAGE = 0x1e;
 	public static final boolean DEFAULT_BUG_REPORTING = true;
 	public static final boolean DEFAULT_FREE_APPS = false;
 	public static final int DEFAULT_NETWORK_TIMEOUT = 0xa;
 	
 	public static String DEFAULT_COUNTRY = null;
 	public static String DEFAULT_DUMP_LEVEL = null;
 	public static String DEFAULT_X_SCREENSHOTS = "3";
 	
 	public List<AppInfo> appInfo;
 	public BundleIdsSdCardAdapter mDbHelper;
 	
 	private static final HashMap<Integer, Object> map = new HashMap<Integer,Object>(0xa);
 	private static String deviceId;
 	private static SAM instance = null;
 	private static ApplicationSettings settings = null;
 	
 	private List<PackageInfo> activities = null;
 	private String deviceInfoUrl;
 	private List <ResolveInfo> launchers = null;
 	private List<PackageInfo> packages = null;
 	private SAMService service;
	
 	public SAM() 
 	{
 		super();
 	}
 
 	public static SAM getInstance()
 	{
 		if (instance == null)
 		{
 			throw new IllegalStateException("Not initialized yet");
 		}
 		return instance;
 	}
 	
 	public static String getSdCardPath()
 	{
 			StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getPath());
 			sb.append("/SlideME/");
 			return sb.toString();
 	}
 	
 	public static ApplicationSettings getSettings()
 	{
 		if (settings == null)
 		{
 			throw new IllegalStateException("Initialize first.");
 		}
 		return this.settings;
 	}
 	
 	private String matchCountryName(String simCountry)
 	{
 		Resources res = getResources();
 		String[] iso = res.getStringArray(R.array.listCountryIso);
 		for (int i = 0 ; i < iso.Length; i++)
 		{
 			String v4 = iso[i];
 			if (v4.equalsIgnoreCase(simCountry)
 			{
 				String[] country = res.getStringArray(R.array.listCountries);
 				return country[i];
 			}
 			
 		}
 		return getString(R.string.category_unknown);
 	}
 
 	private void refreshActivities()
 	{
 		activites = PackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
 	}
 
 	private void refreshLaunchers()
 	{
 		Intent mainIntent = new Intent("android.intent.action.MAIN");
		mainIntent.addCategory("android.intent.category.LAUNCHER");
 		launchers = getPackageManager().queryIntentActivities(mainIntent,0);
 	}
 	
 	private void refreshPackages()
 	{
 		packages = PackageManager.getInstalledPackages(0);
 	}
 
 	private restoreFromSP(ApplicationSettings settings, SharedPreferences defaultSharedPreferences)
 	{
 		
 		String v2 = getString(R.String.key_settings_debug_enable_bug_reporting);
 		settings.getDebugSettings().setBugReporting(defaultSharedPreferences.getBoolean(v2,true));
 		
 		v2 = getString(R.String.key_settings_network_connection_timeout);
 		settings.getNetworkingSettings().setNetworkTimeout(defaultSharedPreferences.getInt(v2,0xa));
 		
 		ApplicationSettings.GeneralSettings general = settings.getGeneralSettings();
 		
 		v2 = getString(R.String.key_settings_general_country);
 		general.setCountry(defaultSharedPreferences.getString(v2,DEFAULT_COUNTRY));
 		
 		v2 = getString(R.String.key_settings_general_x_screenshots);
 		general.setXScreenshots(Integer.parseInt(defaultSharedPreferences.getString(v2,DEFAULT_X_SCREENSHOTS)));
 		
 		v2 = getString(R.String.key_settings_general_apps_per_page);
 		general.setAppsPerPage(defaultSharedPreferences.getInt(v2,0x1e));
 		
 		String v2 = getString(R.String.key_settings_general_free_apps);
 		settings.getDebugSettings().setFreeApps(defaultSharedPreferences.getBoolean(v2,false));
 	}
 	
 	private boolean testVersion(String version, String versionName)
 	{
 		if (version == null || version.equals(versionName))
 		{
 			return true;
 		}else
 		{
 			return false;
 		}
 	}
 	
 	public boolean checkAndroidVersions()
 	{
 		try
 		{
 			FileInputStream versions = openFileInput("sam.android_versions");
 			if (versions == null)
 			{
 				return true;
 			}
 			versions.close();
 		} catch (IOException e){ return true;}
 		return false;
 	}
 	
 	public boolean checkConfig()
 	{
 		try
 		{
 			FileInputStream versions = openFileInput("sam.config");
 			if (versions == null)
 			{
 				return true;
 			}
 			versions.close();
 		} catch (IOException e){ return true;}
 		return false;
 	}
 
 	public void createIni()
 	{
 		
 	}
 	
 }
