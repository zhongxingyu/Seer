 package com.luzi82.codeindex.android;
 
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 
 public class AboutActivity extends PreferenceActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		PackageManager pm = getPackageManager();
 		String pn = getPackageName();
 
 		addPreferencesFromResource(R.xml.about);
 
 		// market link
 		Preference appNamePreference = findPreference("about_appname");
 		Intent marketIntent = new Intent(Intent.ACTION_VIEW);
 		String martetUri = "market://details?id=" + pn;
 		System.out.println(martetUri);
 		marketIntent.setData(Uri.parse(martetUri));
 		appNamePreference.setIntent(marketIntent);
 
 		// app ver
 		Preference appVerPreference = findPreference("about_version");
 		String appVerString = "unknown";
 		try {
 			PackageInfo pi = pm.getPackageInfo(pn, 0);
 			appVerString = String.format("%1$s (%2$d)", pi.versionName, pi.versionCode);
 		} catch (NameNotFoundException e1) {
 			e1.printStackTrace();
 		}
 		appVerPreference.setSummary(appVerString);
 
 		// email intent
 		Preference emailPreference = findPreference("about_email");
 		Intent emailIntent = new Intent(Intent.ACTION_SEND);
 		emailIntent.setType("message/rfc882");
 		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getResources().getString(R.string.about_email_address) });
 		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
 		emailPreference.setIntent(Intent.createChooser(emailIntent, emailPreference.getTitle()));
 
 		// qrcode
 		Preference qrcodePreference = findPreference("about_share");
 		Intent qrcodeIntent = new Intent(this, QRCode.class);
 		qrcodePreference.setIntent(qrcodeIntent);
 	}
 
 }
