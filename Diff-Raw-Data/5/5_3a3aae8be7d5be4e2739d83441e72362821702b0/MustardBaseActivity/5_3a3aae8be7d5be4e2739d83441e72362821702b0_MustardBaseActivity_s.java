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
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.mustard.android.Controller;
 import org.mustard.android.MessagingListener;
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.Preferences;
 import org.mustard.android.R;
 import org.mustard.android.provider.StatusNet;
 import org.mustard.android.view.GimmeMoreListView;
 import org.mustard.android.view.MustardStatusTextView;
 import org.mustard.android.view.RemoteImageView;
 import org.mustard.geonames.GeoName;
 import org.mustard.statusnet.Attachment;
 import org.mustard.util.DateUtils;
 import org.mustard.util.MustardException;
 import org.mustard.util.StatusNetUtils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.AsyncTask.Status;
 import android.preference.PreferenceManager;
 import android.text.ClipboardManager;
 import android.text.Html;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.TextView.BufferType;
 
 public abstract class MustardBaseActivity extends ListActivity implements GimmeMoreListView.OnNeedMoreListener {
 
 	protected String TAG="MustardBaseActivity";
 
 	protected static final String EXTRA_USER="mustard.user";
 	
 	protected static final int DIALOG_FETCHING_ID=0;
 	protected static final int DIALOG_OPENING_ID=1;
 	
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int ACTIVITY_EDIT = 1;
 	private static final int ACCOUNT_ADD = 2;
 	private static final int ACCOUNT_DEL = 3;
 //	private static final int ACTIVITY_MENTIONS = 4;
 	private static final int ACCOUNT_ADD_SWITCH = 5;
 //	private static final int ACTIVITY_FRIENDS = 6;
 //	private static final int ACTIVITY_FAVORITES = 7;
 	private static final int ACTIVITY_PUBLIC = 8;
 	
 	private static final int INSERT_ID = 0;
 	private static final int USER_TL_ID = 1;
 	private static final int REPLY_ID = 2;
 	private static final int REPEAT_ID = 3;
 	protected static final int MENTIONS_ID = 4;
 	private static final int PUBLIC_ID = 5;
 	private static final int REFRESH_ID = 6;
 	private static final int LOGOUT_ID = 7;
 	private static final int DELETE_ID = 8;
 	private static final int SWITCH_ID = 9;
 	private static final int SEARCH_ID = 10;
 	private static final int ABOUT_ID = 11;
 	private static final int BACK_ID = 12;
 	private static final int FAVORS_ID = 13;
 	private static final int UNFAVORS_ID = 14;
 	protected static final int SUB_ID = 15;
 	protected static final int UNSUB_ID = 16;
 	protected static final int FAVORITES_ID = 17;
 	protected static final int FRIENDS_ID = 18;
 	private static final int ACCOUNT_SETTINGS_ID = 19;
 	private static final int SETTINGS_ID = 20;
 	private static final int BOOKMARKS_ID = 21;
 	private static final int BOOKMARK_THIS_ID = 22;
 //	private static final int ACCOUNT_MENU_ID = 23;
 	private static final int BLOCK_ID = 24;
 	private static final int GEOLOCATION_ID = 25;
 	protected static final int GROUP_JOIN_ID= 26;
 	protected static final int GROUP_LEAVE_ID= 27;
 	protected static final int M_SUB_ID = 28;
 	protected static final int M_UNSUB_ID = 29;
 	private static final int SHARE_ID = 30;
 	private static final int COPY2CLIPBOARD_ID = 31;
 
 
 	protected MustardDbAdapter mDbHelper;
 	private StatusesLoadMore mLoadMoreTask = null;
 	private StatusesFetcher mFetcherTask = null;
 	private boolean mIsRefresh=false;
 	protected boolean mNoMoreDents=false;
 	private String mErrorMessage="";
 	private long mCurrentRowid = -1;
 	
 	protected StatusNet mStatusNet = null;
 	protected boolean mFromSavedState = false;
 	private boolean  mIsOnSaveInstanceState=false;
 
 	protected int DB_ROW_TYPE;
 	protected String DB_ROW_EXTRA;
 	protected String DB_ROW_ORDER = "DESC";
 	protected int R_ROW_ID=R.layout.timeline_list_item;
 	protected boolean isRefreshEnable=true;
 	protected boolean isBookmarkEnable=true;
 //	private Cursor mNoticesCursor;
 	NoticeListAdapter mNoticeCursorAdapter = null;
 	protected SharedPreferences mPreferences;
 	
 	//private Timer mAutoRefreshTimer ;
 	protected boolean mAutoRefresh=false;
 	
 	protected boolean mLayoutLegacy=false;
 	private boolean mLayoutNewButton=false;
 	
 //	protected static boolean isMainTimeline = false;
 	
 	class NoticeListAdapter extends SimpleCursorAdapter {
 
 		private boolean mLoading = true;
 
 		private int mIdIdx;
 		private int mIdAccountIdx;
 		private int mProfileImageIdx;
 		private int mGeolocationIdx;
 		private int mScreenNameIdx;
 		private int mStatusIdx;
 		private int mStatusIdIdx;
 		private int mDatetimeIdx;
 		private int mSourceIdx;
 		private int mInReplyToIdx;
 		private int mLonIdx;
 		private int mLatIdx;
 		private int mAttachmentIdx;
 
 		private HashMap<Long, String> hmAccounts;
 		
 		class ViewHolder {
 
 			RemoteImageView profile_image;
 			ImageButton noticeinfo;
 			ImageButton geolocation;
 			ImageButton conversation;
 			TextView screen_name;
 			TextView account_name;
 			MustardStatusTextView status;
 			TextView datetime;
 			TextView source;
 			MustardStatusTextView in_reply_to;
 			ImageButton attachment;
 			View bottomRow;
 		}
 		
 		/**
 		 * @param context
 		 * @param layout
 		 * @param c
 		 * @param from
 		 * @param to
 		 */
 		public NoticeListAdapter(Context context, int layout,
                 String[] from, int[] to) {
 			super(context, layout, null, from, to);
 		}
 
 		public void setLoading(boolean loading) {
 			mLoading = loading;
 		}
 
 
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			View v = super.newView(context, cursor, parent);
 //			Log.d(TAG, "newView");
             ViewHolder vh = new ViewHolder();
             try {
             	vh.profile_image = (RemoteImageView)v.findViewById(R.id.profile_image);
             } catch (Exception e) {
             }
 
             vh.noticeinfo = (ImageButton)v.findViewById(R.id.noticeinfo);
             vh.geolocation = (ImageButton)v.findViewById(R.id.geolocation);          
             vh.screen_name = (TextView)v.findViewById(R.id.screen_name);
             try {
             	vh.account_name = (TextView)v.findViewById(R.id.account_name);
             } catch (Exception e) {
             	
             }
             vh.status = (MustardStatusTextView)v.findViewById(R.id.status);
             vh.datetime = (TextView)v.findViewById(R.id.datetime);
             vh.source = (TextView)v.findViewById(R.id.source);
             try {
             	vh.bottomRow = (View)v.findViewById(R.id.bottom_row);
             }catch (Exception e) {
             } 
             try {
             	vh.conversation = (ImageButton)v.findViewById(R.id.conversation);
             }catch (Exception e) {
             }
             try {
             	vh.in_reply_to = (MustardStatusTextView)v.findViewById(R.id.in_reply_to);
             } catch (Exception e) {
             }
             vh.attachment = (ImageButton)v.findViewById(R.id.attachment);
             v.setTag(vh);
             return v;
         }
 
         public void bindView(final View view, final Context context, Cursor cursor) {
             ViewHolder vh = (ViewHolder) view.getTag();
             
             if (hmAccounts==null) {
             	hmAccounts = new HashMap<Long,String>();
             	if (MustardApplication.DEBUG)
             		Log.i(TAG, "############################## CREATO hmAccounts ##########");
             }
             final long id = cursor.getLong(mIdIdx);
         	final long statusId = cursor.getLong(mStatusIdIdx);
 
         	view.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
 //    				openContextMenu(v);
     				onShowNoticeMenu(v,id);
     			}
     		});
         	view.setOnLongClickListener(new View.OnLongClickListener() {
     			public boolean onLongClick(View v) {
     				openContextMenu(v);
     				return true;
     			}
     		});
         	if (MustardApplication.DEBUG)
         		Log.v(TAG, "Binding id=" + id + " " + statusId + " cursor=" + cursor);
         	long accountId = cursor.getLong(mIdAccountIdx);
         	if (vh.screen_name != null)
         		vh.screen_name.setText(cursor.getString(mScreenNameIdx));
         	if ( mMergedTimeline && vh.account_name != null) {
         		
         		if(!hmAccounts.containsKey(accountId)) {
         			Cursor c = mDbHelper.fetchAccount(accountId);
         			String account = "";
         			if (c.moveToNext()) {
         				account=c.getString(c.getColumnIndex(MustardDbAdapter.KEY_USER));
         				String instance = c.getString(c.getColumnIndex(MustardDbAdapter.KEY_INSTANCE));
         				try {
         					URL url = new URL(instance);
         					account +=  "@" + url.getHost() + url.getPath();
 //        					Log.i(TAG, "AccountID " + accountId + " => " + account + " " + host + " (" +instance +")");
         				} catch (Exception e) {
         					e.printStackTrace();
         				}
         			} else {
         				Log.e(TAG,"NO ACCOUNT WITH ID: " + accountId);
         			}
         			c.close();
         			hmAccounts.put(accountId, account);
         		}
         		vh.account_name.setText(hmAccounts.get(accountId));
         		vh.account_name.setVisibility(View.VISIBLE);
         	}
             String source = cursor.getString(mSourceIdx) ;
             if (source != null && !"".equals(source)) {
             	source = Html.fromHtml("&nbsp;from " + source.replace("&lt;", "<").replace("&gt;", ">"))+" ";
             	if (source.length()>15)
             		source = source.substring(0, 15) +"..";
             }
             vh.source.setText(source, BufferType.SPANNABLE);
 
             long inreplyto = cursor.getLong(mInReplyToIdx);
             TextView vr = vh.in_reply_to;
             if (vr != null) {
             	if (inreplyto > 0) {
             		vr.setText(Html.fromHtml("&nbsp;<a href='statusnet://status/"+inreplyto+"' >in reply to</a>&nbsp;&nbsp;<a href='statusnet://conversation/"+statusId+"' >thread</a>") , BufferType.SPANNABLE);
             		vr.setVisibility(View.VISIBLE);
             	} else {
             		vr.setVisibility(View.GONE);
             	}
             }
 
             if (vh.profile_image != null) {
             	String profileUrl = cursor.getString(mProfileImageIdx);
             	if (profileUrl != null && !"".equals(profileUrl)) {
             		vh.profile_image.setRemoteURI(profileUrl);
             		vh.profile_image.loadImage();
             	}
             	if(mLayoutNewButton) {
             		vh.profile_image.setOnClickListener(new View.OnClickListener() {
 
             			public void onClick(View v) {
             				openContextMenu(v);
             			}
 
             		});
             	}
             	vh.profile_image.setFocusable(mLayoutNewButton);
             }
             Date d = new Date();
             d.setTime(cursor.getLong(mDatetimeIdx));
 			vh.datetime.setText(DateUtils.getRelativeDate( d ));
             
 			if(mPreferences.getBoolean(Preferences.COMPACT_VIEW, false) ) {
 				if (vh.bottomRow!=null)
 					vh.bottomRow.setVisibility(View.GONE);
 				
 			} else {
 			
 				if(vh.conversation != null) {
 					if (inreplyto > 0) {
 						vh.conversation.setImageResource(R.drawable.conversation);
 						vh.conversation.setOnClickListener(new View.OnClickListener() {
 
 							public void onClick(View v) {
 								doOpenConversation(statusId);
 							}
 
 						});
 
 					} else {
 						vh.conversation.setImageResource(R.drawable.conversation_disabled);
 					}
 					vh.conversation.setFocusable(mLayoutNewButton);
 				}
 
 				int geo = cursor.getInt(mGeolocationIdx);
 				if (geo == 1) {
 					final String lon = cursor.getString(mLonIdx);
 					final String lat = cursor.getString(mLatIdx);
 					vh.geolocation.setOnClickListener(new View.OnClickListener() {
 
 						public void onClick(View v) {
 							doShowLocation(lon, lat);
 						}
 
 					});
 					vh.geolocation.setImageResource(R.drawable.pin_button);
 				} else {
 					vh.geolocation.setImageResource(R.drawable.pin_disabled);
 				}
 				vh.geolocation.setFocusable(mLayoutNewButton);
 
 				int attachment = cursor.getInt(mAttachmentIdx);
 				if (attachment == 1) {
 
 					vh.attachment.setOnClickListener(new View.OnClickListener() {
 
 						public void onClick(View v) {
 							onShowAttachemntList(id);
 						}
 
 					});
 					vh.attachment.setImageResource(R.drawable.attachment);
 				} else {
 					vh.attachment.setImageResource(R.drawable.attachment_disabled);
 				}
 				vh.attachment.setFocusable(mLayoutNewButton);
 
 			}
             String status = cursor.getString(mStatusIdx);
            if (status.indexOf("<")>0)
             	status=status.replaceAll("<", "&lt;");
			if(status.indexOf(">")>0)
 				status=status.replaceAll(">","&gt;");
 			
 			TextView v = vh.status;
 			v.setText(Html.fromHtml(status).toString(), BufferType.SPANNABLE);
 			Linkify.addLinks(v, Linkify.WEB_URLS);
 			StatusNetUtils.linkifyUsers(v);
 			if(mStatusNet.isTwitterInstance()) {
 				StatusNetUtils.linkifyGroupsForTwitter(v);
 				StatusNetUtils.linkifyTagsForTwitter(v);
 			} else {
 				StatusNetUtils.linkifyGroups(v);
 				StatusNetUtils.linkifyTags(v);
 			}
 			if (vh.noticeinfo !=null) {
 				
 				if(mLayoutNewButton) {
 				
 					vh.noticeinfo.setOnClickListener(new View.OnClickListener() {
 	
 						public void onClick(View v) {
 							onShowNoticeAction(id);
 						}
 	
 					});
 					
             	} else {
 
             		vh.noticeinfo.setVisibility(View.GONE);
             	}
 				vh.noticeinfo.setFocusable(mLayoutNewButton);
 			}
 			
         }
         
         public void changeCursor(Cursor cursor) {
             super.changeCursor(cursor);
 //            Log.v(TAG, "Setting cursor to: " + cursor + " from: " + TimelineNoticeList.this.mCursor);
             
             if (cursor != null) {
             	mIdIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_ROWID);
             	mIdAccountIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_ACCOUNT_ID);
             	mGeolocationIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_GEO);
             	mScreenNameIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_SCREEN_NAME);
             	mStatusIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_STATUS);
             	mStatusIdIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_STATUS_ID);
             	mDatetimeIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_INSERT_AT);
             	mSourceIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_SOURCE);
             	mInReplyToIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_IN_REPLY_TO);
             	mProfileImageIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_USER_IMAGE);
             	mLonIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_LON);
             	mLatIdx = cursor.getColumnIndex(MustardDbAdapter.KEY_LAT);
             	mAttachmentIdx =  cursor.getColumnIndex(MustardDbAdapter.KEY_ATTACHMENT);
             } else {
             	Log.v(TAG, "Cursor is null "); 
             }
             
         }
         
 		public boolean isEmpty() {
 			if (mLoading) {
 				// We don't want the empty state to show when loading.
 				return false;
 			} else {
 				return super.isEmpty();
 			}
 		}
 
 
 	}
 	
 	private void onShowNoticeMenu(View v,final long id) {
 		
 		Cursor c = mDbHelper.fetchStatus(id);
 		BitmapDrawable _icon = new BitmapDrawable(
 				MustardApplication.sImageManager.get(
 						c.getString(
 								c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_IMAGE)
 						)
 				)
 		);
 		String userName = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 		boolean favorited = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FAVORITE)) == 1 ? true : false;
 		long in_reply_to = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_IN_REPLY_TO));
 		int geo = c.getInt(c.getColumnIndex(MustardDbAdapter.KEY_GEO));
 		int attachment = c.getInt(c.getColumnIndex(MustardDbAdapter.KEY_ATTACHMENT));
 		try {
 			c.close();
 		} catch (Exception e) {
 			
 		}
 //		Log.v(TAG, "Username id: " + usernameId + " vs " + mStatusNet.getUsernameId());
 		
 		ArrayList<CharSequence> aitems = new ArrayList<CharSequence>();
 		aitems.add(getString(R.string.menu_reply));
 		aitems.add(getString(R.string.menu_forward));
 		aitems.add(getString(favorited ? R.string.menu_unfav : R.string.menu_fav));
 		if (in_reply_to > 0) {
 			aitems.add(getString(R.string.menu_conversation));
 		}
 		final int conversationIdx = in_reply_to > 0 ? aitems.size()-1 : -1;
 //		Log.i(TAG,"conversationIdx = " + conversationIdx);
 		if (attachment>0) {
 			aitems.add(getString(R.string.menu_view_attachment));
 		}
 		final int attachmentIdx = attachment > 0 ? aitems.size()-1 : -1;
 //		Log.i(TAG,"attachmentIdx = " + attachmentIdx);
 		if (geo == 1) {
 			aitems.add(getString(R.string.menu_view_geo));
 		}
 		final int geoIdx = geo == 1 ? aitems.size()-1 : -1;
 //		Log.i(TAG,"geoIdx = " + geoIdx);
 		final CharSequence[] items = new CharSequence[aitems.size()];
 		for(int i=0;i<aitems.size();i++) {
 			items[i]=aitems.get(i);
 		}
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);		
 		builder.setTitle(userName);
 		builder.setIcon(_icon);
 		
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int item) {
 		    	// Funky hack.. if you have a better idea.. you're welcome!
 //		    	Log.i(TAG,"Clicked = " + item);
 		    	if (item > 2) {
 		    		if (item == conversationIdx)
 		    			item = 3;
 		    		else if (item == attachmentIdx)
 		    			item = 4;
 		    		else if (item == geoIdx)
 		    			item = 5;
 		    	}
 		    	onNoticeMenuItemClick(id, item);
 		        dialog.dismiss();
 		    }
 		});
 		builder.create().show();		 
 	}
 	
 	private void onNoticeMenuItemClick(long rowid, int action) {
 //		Log.i(TAG,"onNoticeMenuItemClick Action " + action);
 		Cursor c = mDbHelper.fetchStatus(rowid);
 		switch (action) {
         case 0:
         	// Reply:
         	MustardUpdate.actionReply(this,mHandler, rowid);
         	break;
         case 1:
         	// Forward
         	boolean newRepeat = mPreferences.getBoolean(Preferences.NEW_REPEAT_ENABLES_KEY, false);
 			if(newRepeat) {
 				String ss = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 				repeat(ss);
 			} else {
 				MustardUpdate.actionForward(this,mHandler, rowid);
 			}
 			break;
         case 2:
         	// Fav/Unfav
         	boolean favorited = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FAVORITE)) == 1 ? true : false;
         	if (favorited)
         		new StatusDisfavor().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID)));
         	else
         		new StatusFavor().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID)));
         	break;
         case 3:
 //        	boolean in_reply_to = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_IN_REPLY_TO)) > 0 ? true : false;
 //        	if(in_reply_to)
         		MustardConversation.actionHandleTimeline(this, c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID)));
         	break;
         case 4:
         	// Attachment
 //        	boolean attachment = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_ATTACHMENT)) > 0 ? true : false;
 //        	if (attachment)
         		onShowAttachemntList(rowid);
         	break;
         case 5:
         	// Geo
 //        	boolean geo = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_GEO)) == 1 ? true : false;
 //        	if (geo)
         	doShowLocation(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LON)),
         			c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LAT)));
         	break;
         }
 		try { c.close(); } catch (Exception e) {}
 	}
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		
 		if(savedInstanceState != null) {
 			mFromSavedState=true;
 		}
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 
 		mLayoutLegacy=mPreferences.getBoolean(Preferences.LAYOUT_LEGACY, false);
 		mLayoutNewButton=mPreferences.getBoolean(Preferences.LAYOUT_NEW_BUTTON, false);
 		if (MustardApplication.DEBUG) Log.i(TAG,"onCreate");
 		if (mDbHelper!=null) {
 			try {
 				mDbHelper.close();
 			} catch (Exception e) {
 				
 			}
 		}
 		mDbHelper = new MustardDbAdapter(this);
 		mDbHelper.open();
 		
 		
 		if(mLayoutLegacy) {
 			R_ROW_ID=R.layout.legacy_timeline_list_item;
 		} else {
 			R_ROW_ID=R.layout.timeline_list_item;
 		}
 		onSetListView();
 		ListView view = null;
 		try {
 			view = (GimmeMoreListView)getListView();
 			((GimmeMoreListView)view).setOnNeedMoreListener(this);
 		} catch(ClassCastException e){ 
 			Log.e(TAG," change view type!!");
 			view = getListView();
 		}
 		
 		mNoticeCursorAdapter =  new NoticeListAdapter(this,
 				R_ROW_ID, new String[] {},
                 new int[] {});
 		setListAdapter(mNoticeCursorAdapter);
 		registerForContextMenu(view);
 
 		getStatusNet();
 
 		if (mStatusNet == null) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "No account found. Starting Login activity");
 			showLogin();
 		} else {
 			if (MustardApplication.DEBUG) Log.i(TAG, "calling onBeforeFetch()");
 			onBeforeFetch();
 			changeTitle();
 //			onStartScheduler();
 		}
 	}
 	
 	protected abstract void onSetListView() ;
 	
 	private void onShowNoticeAction(final long id) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		Cursor c = mDbHelper.fetchStatus(id);
 		String text =  c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 		boolean fav =  c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FAVORITE)) == 1 ? true : false;
 		View view = LayoutInflater.from(this).inflate(R.layout.notice_action, null);
 		TextView tv = (TextView)view.findViewById(R.id.text_status);
 		tv.setText(text);
 		final Context context = this;
 		final long statusId = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 		
 		Button ibr = (Button)view.findViewById(R.id.button_reply);
 		ibr.setOnClickListener(new View.OnClickListener() {
 
         	public void onClick(View v) {
         		MustardUpdate.actionReply(context,mHandler, id);
         		closeNoticeDialog();
 			}
 
 		});
 		
 		Button ibf = (Button)view.findViewById(R.id.button_repeat);
 		ibf.setOnClickListener(new View.OnClickListener() {
 
         	public void onClick(View v) {
         		boolean newRepeat = mPreferences.getBoolean(Preferences.NEW_REPEAT_ENABLES_KEY, false);
     			if(newRepeat) {
     				repeat(Long.toString(statusId));
     			} else {
     				MustardUpdate.actionForward(context,mHandler, id);
     			}
     			closeNoticeDialog();
 			}
 
 		});
 		Button ib = (Button)view.findViewById(R.id.button_fav);
 		if(!fav) {
 			ib.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,  R.drawable.favorited);
 			ib.setText(R.string.menu_fav);
 			ib.setOnClickListener(new View.OnClickListener() {
 
             	public void onClick(View v) {
             		new StatusFavor().execute(Long.toString(statusId));
             		closeNoticeDialog();
 				}
 
 			});
 			
 		} else {
 			ib.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.unfavorited);
 			ib.setText(R.string.menu_unfav);
 			ib.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		new StatusDisfavor().execute(Long.toString(statusId));
             		closeNoticeDialog();
 				}
 			});
 		}
 		BitmapDrawable _icon = new BitmapDrawable(
 				MustardApplication.sImageManager.get(
 						c.getString(
 								c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_IMAGE)
 						)
 				)
 		);
 		String userName = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 		
 		try {
 			c.close();
 		} catch (Exception e) {
 			
 		}
 		builder.setIcon(_icon);
 		builder.setView(view);
 		builder.setCancelable(true);
 		builder.setTitle(userName);
 		builder.setPositiveButton(R.string.close, null);
 		noticeDialog = builder.create();
 		noticeDialog.show();
 	}
 	
 	private AlertDialog noticeDialog;
 	
 	private void closeNoticeDialog() {
 		noticeDialog.dismiss();
 	}
 	
 	private void onShowAttachemntList(long statusId) {
 
 		Cursor c = mDbHelper.fetchAttachment(statusId);
 		final CharSequence[] items = new CharSequence[c.getCount()];
 		final ArrayList<Attachment> attachments = new ArrayList<Attachment>();
 		int cc=0;
 		while(c.moveToNext()) {
 			Attachment a = new Attachment();
 			String mimeType = c.getString(c.getColumnIndex(MustardDbAdapter.KEY_MIMETYPE));
 			a.setMimeType(mimeType);
 			a.setUrl(c.getString(c.getColumnIndex(MustardDbAdapter.KEY_URL)));
 			attachments.add(a);
 			if (mimeType.startsWith("image")) {
 				items[cc]="Image";
 			} else if (mimeType.startsWith("text/html")) {
 				items[cc]="Html";
 			} else {
 				items[cc]="Unknown";
 			}
 			cc++;
 		}
 		c.close();
 		
 		if (attachments.size() > 1) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("View attachment");
 
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface xdialog, int item) {
 					Attachment a = attachments.get(item);
 					if (a.getMimeType().startsWith("image")) {
 						showAttachmentImage(a.getUrl(),true);
 					} else if (a.getMimeType().startsWith("text/html")){
 						showAttachmentText(a.getUrl());
 					}
 				}
 			});
 			builder.create();
 			builder.setPositiveButton(R.string.close, null);
 			builder.show();	
 		} else {
 			Attachment a = attachments.get(0);
 			if (a.getMimeType().startsWith("image")) {
 				showAttachmentImage(a.getUrl(),true);
 			} else if (a.getMimeType().startsWith("text/html")){
 				showAttachmentText(a.getUrl());
 			}
 		}
 	}
 	
 	void showAttachmentImage(String url, boolean extraLink) {
 
 		View view = LayoutInflater.from(this).inflate(R.layout.html, null);
 		WebView html = (WebView)view.findViewById(R.id.html);
 		String summary = "<html><body>" +
 				"<center>" +
 				"<img width=\"100%\" src=\""+url+"\"/>";
 		if (extraLink)
 			summary += "<br/><a href=\""+url+"\">Open with Browser</a></center>";
 		
 		summary += "</body></html>";
 		html.loadDataWithBaseURL("fake://this/is/not/real",summary, "text/html", "utf-8","");
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setIcon(R.drawable.attachment);
 		builder.setView(view);
 		builder.setCancelable(true);
 		builder.setTitle("View Image");
 		builder.setPositiveButton(R.string.close, null);
 		builder.create().show();
 		
 	}
 	
 	void showAttachmentText(String url) {
 
 		View view = LayoutInflater.from(this).inflate(R.layout.html, null);
 		
 		WebView html = (WebView)view.findViewById(R.id.html);
 		html.loadUrl(url);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(view);
 		builder.setIcon(R.drawable.attachment);
 		builder.setCancelable(true);
 		builder.setTitle("View Text");
 		builder.setPositiveButton(R.string.close, null);
 		builder.create().show();
 		
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onResume()");
 		mIsOnSaveInstanceState=false;
 	}
 	
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onRestart()");
 		mIsOnSaveInstanceState=false;
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle state) {
 		super.onRestoreInstanceState(state);
 		if (MustardApplication.DEBUG) Log.i(TAG, "onRestoreInstanceState()");
 		mIsOnSaveInstanceState=false;
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onPause()");
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onStart()");
 		mIsOnSaveInstanceState=false;
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (MustardApplication.DEBUG) Log.i(TAG, "onStop()");
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if (MustardApplication.DEBUG) Log.i(TAG, "onConfigurationChanged()");
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		mIsOnSaveInstanceState=true;
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onDestroy() {
 		
 		super.onDestroy();
 		if (MustardApplication.DEBUG) Log.i(TAG,"onDestroy()");
 
 
 		if(mDbHelper != null) {
 			try {
 				if(!mIsOnSaveInstanceState) {
 					if (MustardApplication.DEBUG) Log.i(TAG,"deleting dents");
 					mDbHelper.deleteStatuses(DB_ROW_TYPE,DB_ROW_EXTRA);
 				}
 			} catch (Exception e) {
 				if (MustardApplication.DEBUG) e.printStackTrace();
 			} finally {
 				mDbHelper.close();
 			}
 		}
 	}
 	
 	protected void onPreCreateOptionsMenu(Menu menu) {
 	}
 	
 	protected void onPostCreateOptionsMenu(Menu menu) {
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		onPreCreateOptionsMenu(menu);
 		
 		if(isTaskRoot()) {
 			menu.add(0, INSERT_ID, 0, R.string.menu_insert)
 				.setIcon(android.R.drawable.ic_menu_add);
 		}
 		
 		if(isRefreshEnable)
 			menu.add(0, REFRESH_ID, 0, R.string.menu_refresh)
 			.setIcon(android.R.drawable.ic_menu_rotate);
 	
 		if(isTaskRoot()) {
 			menu.add(0, MENTIONS_ID, 0, R.string.menu_mentions)
 				.setIcon(android.R.drawable.ic_menu_mylocation);
 			menu.add(0, SEARCH_ID, 0, R.string.menu_search)
 				.setIcon(android.R.drawable.ic_menu_search);
 			menu.add(0, BOOKMARKS_ID, 0, R.string.menu_bookmarks)
 				.setIcon(android.R.drawable.ic_menu_compass);
 			menu.add(0, PUBLIC_ID, 0, R.string.menu_public)
 			.setIcon(android.R.drawable.ic_menu_myplaces);
 			menu.add(0, FAVORITES_ID, 0, R.string.menu_favorites)
 			.setIcon(android.R.drawable.ic_menu_recent_history);
 			menu.add(0, SWITCH_ID, 0, R.string.menu_switch)
 				.setIcon(android.R.drawable.ic_menu_directions);
 			menu.add(0, ACCOUNT_SETTINGS_ID, 0, R.string.menu_account_settings)
 				.setIcon(android.R.drawable.ic_menu_gallery);
 		    menu.add(0, SETTINGS_ID, 0, R.string.menu_settings)
 		    	.setIcon(android.R.drawable.ic_menu_preferences);
 //		    menu.add(0, OAUTHSETTING_ID, 0, R.string.menu_oauth_settings)
 //	    		.setIcon(android.R.drawable.ic_menu_preferences);
 			menu.add(0, LOGOUT_ID, 0, R.string.menu_logout)
 				.setIcon(android.R.drawable.ic_menu_delete);
 			menu.add(0, ABOUT_ID, 0, R.string.menu_about)
 				.setIcon(android.R.drawable.ic_menu_info_details);
 		} else {
 			if(isBookmarkEnable) {
 				menu.add(0, BOOKMARK_THIS_ID, 0, R.string.menu_bookmark_this_page)
 					.setIcon(android.R.drawable.btn_star);
 			}
 			menu.add(0, BACK_ID, 0, R.string.menu_back)
 				.setIcon(android.R.drawable.ic_menu_revert);
 		}
 		onPostCreateOptionsMenu(menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case INSERT_ID:
 			update();
 			return true;
 		case REFRESH_ID:
 			refresh();
 			return true;
 		case MENTIONS_ID:
 			getMentions();
 			return true;
 		case PUBLIC_ID:
 			getPublic();
 			return true;
 		case LOGOUT_ID:
 			logout();
 			return true;
 		case SWITCH_ID:
 			switchUser();
 			return true;
 //		case OAUTHSETTING_ID:
 //			oauthSettings();
 //			return true;
 		case SEARCH_ID:
 			search();
 			return true;
 		case BOOKMARKS_ID:
 			bookmark();
 			return true;
         case ABOUT_ID:
             AboutDialog.show(this);
             return true;
         case BACK_ID:
             setResult(RESULT_OK);
             finish();
             return true;
         case BOOKMARK_THIS_ID:
         	bookmarkThis();
         	return true;
         case FAVORITES_ID:
         	getFavorites();
         	return true;
         case FRIENDS_ID:
         	getFriends();
         	return true;
         case ACCOUNT_SETTINGS_ID:
         	AccountSettings.actionAccountSettings(this);
         	return true;
         case SETTINGS_ID:
         	settings();
         	return true;
 		case GROUP_LEAVE_ID:
 			doLeaveGroup();
 			return true;
 		case GROUP_JOIN_ID:
 			doJoinGroup();
 			return true;
 		case SUB_ID:
 			doSubscribe();
 			return true;
 		case UNSUB_ID:
 			doUnsubscribe();
 			return true;			
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	protected abstract void onBeforeFetch() ;
 	
 	public void needMore() {
 		if(MustardApplication.DEBUG) Log.d(TAG,"Asked for more!");
 		doLoadMore();
 	}
 	
 	private void refresh() {
 		mIsRefresh=true;
 		getStatuses();
 	}
 	
 	private void switchUser() {
 		Cursor c = mDbHelper.fetchAllNonDefaultAccounts();
 		final CharSequence[] items = new CharSequence[c.getCount()+1];
 		final long[] rowIds = new long[c.getCount()];
 		int cc=0;
 		while(c.moveToNext()) {
 			items[cc]=c.getString(c.getColumnIndex(MustardDbAdapter.KEY_INSTANCE)) +
 				"/" + c.getString(c.getColumnIndex(MustardDbAdapter.KEY_USER));
 			rowIds[cc]=c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 			cc++;
 		}
 		items[cc]=getString(R.string.menu_add_new);
 		c.close();
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.menu_choose_account));
 
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface xdialog, int item) {
 	    		mDbHelper.resetDefaultAccounts();
 	    		mDbHelper.deleteStatuses(MustardDbAdapter.ROWTYPE_ALL, "");
 	    		mDbHelper.deleteStatuses(DB_ROW_TYPE,DB_ROW_EXTRA);
 	    		if(mFetcherTask!=null) {
 	    			mFetcherTask.cancel(true);
 	    		}
 	    		mFetcherTask = null;
 		    	if (items[item].equals(getString(R.string.menu_add_new))) {
 		    		if (mNoticeCursorAdapter!=null) {
 		    			mNoticeCursorAdapter.notifyDataSetInvalidated();
 //		    			mNoticeCursorAdapter.getCursor().requery();
 //		    			mNoticeCursorAdapter = null;
 		    		}
 		    		showLogin();
 		    	} else {
 		    		mDbHelper.setDefaultAccount(rowIds[item]);
 		    		startMainTimeline();
 //		    		if (mNoticeCursorAdapter!=null) {
 //		    			mNoticeCursorAdapter.notifyDataSetInvalidated();
 ////		    			mNoticeCursorAdapter.getCursor().requery();
 ////		    			mNoticeCursorAdapter = null;
 //		    		}
 //					getStatusNet();
 //					onBeforeFetch();
 //					changeTitle();
 //					getStatuses();
 		    	}
 		    }
 		});
 		builder.create();
 		builder.show();
 	}
 	
 	private void startMainTimeline() {
 		MustardMain.actionHandleTimeline(this);
 		finish();
 	}
 	
 	private void changeTitle() {
 		if (mStatusNet != null) {
 			setTitle(getString(R.string.app_name)  + " - " + mStatusNet.getMUsername() + "@" + mStatusNet.getURL().getHost());
 		}
 	}
 
 	private void showLogin() {
 //		Intent i = new Intent(this, Login.class);
 //		startActivityForResult(i, ACCOUNT_ADD_SWITCH);
 		Login.actionHandleLogin(this);
 		finish();
 	}
 	
 	private void logout() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.msg_logout))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				mDbHelper.deleteAccount(mStatusNet.getUserId());
 				mDbHelper.deleteBookmarks(mStatusNet.getUserId());
 				mDbHelper.deleteStatuses(MustardDbAdapter.ROWTYPE_ALL, "");
 				finish();
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		builder.create();
 		builder.show();
 	}
 	
 	private void repeat(final String ss) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.msg_confirm_repeat))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				new StatusRepeat().execute(ss);
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		builder.create();
 		builder.show();
 	}
 	
 	private void getFriends() {
 		MustardFriend.actionHandleTimeline(this, DB_ROW_EXTRA);
 //		Intent i = new Intent("android.intent.action.VIEW",Uri.parse("statusnet://friends/"+DB_ROW_EXTRA));
 //		startActivityForResult(i, ACTIVITY_FRIENDS);
 	}
 	
 	protected void getMentions() {
 		MustardMention.actionHandleTimeline(this, mStatusNet.getUsernameId());
 //		Intent i = new Intent("android.intent.action.VIEW",Uri.parse("statusnet://mentions/"+mStatusNet.getUsernameId()));
 //		startActivityForResult(i, ACTIVITY_MENTIONS);
 	}
 	
 	private void getFavorites() {
 		MustardFavorite.actionHandleTimeline(this,DB_ROW_EXTRA);
 	}
 	
 	private void getPublic() {
 		Intent i = new Intent("android.intent.action.VIEW",Uri.parse("statusnet://public/"));
 		startActivityForResult(i, ACTIVITY_PUBLIC);
 	}
 	
 	private void update() {
 //		Intent i = new Intent(this, MustardUpdate.class);
 //		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //		startActivityForResult(i, ACTIVITY_CREATE);
 		MustardUpdate.actionCompose(this,mHandler);
 	}
 	
 	private void search() {
 		Intent i = new Intent(this, Search.class);
 		startActivity(i);
 	}
 	
 	private void bookmark() {
 		Intent i = new Intent(this, Bookmark.class);
 		startActivity(i);
 	}
 	
 	private void bookmarkThis() {
 		try {
 			mDbHelper.createBookmark(mStatusNet.getUserId(), DB_ROW_TYPE, DB_ROW_EXTRA);
 		} catch (MustardException e) {
 			if(MustardApplication.DEBUG) Log.e(TAG, e.getMessage());
 		}
 	}
 		
 //	private void avatar() {
 //		Intent i = new Intent(this, Avatar.class);
 //		startActivity(i);
 //	}
 	
 //	private void oauthSettings() {
 //		Intent i = new Intent(this, OAuthSettings.class);
 //		startActivity(i);
 //	}
 	
 	private void settings() {
 		Intent i = new Intent(this, Settings.class);
 		startActivity(i);
 	}
 	
 	protected void showToastMessage(CharSequence message) {
 		showToastMessage(message,false);
 	}
 
 	protected void showToastMessage(CharSequence message,boolean longView) {
 		int popTime = longView ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
 		Toast.makeText(this,
                 message,
                 popTime).show();
 	}
 	
 	protected void showAlertMessage(String errorTitle,String errorMessage) {
 		new AlertDialog.Builder(this)
 		.setTitle(errorTitle)
 		.setMessage(errorMessage)
 		.setNeutralButton(getString(R.string.close), null).show();
 	}
 	
 	protected void showAlertMessageAndFinish(String errorTitle,String errorMessage) {
 		new AlertDialog.Builder(this)
 		.setTitle(errorTitle)
 		.setMessage(errorMessage)
 		.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				finish();
 			}
 		}).show();
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		ProgressDialog dialog;
 	    switch(id) {
 	    case DIALOG_FETCHING_ID:
 	        // do the work to define the pause Dialog
 	    	dialog = new ProgressDialog(this);
 	    	dialog.setIndeterminate(true);
 	    	dialog.setCancelable(true);
 	    	dialog.setMessage(getString(R.string.please_wait_fetching_dents));
 	        break;
 
 	    case DIALOG_OPENING_ID:
 	    	dialog = new ProgressDialog(this);
 	    	dialog.setIndeterminate(true);
 	    	dialog.setCancelable(true);
 	    	dialog.setMessage(getString(R.string.please_wait_opening));
 	        break;
 	        
 	    default:
 	    	if(MustardApplication.DEBUG) Log.d(TAG,"onCreateDialog null....");
 	        dialog = null;
 	    }
 	    return dialog;
 	}
 
 
 	private void getStatusNet() {
 		MustardApplication _ma = (MustardApplication) getApplication();
 		mStatusNet = _ma.checkAccount(mDbHelper);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (MustardApplication.DEBUG) Log.i(TAG,"onActivityResult");
 		super.onActivityResult(requestCode, resultCode, intent);
 		if (requestCode == ACCOUNT_ADD || requestCode == ACCOUNT_ADD_SWITCH) {
 			if (resultCode == RESULT_OK) {
 				// ... have to recheck
 				mStatusNet = null;
 				if (MustardApplication.DEBUG) 
 					Log.d(TAG,"Back OK from ActivityResult " + requestCode);
 				getStatusNet();
 				onBeforeFetch();
 				changeTitle();
 				getStatuses();
 			} else {
 //				Log.i(TAG, "Finshed..." );
 				finish();
 			}
 		} else if (requestCode == ACCOUNT_DEL) {
 			if (!isTaskRoot()) {
 			      setResult(ACCOUNT_DEL);
 			}
 			finish();
 		} else if (requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_CREATE) {
 			if(mPreferences.getBoolean(Preferences.REFRESH_ON_POST_ENABLES_KEY, false) && 
 					resultCode == RESULT_OK) {
 				if(MustardApplication.DEBUG) Log.d(TAG, "Refresh");
 				refresh();
 			}
 		}
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		mCurrentRowid=info.id;
 //		Log.d(TAG,"Set mCurrentRowid " + mCurrentRowid);
 		Cursor c = mDbHelper.fetchStatus(info.id);
 		BitmapDrawable _icon = new BitmapDrawable(
 				MustardApplication.sImageManager.get(
 						c.getString(
 								c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_IMAGE)
 						)
 				)
 		);
 		String userName = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_SCREEN_NAME));
 		long usernameId = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID));
 //		Log.v(TAG, "Username id: " + usernameId + " vs " + mStatusNet.getUsernameId());
 		menu.setHeaderTitle(userName);
 		menu.setHeaderIcon(_icon);
 
 		if(!mLayoutNewButton) {
 //			menu.add(0, REPLY_ID, 0, R.string.menu_reply);
 //	
 //			boolean favorited = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FAVORITE)) == 1 ? true : false;
 //			if (favorited)
 //				menu.add(0, UNFAVORS_ID,0, R.string.menu_unfav).setIcon(android.R.drawable.star_off);
 //			else
 //				menu.add(0, FAVORS_ID,0, R.string.menu_fav).setIcon(android.R.drawable.star_on);
 //	
 //			menu.add(0, REPEAT_ID, 0, R.string.menu_forward);
 			
 			menu.add(0, SHARE_ID, 0, R.string.menu_share);
 			menu.add(0, COPY2CLIPBOARD_ID, 0, R.string.menu_copy2clipboard);
 			
 //			SubMenu accountMenu = menu.addSubMenu(0, ACCOUNT_MENU_ID, 0, R.string.menu_account);
 //			accountMenu.setHeaderTitle(userName);
 //	
 //			accountMenu.setHeaderIcon(_icon);
 //			
 //			accountMenu.add(0, USER_TL_ID, 0, R.string.menu_timeline);
 			menu.add(0, USER_TL_ID, 0, R.string.menu_timeline);
 			if (usernameId != mStatusNet.getUsernameId()) {
 				boolean following = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FOLLOWING)) == 1 ? true : false;
 				if (following)
 					menu.add(0, M_UNSUB_ID,0, R.string.menu_unsub);
 				else
 					menu.add(0, M_SUB_ID,0, R.string.menu_sub);
 				menu.add(0,BLOCK_ID,0,R.string.menu_block);
 			}
 			
 //			String geo = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_GEO));
 //			
 //			if ("1".equals(geo)) {
 //				accountMenu.add(0,GEOLOCATION_ID, 0,R.string.menu_view_geo);
 //			}
 			
 			
 		} else {
 			menu.add(0, USER_TL_ID, 0, R.string.menu_timeline);
 	
 			if (usernameId != mStatusNet.getUsernameId()) {
 				boolean following = c.getInt(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_FOLLOWING)) == 1 ? true : false;
 				if (following)
 					menu.add(0, M_UNSUB_ID,0, R.string.menu_unsub);
 				else
 					menu.add(0, M_SUB_ID,0, R.string.menu_sub);
 				menu.add(0,BLOCK_ID,0,R.string.menu_block);
 	
 			}
 			
 			String geo = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_GEO));
 			
 			if ("1".equals(geo)) {
 				menu.add(0,GEOLOCATION_ID, 0,R.string.menu_view_geo);
 			}
 			
 		}
 		if (usernameId == mStatusNet.getUsernameId()) {
 			menu.add(0, DELETE_ID, 0, R.string.menu_delete).setIcon(android.R.drawable.ic_delete);
 		}
 		
 		try {
 			c.close();
 		} catch(Exception e) {
 		}
 	}
 	
 	private void doOpenConversation(long statusId) {
 		MustardConversation.actionHandleTimeline(this,statusId);
 	}
 	
 	private void doShowLocation(final String lon,final String lat) {
 		new Thread() {
             public void run() {
             	Controller.getInstance(getApplication())
             		.loadGeoNames(getApplication(), lon, lat, mListener);
             }
 		}.start();
 	}
 	
 	private void doShowGeolocation(GeoName gn) {
 		Builder b = StatusNetUtils.getGeoInfo(this, gn);
 		b.show();
 	}
 	
 	private void doShare(String text) {
 		Intent i = new Intent(Intent.ACTION_SEND);
 		i.setType("text/plain");
 		i.putExtra(Intent.EXTRA_TEXT, text);
 		startActivity(i);
 	}
 	
 	@Override
 	public void onContextMenuClosed(Menu menu) {
 		super.onContextMenuClosed(menu);
 //		Log.d(TAG,"Remove mCurrentRowid");
 		mCurrentRowid = -1;
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		long rowid = mCurrentRowid;
 
 		Intent i = null;
 		Cursor c = null;
 		int x = item.getItemId();
 		
 		switch (x) {
 		case REPLY_ID:
 			MustardUpdate.actionReply(this,mHandler, rowid);
 			return true;
 			
 		case REPEAT_ID:
 			boolean newRepeat = mPreferences.getBoolean(Preferences.NEW_REPEAT_ENABLES_KEY, false);
 			if(newRepeat) {
 				c = mDbHelper.fetchStatus(rowid);
 				String ss = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 				repeat(ss);
 				try { c.close(); } catch (Exception e) {}
 			} else {
 				MustardUpdate.actionForward(this,mHandler, rowid);
 			}
 			return true;
 		
 		case SHARE_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			String text = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS));
 			doShare(text);
 			try { c.close(); } catch (Exception e) {}
 			return true;
 		
 		case COPY2CLIPBOARD_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			String id = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID));
 			try { c.close(); } catch (Exception e) {}
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			String url = mStatusNet.getURL().toExternalForm();
 			if (url.endsWith("/api"))
 				url = url.substring(0, -4);
 			clipboard.setText(url+"/notice/"+id);
 			Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show();
 			return true;
 			
 		case USER_TL_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			long userid = c.getLong(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID));
 			if (MustardApplication.DEBUG) Log.d(TAG,"Called user timeline userid " + userid);
 			i = new Intent("android.intent.action.VIEW",Uri.parse("statusnet://users/"+userid));
 			startActivityForResult(i,0);
 			try { c.close(); } catch (Exception e) {}
 			return true;
 			
 		case DELETE_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			if(mStatusNet.delete(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID)))) {
 				mDbHelper.deleteStatus(rowid);
 				Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
 				fillData();
 			} else {
 				Toast.makeText(this, "Can't Delete", Toast.LENGTH_SHORT).show();
 			}
 			try { c.close(); } catch (Exception e) {}
 			return true;
 			
 		case FAVORS_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			new StatusFavor().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID)));
 			try { c.close(); } catch (Exception e) {}
 			return true;
 			
 		case UNFAVORS_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			new StatusDisfavor().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_STATUS_ID)));
 			try { c.close(); } catch (Exception e) {}
 			return true;
 		
 			
 		case M_SUB_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			new StatusSubscribe().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID)));
 			try { c.close(); } catch (Exception e) {}
 			return true;
 	
 		case M_UNSUB_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			new StatusUnsubscribe().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID)));
 			try { c.close(); } catch (Exception e) {}
 			return true;
 			
 		case BLOCK_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			new StatusBlock().execute(c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_USER_ID)));
 			try { c.close(); } catch (Exception e) {}
 			return true;
 		
 		case GEOLOCATION_ID:
 			c = mDbHelper.fetchStatus(rowid);
 			String lon = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LON));
 			String lat = c.getString(c.getColumnIndexOrThrow(MustardDbAdapter.KEY_LAT));
 			doShowLocation(lon, lat);
 			try { c.close(); } catch (Exception e) {}
 			return true;
 
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	private void doJoinGroup() {
 		new StatusGroupJoin().execute(DB_ROW_EXTRA);
 	}
 	
 	protected void doSubscribe() {
 	}
 	
 	protected void doUnsubscribe() {
 	}
 	
 	private void doLeaveGroup() {
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getString(R.string.warning_leave_group,DB_ROW_EXTRA))
 		.setCancelable(false)
 		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				new StatusGroupLeave().execute(DB_ROW_EXTRA);
 			}
 		})
 		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface xdialog, int id) {
 				xdialog.cancel();
 			}
 		});
 		builder.create();
 		builder.show();
 	}
 	
 	private void showIntederminateProgressBar(boolean show) {
 		setProgressBarIndeterminateVisibility(show);
 	}
 	
 	protected abstract void onAfterFetch() ;
 	
 	protected void doLoadMore() {
 		if (MustardApplication.DEBUG) Log.d(TAG, "Attempting load more.");
 
 		if (mLoadMoreTask != null
 				&& mLoadMoreTask.getStatus() == Status.RUNNING 
 			 ) {
 			if(MustardApplication.DEBUG) Log.w(TAG, "Already loading more.");
 		} else {
 			if (mNoMoreDents) {
 				if(MustardApplication.DEBUG) Log.w(TAG, "Reached NoMoreDent!");
 			} else {
 				if (mMergedTimeline)
 					mLoadMoreTask = new MergedStatusesLoadMore();
 				else
 					mLoadMoreTask = new StatusesLoadMore();
 				mLoadMoreTask.execute();
 			}
 		}
 	}
 	
 	protected void doSilentRefresh() {
 		if(MustardApplication.DEBUG) Log.d(TAG, "Silent Refresh.");
 		
 		if(mFetcherTask != null
 				&& mFetcherTask.getStatus()==Status.RUNNING) {
 			if(MustardApplication.DEBUG) 
 				if(MustardApplication.DEBUG) Log.w(TAG, "Already fetching statuses");
 		} else {
 			if(MustardApplication.DEBUG) Log.i(TAG, "Fetching statuses silently");
 			if (mMergedTimeline)
 				mFetcherTask = new MultiStatusesFetcher();
 			else
 				mFetcherTask = new StatusesFetcher();
 			mFetcherTask.setSilent(true);
 			mFetcherTask.execute();
 		}
 	}
 	
 	protected boolean mMergedTimeline=false;
 	
 	protected void getStatuses() {
 		getStatuses(mMergedTimeline);
 	}
 	
 	protected void getStatuses(boolean multiple) {
 		if(mFromSavedState) {
 			mFromSavedState=false;
 			fillData();
 			return;
 		}
 		if (MustardApplication.DEBUG) Log.d(TAG,"Attempting fetching statuses");
 		if(mFetcherTask != null
 				&& mFetcherTask.getStatus()==Status.RUNNING) {
 			if(MustardApplication.DEBUG) 
 				if(MustardApplication.DEBUG) Log.w(TAG, "Already fetching statuses");
 		} else {
 			if(MustardApplication.DEBUG) Log.i(TAG, "Fetching statuses");
 			if(multiple) {
 				mMergedTimeline=true;
 				Log.i(TAG, "MULTIPLE STATUS FETCHER!!!");
 				mFetcherTask = new MultiStatusesFetcher();
 				mFetcherTask.execute();
 			} else {
 				mFetcherTask = new StatusesFetcher();
 				mFetcherTask.execute();
 			}
 		}
 	}
 	
 	protected void fillData() {
 		
 		Cursor mNoticesCursor = mDbHelper.fetchAllStatuses(DB_ROW_TYPE, DB_ROW_EXTRA,DB_ROW_ORDER);
 
 		if (mNoticesCursor == null) {
 			Log.e(TAG, "Cursor is null.. ");
 			return;
 		}
 		
 		startManagingCursor(mNoticesCursor);
 
 		if(MustardApplication.DEBUG) 
 			Log.d(TAG,"Found: " + mNoticesCursor.getCount() + " rows");
 		mNoticeCursorAdapter.changeCursor(mNoticesCursor);
 	}
 
 	public class StatusesFetcher extends AsyncTask<Void, Integer, Integer> {
 
 		private final String TAG = "StatusesFetcher";
 		protected boolean mGetdents=false;
 		protected boolean mSilent = false;
 		
 		public void setSilent(boolean silent) {
 			mSilent=silent;
 		}
 		
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) 
 				Log.i(TAG, "background task - start");
 			
 			ArrayList<org.mustard.statusnet.Status> al = null;
 			try {
 				if (mStatusNet==null) {
 					Log.e(TAG, "Statusnet is null!");
 					return 0;
 				}
 				long maxId = mDbHelper.fetchMaxStatusesId(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA);
 				al=mStatusNet.get(DB_ROW_TYPE,DB_ROW_EXTRA,maxId,true);
 				if(al==null || al.size()< 1) {
 					return 0;
 				} else {
 					mGetdents=mDbHelper.createStatuses(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,al);
 				}
 			} catch(Exception e) {
 				if (MustardApplication.DEBUG) 
 					e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return -1;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 			}
 			return 1;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			if (MustardApplication.DEBUG) Log.i(TAG, "onPreExecute");
 			try {
 				if(mSilent)
 					showIntederminateProgressBar(true);
 				else
 					showDialog(MustardBaseActivity.DIALOG_FETCHING_ID);
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		protected void onPostExecute(Integer result) {
 			if(mSilent)
 				showIntederminateProgressBar(false);
 			else
 				dismissDialog(MustardBaseActivity.DIALOG_FETCHING_ID);
 			try {
 				if (result==-1) {
 					showToastMessage(getText(R.string.error_fetch_dents)+"\n"+mErrorMessage);
 				} else if (result == -10) {
 					showToastMessage("Merged timeline active but no account selected!");
 				} else {
 					if(mGetdents) {
 						onAfterFetch();
 					}
 					fillData();
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {
 				if(mIsRefresh)
 					mIsRefresh=false;
 			}
 		}
 
 	}
 	
 	public class MultiStatusesFetcher extends StatusesFetcher {
 
 		private final String TAG = "MultiStatusesFetcher";
 
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) 
 				Log.i(TAG, "background task - start");
 			
 			
 			try {
 				if (mStatusNet==null) {
 					Log.e(TAG, "Statusnet is null!");
 					return 0;
 				}
 				
 				MustardApplication _ma = (MustardApplication) getApplication();
 				StatusNet _sn = null;
 				boolean haveAtLeastOneAccount=false;
 				boolean haveAtLeastOneStatus=false;
 				Cursor c = mDbHelper.fetchAllAccountsToMerge();
 				while(c.moveToNext()) {
 					ArrayList<org.mustard.statusnet.Status> al = null;
 					haveAtLeastOneAccount=true;
 					long _aid = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 					_sn = _ma.checkAccount(mDbHelper,false,_aid);
 //					Log.i(TAG, "Fetching " + _sn.getMUsername() + "@" + _sn.getURL().getHost());
 					long maxId = mDbHelper.fetchMaxStatusesId(_aid, DB_ROW_TYPE,DB_ROW_EXTRA);
 					try {
 						al=_sn.get(DB_ROW_TYPE,Long.toString(_sn.getUsernameId()),maxId,true);
 					} catch (MustardException e) {
 						Log.e(TAG,e.toString());
 					}
 					if(al==null || al.size()< 1) {
 						continue;
 					} else {
 						haveAtLeastOneStatus=true;
 						mGetdents=mDbHelper.createStatuses(_aid,DB_ROW_TYPE,DB_ROW_EXTRA,al);
 					}
 				}
 				c.close();
 				if(!haveAtLeastOneAccount) {
 					return -10;
 				}
 				if(!haveAtLeastOneStatus) {
 					return 0;
 				}
 				
 			} catch(Exception e) {
 				if (MustardApplication.DEBUG) 
 					e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return -1;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 			}
 			return 1;
 		}
 
 	}
 	
 	
 	public class StatusRepeat extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = "StatusRepeat";
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				mStatusNet.doRepeat(s[0]);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_repeat));
 				} else {
 					showToastMessage(getText(R.string.error_repeat)+"\n"+mErrorMessage,true);
 					mPreferences.edit().putBoolean(Preferences.NEW_REPEAT_ENABLES_KEY, false).commit();
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusFavor extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = "StatusFavor";
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				mStatusNet.doFavour(s[0]);
 				mDbHelper.updateStatusFavor(s[0], true);
 			} catch(Exception e) {
 				e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_fav));
 				} else {
 					showToastMessage(getText(R.string.error_fav)+"\n"+mErrorMessage,true);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusDisfavor extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = "StatusDisfavor";
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				mStatusNet.doDisfavour(s[0]);
 				mDbHelper.updateStatusFavor(s[0], false);
 			} catch(Exception e) {
 				e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_unfav));
 				} else {
 					showToastMessage(getText(R.string.error_unfav)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusBlock extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				mStatusNet.doBlock(s[0]);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_block));
 				} else {
 					showToastMessage(getText(R.string.error_block)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusGroupJoin extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				String group = s[0];
 				mStatusNet.doJoinGroup(group);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_join));
 				} else {
 					showToastMessage(getText(R.string.error_join)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusGroupLeave extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				String group = s[0];
 				mStatusNet.doLeaveGroup(group);
 			} catch(Exception e) {
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_leave));
 				} else {
 					showToastMessage(getText(R.string.error_leave)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusSubscribe extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				if(mStatusNet.doSubscribe(s[0])) {
 					mDbHelper.updateStatusFollowing(s[0], true);
 				} else {
 					mErrorMessage=getString(R.string.error_sub);
 					return 0;
 				}
 			} catch(MustardException e) {
 				mErrorMessage=e.getMessage();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_sub));
 				} else {
 					showToastMessage(getText(R.string.error_sub)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusUnsubscribe extends AsyncTask<String, Integer, Integer> {
 
 		private final String TAG = getClass().getCanonicalName();
 
 		@Override
 		protected Integer doInBackground(String... s) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			
 			try {
 				if(mStatusNet.doUnsubscribe(s[0])) {
 					mDbHelper.updateStatusFollowing(s[0], false);
 				} else {
 					return 0;
 				}
 			} catch(MustardException e) {
 				mErrorMessage=e.getMessage();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return 0;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end ");
 			}
 			return 1;
 		}
 
 		protected void onPostExecute(Integer result) {
 			try {
 				if (result>0) {
 					showToastMessage(getText(R.string.confirm_unsub));
 				} else {
 					showToastMessage(getText(R.string.error_unsub)+"\n"+mErrorMessage);
 				}
 			} catch(IllegalArgumentException e) {
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			} finally {				
 			}
 		}
 	}
 	
 	public class StatusesLoadMore extends AsyncTask<Void, Integer, Integer> {
 
 		private final String TAG = "StatusesLoadMore";
 		protected boolean mGetdents=false;
 		
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 			long maxId = mDbHelper.fetchMinStatusesId(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA);
 			Log.v(TAG,"Search " + (maxId-1));
 			if (maxId-1 < 1) {
 				return -1;
 			}
 			ArrayList<org.mustard.statusnet.Status> al = null;
 			try {
 				al=mStatusNet.get(DB_ROW_TYPE,DB_ROW_EXTRA,maxId-1,false);
 				
 				if(al==null) {
 					return -1;
 				} else if (al.size()< 1) {
 					return -1;
 				} else {
 					Log.v(TAG,"Found  " + al.size());
 					mGetdents=mDbHelper.createStatuses(mStatusNet.getUserId(),DB_ROW_TYPE,DB_ROW_EXTRA,al);
 				}
 			} catch(Exception e) {
 				mNoMoreDents=true;
 //				if (MustardApplication.DEBUG) e.printStackTrace();
 				mErrorMessage=e.toString();
 				if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 				return -1;
 			} finally {
 				if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 			}
 			return 1;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			setProgressBarIndeterminateVisibility(true);
 		}
 
 		protected void onPostExecute(Integer result) {
 			setProgressBarIndeterminateVisibility(false);
 
 			if (result<0) {
 				mNoMoreDents=true;
 			} else {
 				if(mGetdents) {
 					fillData();
 				} else {
 					mNoMoreDents=true;
 					showToastMessage(getText(R.string.error_fetch_more_dents)+"\n"+mErrorMessage);
 				}
 			}
 
 		}
 	}
 	
 	public class MergedStatusesLoadMore extends StatusesLoadMore {
 
 		private final String TAG = "MergedStatusesLoadMore";
 		
 		@Override
 		protected Integer doInBackground(Void... v) {
 			if (MustardApplication.DEBUG) Log.i(TAG, "background task - start");
 
 			
 			MustardApplication _ma = (MustardApplication) getApplication();
 			StatusNet _sn = null;
 			boolean haveAtLeastOneStatus=false;
 			Cursor c = mDbHelper.fetchAllAccountsToMerge();
 			while(c.moveToNext()) {
 				long _aid = c.getLong(c.getColumnIndex(MustardDbAdapter.KEY_ROWID));
 				_sn = _ma.checkAccount(mDbHelper,false,_aid);
 				Log.i(TAG, "Fetching " + _sn.getMUsername() + "@" + _sn.getURL().getHost());
 				long maxId = mDbHelper.fetchMinStatusesId(_aid,DB_ROW_TYPE,DB_ROW_EXTRA);
 				Log.v(TAG,"Search " + (maxId-1));
 				if (maxId-1 < 1) {
 					return -1;
 				}
 				ArrayList<org.mustard.statusnet.Status> al = null;
 				try {
 					al=_sn.get(DB_ROW_TYPE,Long.toString(_sn.getUsernameId()),maxId-1,false);
 
 					if(al==null || al.size()< 1) {
 						continue;
 					} else {
 						Log.v(TAG,"Found  " + al.size());
 						mGetdents=mDbHelper.createStatuses(_aid,DB_ROW_TYPE,DB_ROW_EXTRA,al);
 						haveAtLeastOneStatus=true;
 					}
 				} catch(Exception e) {
 					mNoMoreDents=true;
 					//				if (MustardApplication.DEBUG) e.printStackTrace();
 					mErrorMessage=e.toString();
 					if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 					return -1;
 				} finally {
 					if (MustardApplication.DEBUG) Log.i(TAG, "background task - end " + mGetdents);
 				}
 			
 			}
 			c.close();
 			
 			return haveAtLeastOneStatus ? 1 : 0;
 		}
 		
 	}
 	
 	protected TimelineHandler mHandler = new TimelineHandler();
 	
 	class TimelineHandler extends Handler {
 
 		private static final int MSG_PROGRESS = 2;
 		private static final int MSG_GEOLOCATION_OK = 3;
 		private static final int MSG_GEOLOCATION_KO = 4;
 		private static final int MSG_REFRESH = 5;
 		
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_PROGRESS:
 				setProgressBarIndeterminateVisibility(msg.arg1 != 0);
 				break;
 			case MSG_GEOLOCATION_OK:
 				GeoName gn = (GeoName)msg.obj;
 				doShowGeolocation(gn);
 				break;
 			case MSG_GEOLOCATION_KO:
 				showErrorMessage((String)msg.obj);
 				break;
 				
 			case MSG_REFRESH:
 				doSilentRefresh();
 				break;
 			}
 		}
 
 		public void progress(boolean progress) {
 			Message msg = new Message();
 			msg.what = MSG_PROGRESS;
 			msg.arg1 = progress ? 1 : 0;
 			sendMessage(msg);
 		}
 		
 		public void showGeolocation(GeoName geoname) {
 			Message msg = new Message();
 			msg.what = MSG_GEOLOCATION_OK;
 			msg.obj = geoname;
 			sendMessage(msg);			
 		}
 	
 		public void errorGeolocation(String error) {
 			Message msg = new Message();
 			msg.what = MSG_GEOLOCATION_KO;
 			msg.obj = error;
 			sendMessage(msg);			
 		}
 	
 	}
 	
 	private void showErrorMessage(String reason) {
 		Toast.makeText(this, getString(R.string.error_generic_detail,reason), Toast.LENGTH_LONG).show();
 	}
 	
 	private MessagingListener mListener = new MessagingListener() {
 		
 	    public void loadGeonameStarted(Context context) {
 	    	mHandler.progress(true);
 	    }
 	    
 	    public void loadGeonameFinished(Context context, GeoName geoname ) {
 	    	mHandler.progress(false);
 	    	mHandler.showGeolocation(geoname);
 	    }
 
 	    public void loadGeonameFailed(Context context, String reason) {
 	    	mHandler.progress(false);
 	    	mHandler.errorGeolocation(reason);
 	    }
 	};
 	
 	
 }
