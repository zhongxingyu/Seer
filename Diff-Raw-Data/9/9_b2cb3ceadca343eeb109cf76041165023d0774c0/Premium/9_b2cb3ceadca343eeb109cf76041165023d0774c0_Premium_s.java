 package com.inc.im.serptracker.util;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.flurry.android.FlurryAgent;
 import com.inc.im.serptracker.R;
 import com.inc.im.serptracker.VerifyWebView;
 import com.inc.im.serptracker.adapters.DbAdapter;
 import com.inc.im.serptracker.data.Keyword;
 import com.inc.im.serptracker.data.UserProfile;
 
 /**
  * Holds functions for premium version.
  * 
  */
 
 public class Premium {
 
 	/**
 	 * Adds ranking result keyword on click premium features
 	 */
 	public static void addPremiumOnClick(final Activity a,
 			final int spinnerSelectedItemIndex) {
 
 		ListView lv = (ListView) a.findViewById(R.id.listview_result);
 
 		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					final int arg2, long arg3) {
 
 				Context con = a.getBaseContext();
 
 				// -1 because first is default text
 				final UserProfile selectedUser = new DbAdapter(con)
 						.loadAllProfiles().get(spinnerSelectedItemIndex - 1);
 
 				final Keyword k = selectedUser.keywords.get(arg2);
 
 				// if there isn't anchor/url to show, don't open dialog
 				if (k.url.equals("error getExtraUrlById"))
 					return;
 
 				// display dialog - WITH SHOW LIVE RANKING/VERIFY BUTTON
 				// final CharSequence[] items = {
				// a.getString(R.string.premium_view_live_ranking),
 				// "ANCHOR: " + k.anchorText, "URL: " + k.url };
 				//
 
 				final CharSequence[] items = {
 						a.getString(R.string.anchor) + ": " + k.anchorText,
 						"URL: " + k.url };
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(a);
 
 				builder.setTitle(a.getString(R.string.premium_dialog_title));
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 
 						//REMOVED VERIFY RANKING
 						item++;
 						
 						if (item == 0) {
 
 							// first reserved for verify ranking
 							try {
 
 								String userSearchEngine = PreferenceManager
 										.getDefaultSharedPreferences(a)
 										.getString("prefLocalize", "Google.com");
 
 								Toast.makeText(
 										a,
 										"www."
 												+ userSearchEngine
 												+ "/seach?q="
 												+ URLEncoder.encode(k.keyword,
 														"UTF-8"),
 										Toast.LENGTH_LONG).show();
 							} catch (UnsupportedEncodingException e) {
 								Log.e("MY", e.toString());
 								e.printStackTrace();
 							}
 
 							Intent i = new Intent(a, VerifyWebView.class);
 							i.putExtra("keyword", k.keyword);
 							i.putExtra("url", selectedUser.url);
 
 							a.startActivity(i);
 						} else if (item == 1) {
 
 							// start custom dialog with textbox with url inside
 							customDialogWithTextboxToShowAnchor(k.anchorText);
 
 						} else if (item == 2) {
 							// third reserved for visit url
 							a.startActivity(new Intent(Intent.ACTION_VIEW, Uri
 									.parse(selectedUser.keywords.get(arg2).url)));
 						}
 
 					}
 
 					private void customDialogWithTextboxToShowAnchor(
 							String anchorText) {
 
 						Dialog dialog = new Dialog(a);
 
 						dialog.setContentView(R.layout.premium_dialog_textbox);
 						dialog.setTitle(a
 								.getString(R.string.premium_show_anchor_dialog_title));
 
 						EditText et = (EditText) dialog
 								.findViewById(R.id.editText1);
 
 						et.setText(anchorText);
 
 						dialog.show();
 
 					}
 				});
 				AlertDialog alert = builder.create();
 
 				alert.show();
 
 			}
 		});
 	}
 
 	public static void showBuyPremiumDialog(String msg, final Activity a) {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(a);
 		builder.setMessage(msg)
 				.setCancelable(true)
 				.setPositiveButton(a.getString(R.string.yes),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.dismiss();
 								Intent intent = new Intent(Intent.ACTION_VIEW);
 
 								Boolean isAndroidMarketInstalled = isMarketInstalled();
 
 								if (isAndroidMarketInstalled) {
 									String marketlink = a
 											.getString(R.string.market_details_id_com_inc_im_serptrackerpremium);
 
 									intent.setData(Uri.parse(marketlink));
 
 									a.startActivity(intent);
 								} else {
 									Toast.makeText(
 											a,
 											"Android market is not found on your device. Aborting",
 											Toast.LENGTH_LONG).show();
 								}
 
 							}
 
 							private Boolean isMarketInstalled() {
 
 								try {
 									a.getPackageManager().getApplicationInfo(
 											"com.android.vending", 0);
 								} catch (NameNotFoundException e) {
 									return false;
 								}
 
 								return true;
 							}
 						})
 				.setNegativeButton(a.getString(R.string.cancel),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 
 								dialog.dismiss();
 
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 
 	}
 }
