 /*
  * ApplicationAptoide, part of Aptoide
  * Copyright (C) 2012 Duarte Silveira
  * duarte.silveira@caixamagica.pt
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 package cm.aptoide.pt;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Build;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 import cm.aptoide.com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
 import cm.aptoide.com.nostra13.universalimageloader.core.DisplayImageOptions;
 import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader;
 import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import cm.aptoide.com.nostra13.universalimageloader.core.assist.FlushedInputStream;
 import cm.aptoide.com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 import cm.aptoide.com.nostra13.universalimageloader.core.download.ImageDownloader;
 import cm.aptoide.pt.preferences.ManagerPreferences;
 import cm.aptoide.pt.services.ServiceManagerDownload;
 import cm.aptoide.pt.util.Constants;
 import cm.aptoide.pt.util.NetworkUtils;
 import org.acra.ACRA;
 import org.acra.ReportingInteractionMode;
 import org.acra.annotation.ReportsCrashes;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.*;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * ApplicationAptoide, centralizes, statically, calls to instantiated objects
  *
  * @author dsilveira
  *
  */
 @ReportsCrashes(
         reportType = org.acra.sender.HttpSender.Type.JSON,
         httpMethod = org.acra.sender.HttpSender.Method.PUT,
 //        formUriBasicAuthLogin="",
 //        formUriBasicAuthPassword="",
    		formKey = "",
    		formUri = "http://acra.aptoide.com/acraaptoide",
         mode = ReportingInteractionMode.NOTIFICATION,
         resToastText = R.string.crash_dialog_title, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
         resNotifTickerText = R.string.crash_notif_ticker_text,
         resNotifTitle = R.string.crash_dialog_title,
         resNotifText = R.string.crash_notif_text,
         resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
         resDialogText = R.string.crash_dialog_text,
         resDialogIcon = R.drawable.icon_brand_aptoide, //optional. default is a warning sign
         resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
         resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
         resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
 )
 
 public class ApplicationAptoide extends Application {
 
 	private ManagerPreferences managerPreferences;
 	private static Context context;
 	public static boolean DEBUG_MODE = false;
 	public static File DEBUG_FILE;
 	public static String PARTNERTYPE;
 	public static String PARTNERID;
     public static String DEFAULTSTORENAME;
     public static boolean MATURECONTENTSWITCH = true;
     public static String SPLASHSCREEN;
     public static String SPLASHSCREENLAND;
     public static String BRAND = "";
     public static boolean MATURECONTENTSWITCHVALUE = true;
     public static boolean MULTIPLESTORES = true;
     public static boolean CUSTOMEDITORSCHOICE = false;
     public static boolean SEARCHSTORES = true;
     public static String APTOIDETHEME = "";
     public static String MARKETNAME = "";
     public static String ADUNITID = "";
 
     public static String THEME = null;
     public static String AVATAR = null;
     public static String DESCRIPTION = null;
     public static String VIEW = null;
     public static String ITEMS = null;
     private static boolean restartLauncher;
 	private static boolean reDoLauncherShorcut;
     private String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
 
     public static boolean isRestartLauncher() {
         return restartLauncher;
     }
 
     public static void setRestartLauncher(boolean restartLauncher) {
         ApplicationAptoide.restartLauncher = restartLauncher;
     }
 
     static enum Elements { BOOTCONF, APTOIDECONF, PARTNERTYPE, PARTNERID, DEFAULTSTORENAME, BRAND, SPLASHSCREEN, MATURECONTENTSWITCH, MATURECONTENTSWITCHVALUE,SEARCHSTORES, MULTIPLESTORES, CUSTOMEDITORSCHOICE, APTOIDETHEME, SPLASHSCREENLAND, MARKETNAME, ADUNITID,
     		STORECONF, THEME, AVATAR, DESCRIPTION, VIEW, ITEMS }
 
     SharedPreferences sPref;
 	@Override
 	public void onCreate() {
 
 		ACRA.init(this);
 //
 
         startService(new Intent(this, ServiceManagerDownload.class));
 
 		AptoideThemePicker.setAptoideTheme(this);
 		setContext(getApplicationContext());
 
 
 
         sPref = getSharedPreferences("settings", MODE_PRIVATE);
         if(sPref.contains("PARTNERID") && sPref.getString("PARTNERID", null) != null){
 
             PARTNERID = sPref.getString("PARTNERID",null);
             DEFAULTSTORENAME = sPref.getString("DEFAULTSTORE",null);
             MATURECONTENTSWITCH = sPref.getBoolean("MATURECONTENTSWITCH", true);
             BRAND = sPref.getString("BRAND", null);
             SPLASHSCREEN = sPref.getString("SPLASHSCREEN", null);
             SPLASHSCREENLAND = sPref.getString("SPLASHSCREEN_LAND", null);
             MATURECONTENTSWITCHVALUE = sPref.getBoolean("MATURECONTENTSWITCHVALUE", true);
             MULTIPLESTORES = sPref.getBoolean("MULTIPLESTORES",true);
             CUSTOMEDITORSCHOICE = sPref.getBoolean("CUSTOMEDITORSCHOICE",false);
             SEARCHSTORES = sPref.getBoolean("SEARCHSTORES",true);
             APTOIDETHEME = sPref.getString("APTOIDETHEME","DEFAULT");
             MARKETNAME = sPref.getString("MARKETNAME", "Aptoide");
             ADUNITID = sPref.getString("ADUNITID", "18947d9a99e511e295fa123138070049");
 
             if(!BRAND.equals("brand_aptoide")&&!new File(SDCARD + "/.aptoide_settings/oem").exists()){
                 createSdCardBinary();
             }
 
 


         }else if(new File(SDCARD + "/.aptoide_settings/oem").exists()){
 
             try {
                 Toast.makeText(this, "Regenerating settings from SDCard.", Toast.LENGTH_LONG).show();
                 HashMap<String, String> map = (HashMap<String, String>) new ObjectInputStream(new FileInputStream(new File(SDCARD + "/.aptoide_settings/oem"))).readObject();
 
                 PARTNERID = map.get("PARTNERID");
                 DEFAULTSTORENAME = map.get("DEFAULTSTORE");
                 MATURECONTENTSWITCH = Boolean.parseBoolean(map.get("MATURECONTENTSWITCH"));
                 BRAND = map.get("BRAND");
                 SPLASHSCREEN = map.get("SPLASHSCREEN");
                 SPLASHSCREENLAND = map.get("SPLASHSCREEN_LAND");
                 MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(map.get("MATURECONTENTSWITCHVALUE"));
                 MULTIPLESTORES = Boolean.parseBoolean(map.get("MULTIPLESTORES"));
                 CUSTOMEDITORSCHOICE = Boolean.parseBoolean(map.get("CUSTOMEDITORSCHOICE"));
                 SEARCHSTORES = Boolean.parseBoolean(map.get("SEARCHSTORES"));
                 APTOIDETHEME = map.get("APTOIDETHEME");
                 MARKETNAME = map.get("MARKETNAME");
                 ADUNITID = map.get("ADUNITID");
 
                 if(isUpdate()){
                    BufferedInputStream is = new BufferedInputStream(new URL("http://"+ DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                     parseBootConfigStream(is);
                 }
 
                 savePreferences();
 
 
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (PackageManager.NameNotFoundException e) {
                 e.printStackTrace();
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();
             } catch (SAXException e) {
                 e.printStackTrace();
             }
 
         }else{
             try {
                 InputStream inputStream = getAssets().open("boot_config.xml");
                 parseBootConfigStream(inputStream);
 
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();
             } catch (SAXException e) {
                 e.printStackTrace();
             }
 
         }
 
 
 
 
 
         DEBUG_FILE = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.aptoide/debug.log");
 
 		if(DEBUG_FILE.exists()){
 			DEBUG_MODE = true;
 		}
 
 		DisplayImageOptions options = new DisplayImageOptions.Builder()
 															 .displayer(new FadeInBitmapDisplayer(1000))
 															 .showStubImage(android.R.drawable.sym_def_app_icon)
 															 .resetViewBeforeLoading()
 															 .cacheInMemory()
 															 .cacheOnDisc()
 															 .build();
 
 		ImageLoaderConfiguration config;
 
 		if(DEBUG_MODE){
 			config = new ImageLoaderConfiguration.Builder(getApplicationContext())
 																				  .defaultDisplayImageOptions(options)
 																				  .discCache(new UnlimitedDiscCache(new File(Environment.getExternalStorageDirectory().getPath()+"/.aptoide/icons/")))
 																				  .enableLogging()
 																				  .imageDownloader(new ImageDownloaderWithPermissions())
 																				  .build();
 		}else{
 			config = new ImageLoaderConfiguration.Builder(getApplicationContext())
 																				  .defaultDisplayImageOptions(options)
 																				  .discCache(new UnlimitedDiscCache(new File(Environment.getExternalStorageDirectory().getPath()+"/.aptoide/icons/")))
 																				  .imageDownloader(new ImageDownloaderWithPermissions())
 																				  .build();
 		}
 
         ImageLoader.getInstance().init(config);
 
 
         if(ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("jblow")){
 			ApplicationAptoide.BRAND = "brand_jblow";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_jblow);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("magalhaes")){
 			ApplicationAptoide.BRAND = "brand_magalhaes";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_magalhaes);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("timwe")){
 			ApplicationAptoide.BRAND = "brand_timwe";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_timwe);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("digitallydifferent")){
 			ApplicationAptoide.BRAND = "brand_digitallydifferent";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_digitallydifferent);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("eocean")){
 			ApplicationAptoide.BRAND = "brand_eocean";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_eocean);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.APTOIDETHEME.equalsIgnoreCase("lazerplay")){
 			ApplicationAptoide.BRAND = "brand_lazerplay";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_lazerplay);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.BRAND.equalsIgnoreCase("brand_educomp")){
 			ApplicationAptoide.BRAND = "brand_educomp";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_educomp);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 
 		}else if(ApplicationAptoide.BRAND.equalsIgnoreCase("brand_peoplenet")){
 			ApplicationAptoide.BRAND = "brand_peoplenet";
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("BRAND", ApplicationAptoide.BRAND).commit();
 			ApplicationAptoide.MARKETNAME = getString(R.string.app_name_peoplenet);
 			getSharedPreferences("settings", MODE_PRIVATE).edit().putString("MARKETNAME", ApplicationAptoide.MARKETNAME).commit();
 		}
 		super.onCreate();
 
 
         try {
             if (isUpdate()) {
                 ManagerPreferences.removePreviousShortcuts(this, false);
                 ManagerPreferences.removePreviousShortcuts(this, true);
             }
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
 
         try{
         	managerPreferences = new ManagerPreferences(getApplicationContext());
         	if (ApplicationAptoide.BRAND.equals("brand_aptoide") && isUpdate()) {
         		managerPreferences.createLauncherShortcut(this);
         	}
         } catch (PackageManager.NameNotFoundException e) {
         	e.printStackTrace();
         }
 
     }
 
     private boolean isUpdate() throws PackageManager.NameNotFoundException {
         return PreferenceManager.getDefaultSharedPreferences(this).getInt("version", 0) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
     }
 
     private void parseBootConfigStream(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser parser = factory.newSAXParser();
 
 
         parser.parse(inputStream,new DefaultHandler(){
             StringBuilder sb = new StringBuilder();
 
             @Override
             public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                 super.startElement(uri, localName, qName, attributes);
                 sb.setLength(0);
             }
 
 
             @Override
             public void characters(char[] ch, int start, int length) throws SAXException {
                 super.characters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
                 sb.append(ch,start,length);
             }
 
             @Override
             public void endElement(String uri, String localName, String qName) throws SAXException {
                 super.endElement(uri, localName, qName);
                 try{
                 Elements element = Elements.valueOf(localName.toUpperCase(Locale.ENGLISH));
                     switch (element) {
                         case PARTNERTYPE:
                             PARTNERTYPE = sb.toString();
                             Log.d("Partner type", PARTNERTYPE + "");
                         break;
                         case PARTNERID:
                             PARTNERID = sb.toString();
                             Log.d("Partner ID", PARTNERID + "");
                             break;
                         case DEFAULTSTORENAME:
                             DEFAULTSTORENAME = sb.toString();
                             Log.d("Default store", DEFAULTSTORENAME + "");
                             break;
                         case BRAND:
                             BRAND = sb.toString();
                             Log.d("Brand", BRAND+ "");
                             break;
                         case SPLASHSCREEN:
                             SPLASHSCREEN = sb.toString();
                             Log.d("Splashscreen", SPLASHSCREEN+ "");
                             break;
                         case SPLASHSCREENLAND:
                             SPLASHSCREENLAND = sb.toString();
                             Log.d("Splashscreen landscape", SPLASHSCREENLAND+ "");
                             break;
                         case MATURECONTENTSWITCH:
                             MATURECONTENTSWITCH = Boolean.parseBoolean(sb.toString());
                             Log.d("Mature content Switch", MATURECONTENTSWITCH+ "");
                             break;
                         case MATURECONTENTSWITCHVALUE:
                             MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(sb.toString());
                             Log.d("Mature content value", MATURECONTENTSWITCHVALUE+ "");
                             break;
                         case MULTIPLESTORES:
                             MULTIPLESTORES = Boolean.parseBoolean(sb.toString());
                             Log.d("Multiple stores", MULTIPLESTORES+ "");
                             break;
                         case CUSTOMEDITORSCHOICE:
                             CUSTOMEDITORSCHOICE = Boolean.parseBoolean(sb.toString());
                             Log.d("Custom editors choice", CUSTOMEDITORSCHOICE+ "");
                             break;
                         case APTOIDETHEME:
                             APTOIDETHEME = sb.toString();
                             Log.d("Aptoide theme", APTOIDETHEME+ "");
                             break;
                         case MARKETNAME:
                             MARKETNAME = sb.toString();
                             Log.d("Market name", MARKETNAME+ "");
                             break;
                         case SEARCHSTORES:
                             SEARCHSTORES = Boolean.parseBoolean(sb.toString());
                             Log.d("Search stores", SEARCHSTORES+ "");
                             break;
                         case ADUNITID:
                             ADUNITID = sb.toString();
                             Log.d("AdUnitId", ADUNITID+ "");
                             break;
 
                         case THEME:
                             THEME = sb.toString();
                             Log.d("Store Theme", THEME+ "");
                             break;
                         case AVATAR:
                             AVATAR = sb.toString();
                             Log.d("Store avatar", AVATAR+ "");
                             break;
                         case DESCRIPTION:
                             DESCRIPTION = sb.toString();
                             Log.d("Store description", DESCRIPTION+ "");
                             break;
                         case ITEMS:
                             ITEMS = sb.toString();
                             Log.d("Store items", ITEMS+ "");
                             break;
                         case VIEW:
                             VIEW = sb.toString();
                             Log.d("Store view", VIEW+ "");
                             break;
 
                     }
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }
         });
 
         savePreferences();
 
         createSdCardBinary();
     }
 
     private void savePreferences() {
         sPref.edit().putString("PARTNERID", PARTNERID).putString("DEFAULTSTORE", DEFAULTSTORENAME)
                 .putBoolean("MATURECONTENTSWITCH", MATURECONTENTSWITCH)
                 .putString("BRAND", BRAND)
                 .putString("SPLASHSCREENLAND", SPLASHSCREENLAND)
                 .putString("SPLASHSCREEN", SPLASHSCREEN)
                 .putBoolean("MATURECONTENTSWITCHVALUE", MATURECONTENTSWITCHVALUE)
                 .putBoolean("MULTIPLESTORES", MULTIPLESTORES)
                 .putBoolean("CUSTOMEDITORSCHOICE", CUSTOMEDITORSCHOICE)
                 .putBoolean("SEARCHSTORES", SEARCHSTORES)
                 .putString("APTOIDETHEME", APTOIDETHEME)
                 .putString("MARKETNAME", MARKETNAME)
                 .putString("ADUNITID", ADUNITID)
                 .commit();
     }
 
     private void createSdCardBinary() {
         if(!BRAND.equals("brand_aptoide")){
 
             HashMap<String, String> map = new HashMap<String, String>();
 
             map.put("PARTNERID", PARTNERID);
             map.put("DEFAULTSTORE", DEFAULTSTORENAME);
             map.put("MATURECONTENTSWITCH", MATURECONTENTSWITCH + "");
             map.put("BRAND", BRAND);
             map.put("SPLASHSCREENLAND", SPLASHSCREENLAND);
             map.put("SPLASHSCREEN", SPLASHSCREEN);
             map.put("MATURECONTENTSWITCHVALUE", MATURECONTENTSWITCHVALUE + "");
             map.put("MULTIPLESTORES", MULTIPLESTORES + "");
             map.put("CUSTOMEDITORSCHOICE", CUSTOMEDITORSCHOICE + "");
             map.put("SEARCHSTORES", SEARCHSTORES + "");
             map.put("APTOIDETHEME", APTOIDETHEME);
             map.put("MARKETNAME", MARKETNAME);
             map.put("ADUNITID", ADUNITID);
 
             try {
                 File fileDir = new File(SDCARD + "/.aptoide_settings");
                 if(fileDir.mkdir()){
                     ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(SDCARD + "/.aptoide_settings/oem")));
                     oos.writeObject(map);
                     oos.close();
 
                 }
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
     public ManagerPreferences getManagerPreferences(){
 		return managerPreferences;
 	}
 
     public static void replaceOemIcon(){
     	android.content.SharedPreferences sPref = context.getSharedPreferences("settings", MODE_PRIVATE);
 
         if(sPref.contains("PARTNERID")){
             PackageManager pm = context.getPackageManager();
 
             for (EnumOem enumOem : EnumOem.values()) {
 
                 if (enumOem.equals(EnumOem.valueOf(BRAND.toUpperCase(Locale.ENGLISH)))) {
 
                     pm.setComponentEnabledSetting(new ComponentName(context, "cm.aptoide.pt.Start-" + enumOem.name().toLowerCase(Locale.ENGLISH)),
                             PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                             PackageManager.DONT_KILL_APP);
 
                     Constants.APTOIDE_CLASS_NAME = "cm.aptoide.pt.Start-" + enumOem.name().toLowerCase(Locale.ENGLISH);
                     reDoLauncherShorcut = true;
 
                     Log.d("ApplicationAptoide", "Setting " + enumOem.name().toLowerCase(Locale.ENGLISH) + " enabled");
 
                 } else {
 
                     pm.setComponentEnabledSetting(new ComponentName(context, "cm.aptoide.pt.Start-" + enumOem.name().toLowerCase(Locale.ENGLISH)),
                             PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                             PackageManager.DONT_KILL_APP);
 
                     Log.d("ApplicationAptoide", "Setting " + enumOem.name().toLowerCase(Locale.ENGLISH) + " disabled");
 
 
                 }
 
                 pm.setComponentEnabledSetting(new ComponentName(context, "cm.aptoide.pt.Start"),
                         PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                         PackageManager.DONT_KILL_APP);
 
             }
         }
 
     }
 
     @SuppressLint("NewApi")
 	public static void restartLauncher(Context context) {
         try{
 
 
 
             ManagerPreferences.removePreviousShortcuts2(getContext());
             replaceOemIcon();
             PackageManager pm = context.getPackageManager();
             ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
 
             try{
                 if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD){
                     Intent i = new Intent(Intent.ACTION_MAIN);
                     i.addCategory(Intent.CATEGORY_HOME);
                     i.addCategory(Intent.CATEGORY_DEFAULT);
                     List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
                     for (ResolveInfo res : resolves) {
                         if (res.activityInfo != null) {
                             am.killBackgroundProcesses(res.activityInfo.packageName);
                             Log.d("ApplicationAptoide", "Killing: " + res.activityInfo.packageName);
                         }
                     }
                 }
             }catch (Exception e){
                 e.printStackTrace();
             }
         }catch (Exception e){
             e.printStackTrace();
         }
 
 
         new ManagerPreferences(getContext()).createLauncherShortcut(getContext());
         Log.d("ApplicationAptoide", "End restartLauncher");
 
 
     }
 
 	/**
 	 * @return the context
 	 */
 	public static Context getContext() {
 		return context;
 	}
 
 	public class ImageDownloaderWithPermissions extends ImageDownloader{
 
 		/** {@value} */
 		public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
 		/** {@value} */
 		public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds
 
 		private int connectTimeout;
 		private int readTimeout;
 
 		public ImageDownloaderWithPermissions() {
 			this(DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
 		}
 
 		public ImageDownloaderWithPermissions(int connectTimeout, int readTimeout) {
 			this.connectTimeout = connectTimeout;
 			this.readTimeout = readTimeout;
 		}
 
 		@Override
 		public InputStream getStreamFromNetwork(URI imageUri) throws IOException {
 
 	        boolean download = NetworkUtils.isPermittedConnectionAvailable(context, managerPreferences.getIconDownloadPermissions());
 
 	        Log.d("AplicationAptoide", ""+download);
 	        Log.d("AplicationAptoide", ""+managerPreferences.getIconDownloadPermissions());
 
 	        if(download){
 	        	URLConnection conn = imageUri.toURL().openConnection();
 				conn.setConnectTimeout(connectTimeout);
 				conn.setReadTimeout(readTimeout);
 				return new FlushedInputStream(new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE));
 	        }else{
 	        	return null;
 	        }
 		}
 
 	}
 
 	/**
 	 * @param context the context to set
 	 */
 	public static void setContext(Context context) {
 		ApplicationAptoide.context = context;
 	}
 }
