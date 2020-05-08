 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.activity;
 
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.R;
 import org.mustard.statusnet.Relationship;
 import org.mustard.util.MustardException;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 
 public class MustardUser extends MustardUserBaseActivity {
 	
 	public void onCreate(Bundle savedInstanceState) {
 		TAG = getClass().getCanonicalName();
 		super.onCreate(savedInstanceState);
 
 		if(mStatusNet != null) {
 			
 			try {
 				mUser = mStatusNet.getUser(DB_ROW_EXTRA);
 
 				if (mUser == null) {
 					new AlertDialog.Builder(MustardUser.this)
 					.setTitle(getString(R.string.warning))
 					.setMessage(getString(R.string.error_user_not_found,DB_ROW_EXTRA))
 					.setNeutralButton(R.string.close,  new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface xdialog, int id) {
 							finish();
 						}
 					}).show();
 				} else {
 					prepareUserView();
 					getStatuses();
 				}
 			} catch (MustardException e) {
 				if(e.getCode()==404) {
 					new AlertDialog.Builder(MustardUser.this)
 					.setTitle(getString(R.string.warning))
 					.setMessage(getString(R.string.error_user_not_found,DB_ROW_EXTRA))
 					.setNeutralButton(R.string.close,  new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface xdialog, int id) {
 							finish();
 						}
 					}).show();		
 				} else {
 					new AlertDialog.Builder(MustardUser.this)
 					.setTitle(getString(R.string.error))
 					.setMessage(getString(R.string.error_generic_detail,e.getMessage() == null ? e.toString() : e.getMessage()))
 					.setNeutralButton(R.string.close,  new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface xdialog, int id) {
 							finish();
 						}
 					}).show();
 				}
 			} catch (Exception e) {
 				new AlertDialog.Builder(MustardUser.this)
 				.setTitle(getString(R.string.error))
 				.setMessage(getString(R.string.error_generic_detail,e.getMessage() == null ? e.toString() : e.getMessage()))
 				.setNeutralButton(R.string.close,  new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface xdialog, int id) {
 						finish();
 					}
 				}).show();				
 			}
 		}
 	}
 
 	@Override
 	protected void onBeforeFetch() {
 		Intent intent = getIntent();
 		Uri data = intent.getData();
 		DB_ROW_TYPE=MustardDbAdapter.ROWTYPE_USER;
 		
 		String userid = null;
 		if (intent.hasExtra(EXTRA_USER))
 			userid=intent.getStringExtra(EXTRA_USER);
 		if(userid == null)
 			userid=data.getLastPathSegment();
 		DB_ROW_EXTRA=userid;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, FRIENDS_ID, 0, R.string.menu_friends)
 			.setIcon(android.R.drawable.ic_menu_myplaces);
 		menu.add(0, MENTIONS_ID, 0, R.string.menu_mentions)
 			.setIcon(android.R.drawable.ic_menu_mylocation);
 		menu.add(0, FAVORITES_ID, 0, R.string.menu_favorites)
 			.setIcon(android.R.drawable.ic_menu_recent_history);
 		super.onCreateOptionsMenu(menu);
 		return true;
 	}
 	
 	protected void onPreCreateOptionsMenu(Menu menu) {
 		menu.add(0, UNSUB_ID, 0, R.string.menu_unsub)
 		.setIcon(android.R.drawable.ic_menu_delete);
 		menu.add(0, SUB_ID, 0, R.string.menu_sub)
 		.setIcon(android.R.drawable.ic_menu_save);				
 	}
 	
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		if(mStatusNet.getMUsername().equalsIgnoreCase(mUser.getScreen_name())) {
 			menu.findItem(SUB_ID).setVisible(false);
 			menu.findItem(UNSUB_ID).setVisible(false);
 			return true;
 		}
 		
 		Relationship r = mStatusNet.getFriendshipStatus(mUser.getScreen_name());
 		if (r != null) {
 			boolean isFollowing = r.getSource().isFollowing();
 			//		Log.v(TAG, DB_ROW_EXTRA + " you follow him/her");
 			menu.findItem(SUB_ID).setVisible(!isFollowing);
 			menu.findItem(UNSUB_ID).setVisible(isFollowing);
 		}
 		return true;
 	}
 	
 	@Override
 	protected void onAfterFetch() {
 	}
 
 	@Override
 	protected void onSetListView() {
 		if(mLayoutLegacy) {
 			setContentView(R.layout.legacy_user_list);
 			R_ROW_ID=R.layout.legacy_timeline_list_item_user;			
 		} else {
 			setContentView(R.layout.user_list);
 			R_ROW_ID=R.layout.timeline_list_item_user;
 		}
 	}
 		
 	protected void doSubscribe() {
		new StatusSubscribe().execute(mUser.getScreen_name());
 	}
 	
 	protected void doUnsubscribe() {
		new StatusUnsubscribe().execute(mUser.getScreen_name());
 	}
 	
 	public static void actionHandleTimeline(Context context,String userid) {
 		Intent intent = new Intent(context, MustardUser.class);
 		intent.putExtra(EXTRA_USER, userid);
 	    context.startActivity(intent);
 	}
 	
 	public static void actionHandleTimeline(Context context,long accountid, String userid) {
 		Intent intent = new Intent(context, MustardUser.class);
 		intent.putExtra(EXTRA_USER, userid);
 		intent.putExtra(EXTRA_ACCOUNT, accountid);
 	    context.startActivity(intent);
 	}
 
 }
