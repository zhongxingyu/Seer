 /*
  * Copyright (C) 2010 Daniel Jacobi
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.questmaster.tudmensa;
 
 import java.util.Calendar;
 import de.questmaster.tudmensa.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ExpandableListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.DateFormat;
 import android.view.ContextMenu;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.SimpleCursorTreeAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MensaMeals extends ExpandableListActivity {
 
 	private static final int UPDATE_ID = Menu.FIRST;
 	private static final int TODAY_ID = Menu.FIRST + 1;
 	private static final int SETTINGS_ID = Menu.FIRST + 2;
 
 	private static final int MENU_GROUP_MEAL_ID = 1;
 	private static final int MENU_GROUP_MENSA_ID = 2;
 
 	private static final int MENU_SHARE_ID = ContextMenu.FIRST;
 	private static final int MENU_VOTE_ID = ContextMenu.FIRST + 1;
 
 	protected static final String VOTE_DIALOG_MEAL_ID = "meal_id";
 	protected static final String VOTE_DIALOG_VISUAL_ID = "vote_visual";
 	protected static final String VOTE_DIALOG_TASTE_ID = "vote_taste";
 	protected static final String VOTE_DIALOG_PRICE_ID = "vote_price";
 	protected static final String VOTE_DIALOG_VISUAL_CHANGE_ID = "chg_visual";
 	protected static final String VOTE_DIALOG_TASTE_CHANGE_ID = "chg_taste";
 	protected static final String VOTE_DIALOG_PRICE_CHANGE_ID = "chg_price";
 	protected static final String VOTE_DIALOG_DATE_ID = "meal_date";
 	protected static final String VOTE_DIALOG_MEAL_SCRIPT_ID = "meal_script";
 
 	public static final int ON_SETTINGS_CHANGE = 0;
 
 	private MensaMealsSettings.Settings mSettings = new MensaMealsSettings.Settings();
 	protected MealsDbAdapter mDbHelper;
 
 	private ProgressDialog mPDialog = null;
 	private boolean mRestart = false;
 	protected Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (msg.what == 0) { // progress Dialog data update
 				mPDialog.dismiss();
 				// updateView after update. Don't do it for an update after
 				// startup
 				mRestart = true;
 				// set update time
 				mSettings.setLastUpdate(mContext);
 				fillData();
 
 				invokeVoteUpdater();
 
 			} else if (msg.what == 1) { // VoteHelper finished updating votes
 
 				// update data displayed
 				mRestart = true;
 				fillData();
 			}
 		}
 	};
 
 	private Calendar mToday = Calendar.getInstance();
 	protected Context mContext = this;
 	protected MensaMeals mActivity = this;
 	private String mOldTheme;
 	private GestureDetector gestureDetector;
 	private Bundle mVoteDialogData;
 	private Thread voteUpdater;
 
 	/**
 	 * Copied from K9mail.
 	 * 
 	 * 
 	 */
 	public class MyGestureDetector extends SimpleOnGestureListener {
 		private static final float SWIPE_MIN_DISTANCE_DIP = 130.0f;
 		private static final float SWIPE_MAX_OFF_PATH_DIP = 250f;
 		private static final float SWIPE_THRESHOLD_VELOCITY_DIP = 325f;
 
 		@Override
 		public boolean onDoubleTap(MotionEvent ev) {
 			super.onDoubleTap(ev);
 
 			if (mSettings.m_bGestures) {
 				onClickTodayButton(null);
 			}
 			return false;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			if (mSettings.m_bGestures) {
 				// Convert the dips to pixels
 				final float mGestureScale = getResources().getDisplayMetrics().density;
 				int min_distance = (int) (SWIPE_MIN_DISTANCE_DIP * mGestureScale + 0.5f);
 				int min_velocity = (int) (SWIPE_THRESHOLD_VELOCITY_DIP * mGestureScale + 0.5f);
 				int max_off_path = (int) (SWIPE_MAX_OFF_PATH_DIP * mGestureScale + 0.5f);
 
 				try {
 					if (Math.abs(e1.getY() - e2.getY()) > max_off_path)
 						return false;
 					// right to left swipe
 					if (e1.getX() - e2.getX() > min_distance && Math.abs(velocityX) > min_velocity) {
 						onClickNextButton(null);
 					} else if (e2.getX() - e1.getX() > min_distance && Math.abs(velocityX) > min_velocity) {
 						onClickPrevButton(null);
 					}
 				} catch (Exception e) {
 					// nothing
 				}
 			}
 			return false;
 		}
 	}
 
 	public class CustomCursorTreeAdapter extends SimpleCursorTreeAdapter {
 
 		private int[] mChildFrom;
 		private int[] mChildTo;
 		private int mChildLayoutUsed;
 
 		public CustomCursorTreeAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
 			super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
 
 			mChildLayoutUsed = childLayout;
 
 			mChildFrom = new int[childFrom.length];
 			initFromColumns(getChildrenCursor(cursor), childFrom, mChildFrom);
 
 			mChildTo = childTo;
 		}
 
 		/**
 		 * Copied from SimpleCurserTreeAdapter
 		 * 
 		 * @param cursor
 		 * @param fromColumnNames
 		 * @param fromColumns
 		 */
 		private void initFromColumns(Cursor cursor, String[] fromColumnNames, int[] fromColumns) {
 			if (cursor != null)
 				for (int i = fromColumnNames.length - 1; i >= 0; i--) {
 					fromColumns[i] = cursor.getColumnIndexOrThrow(fromColumnNames[i]);
 				}
 		}
 
 		@Override
 		protected Cursor getChildrenCursor(Cursor groupCursor) {
 			Cursor c = null;
 
 			if (groupCursor.getCount() > 0) {
 				String location = groupCursor.getString(groupCursor.getColumnIndex(MealsDbAdapter.KEY_LOCATION));
 				String date = groupCursor.getString(groupCursor.getColumnIndex(MealsDbAdapter.KEY_DATE));
 				String counter = groupCursor.getString(groupCursor.getColumnIndex(MealsDbAdapter.KEY_COUNTER));
 
 				if (mSettings.m_bEnableVoting) {
 					c = new MealsDbCursorWrapper(mDbHelper.fetchMealsOfGroupDayPlusVote(location, date, counter), mContext);
 				} else {
 					c = new MealsDbCursorWrapper(mDbHelper.fetchMealsOfGroupDay(location, date, counter), mContext);
 				}
 				startManagingCursor(c);
 			}
 
 			return c;
 		}
 
 		@Override
 		protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
 			int[] from = mChildFrom;
 			int[] to = mChildTo;
 
 			for (int i = 0; i < to.length; i++) {
 				View v = view.findViewById(to[i]);
 				if (v != null) {
 					String text = cursor.getString(from[i]);
 					if (text == null) {
 						text = "";
 					}
 					if (v instanceof TextView) {
 						((TextView) v).setText(text);
 					} else if (v instanceof ImageView) {
 						setViewImage((ImageView) v, text);
 					} else if (v instanceof RatingBar) {
 						Float value = cursor.getFloat(from[i]);
 						((RatingBar) v).setRating(value);
 					} else {
 						throw new IllegalStateException("CustomCursorAdapter can bind values only to" + " RatingBar, TextView and ImageView!");
 					}
 				}
 			}
 		}
 
 		@Override
 		public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
 			View new_view = super.newChildView(context, cursor, isLastChild, parent);
 
 			// check if data avail, if not...
 			if (mChildLayoutUsed == R.layout.simple_expandable_list_item_2_rating) {
 				if (cursor.getFloat(cursor.getColumnIndexOrThrow(MealsDbAdapter.KEY_RESULT_VISUAL)) == 0 && cursor.getFloat(cursor.getColumnIndexOrThrow(MealsDbAdapter.KEY_RESULT_PRICE)) == 0
 						&& cursor.getFloat(cursor.getColumnIndexOrThrow(MealsDbAdapter.KEY_RESULT_TASTE)) == 0) {
 					// hide stuff
 					View holder = new_view.findViewById(R.id.ratingHolder);
 					holder.setVisibility(View.GONE);
 				} else {
 					// show stuff
 					View holder = new_view.findViewById(R.id.ratingHolder);
 					holder.setVisibility(View.VISIBLE);
 				}
 			}
 
 			return new_view;
 		}
 
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// Read settings
 		mSettings.ReadSettings(this);
 
 		// Setup Theme
 		if (mSettings.m_sThemes.equals("dark")) {
 			setTheme(R.style.myTheme);
 		} else if (mSettings.m_sThemes.equals("light")) {
 			setTheme(R.style.myThemeLight);
 		}
 
 		// Set Content
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.meals_list);
 		registerForContextMenu(getExpandableListView());
 
 		// Init Database
 		mDbHelper = new MealsDbAdapter(this);
 		mDbHelper.open();
 
 		// Setup date
 		mToday = Calendar.getInstance();
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		// Capture our buttons from layout and set them up
 		Button buttonPrev = (Button) findViewById(R.id.btn_prev);
 		Button buttonNext = (Button) findViewById(R.id.btn_next);
 		buttonPrev.setBackgroundResource(R.drawable.ic_menu_back);
 		buttonNext.setBackgroundResource(R.drawable.ic_menu_forward);
 
 		Button buttonDate = (Button) findViewById(R.id.txt_date);
 		registerForContextMenu(buttonDate);
 
 		updateButtonText();
 
 		// Gesture detection
 		gestureDetector = new GestureDetector(new MyGestureDetector());
 		
 		// FIXME: show help dialog
 		int ver;
 		try {
 			ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
 			if (mSettings.m_iShowDialog != ver) {
 				AlertDialog.Builder d = new AlertDialog.Builder(this);
 				d.setTitle(getResources().getString(R.string.help_dialog_title));
 				d.setMessage(getResources().getString(R.string.help_dialog_msg));
 				d.show();
				
				mSettings.setLastDialogShown(this);
 			}
 		} catch (NameNotFoundException e) {
 			// Nothing to do
 		}
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		super.dispatchTouchEvent(ev);
 		return gestureDetector.onTouchEvent(ev);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		MenuItem mItem = null;
 
 		mItem = menu.add(0, UPDATE_ID, 0, R.string.menu_update);
 		mItem.setIcon(R.drawable.ic_menu_refresh);
 
 		mItem = menu.add(0, TODAY_ID, 1, R.string.menu_today);
 		mItem.setIcon(android.R.drawable.ic_menu_today);
 
 		mItem = menu.add(0, SETTINGS_ID, 2, R.string.menu_settings);
 		mItem.setIcon(android.R.drawable.ic_menu_preferences);
 
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case UPDATE_ID:
 			getData();
 			fillData();
 			break;
 
 		case TODAY_ID:
 			onClickTodayButton(null);
 			break;
 
 		case SETTINGS_ID:
 			Intent iSettings = new Intent();
 			iSettings.setClass(this, MensaMealsSettings.class);
 			startActivityForResult(iSettings, ON_SETTINGS_CHANGE);
 
 			// To be able to check for new data if mensa changed.
 			mRestart = false;
 
 			// Store old theme
 			mOldTheme = mSettings.m_sThemes;
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ON_SETTINGS_CHANGE:
 			mSettings.ReadSettings(this);
 
 			// WORKAROUND: restart activity TODO may have side-effects in froyo
 			if (!mOldTheme.equals(mSettings.m_sThemes)) {
 				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 			}
 
 			// Reread data and display it
 			updateButtonText();
 			fillData();
 
 			break;
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		if (v instanceof ExpandableListView) {
 			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
 			int type = ExpandableListView.getPackedPositionType(info.packedPosition);
 
 			// Only create a context menu for child items
 			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 				menu.setHeaderTitle(getResources().getString(R.string.contextmenu_meals));
 				menu.add(MENU_GROUP_MEAL_ID, MENU_SHARE_ID, 0, getResources().getString(R.string.contextmenu_share_with_friends));
 				if (mSettings.m_bEnableVoting && (mToday.before(Calendar.getInstance()) || mToday.equals(Calendar.getInstance()))) {
 					menu.add(MENU_GROUP_MEAL_ID, MENU_VOTE_ID, 1, getResources().getString(R.string.contextmenu_vote));
 				}
 			}
 		} else if (v instanceof Button) {
 
 			menu.setHeaderTitle(getResources().getString(R.string.pref_MensaLocationLabel));
 			int pos = 0;
 			for (String loc : getResources().getStringArray(R.array.MensaLocations)) {
 				menu.add(MENU_GROUP_MENSA_ID, pos, pos, loc);
 				pos++;
 			}
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		if (item.getGroupId() == MENU_GROUP_MEAL_ID) {
 			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
 
 			// Pull values from the array we built when we created the list
 			// String
 			Cursor c = mDbHelper.fetchMeal(info.id);
 			startManagingCursor(c);
 
 			// get info
 			String meal = c.getString(c.getColumnIndex(MealsDbAdapter.KEY_NAME));
 			String mensa = c.getString(c.getColumnIndex(MealsDbAdapter.KEY_LOCATION));
 
 			switch (item.getItemId()) {
 			case MENU_SHARE_ID:
 				Intent share = new Intent(Intent.ACTION_SEND);
 				share.setType("text/plain");
 				share.putExtra(Intent.EXTRA_SUBJECT, String.format(getResources().getString(R.string.share_subject), DateFormat.getDateFormat(this).format(mToday.getTime())));
 				share.putExtra(Intent.EXTRA_TEXT,
 						String.format(getResources().getString(R.string.share_checkout), meal, DateFormat.getDateFormat(this).format(mToday.getTime()), getMensaLocationString(mensa)));
 
 				startActivity(Intent.createChooser(share, getResources().getString(R.string.share_where_to)));
 				return true;
 			case MENU_VOTE_ID:
 				long meal_id = c.getLong(c.getColumnIndex(MealsDbAdapter.KEY_ROWID));
 				String meal_num = c.getString(c.getColumnIndex(MealsDbAdapter.KEY_MEAL_NUM));
 				String counter = c.getString(c.getColumnIndex(MealsDbAdapter.KEY_COUNTER));
 				float vis = c.getFloat(c.getColumnIndex(MealsDbAdapter.KEY_VOTE_VISUAL));
 				float tst = c.getFloat(c.getColumnIndex(MealsDbAdapter.KEY_VOTE_TASTE));
 				float prc = c.getFloat(c.getColumnIndex(MealsDbAdapter.KEY_VOTE_PRICE));
 
 				mVoteDialogData = new Bundle();
 				mVoteDialogData.putLong(VOTE_DIALOG_MEAL_ID, meal_id);
 				mVoteDialogData.putString(VOTE_DIALOG_MEAL_SCRIPT_ID, (mensa + "|" + counter + "|" + meal_num).replaceAll(" ", "_"));
 				mVoteDialogData.putString(VOTE_DIALOG_DATE_ID, "D" + DateFormat.format("yyyyMMdd", mToday.getTime()));
 				mVoteDialogData.putFloat(VOTE_DIALOG_VISUAL_ID, vis);
 				mVoteDialogData.putFloat(VOTE_DIALOG_TASTE_ID, tst);
 				mVoteDialogData.putFloat(VOTE_DIALOG_PRICE_ID, prc);
 
 				showDialog(R.layout.rating_dialog);
 
 				return true;
 			}
 		} else if (item.getGroupId() == MENU_GROUP_MENSA_ID) {
 			// set Mensa location
 			mSettings.setMensaLocation(mContext, getResources().getStringArray(R.array.MensaLocationsValues)[item.getItemId()]);
 
 			// Reread data and display it
 			updateButtonText();
 			fillData();
 		}
 
 		return super.onContextItemSelected(item);
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 
 		if (id == R.layout.rating_dialog) {
 			AlertDialog.Builder d = new AlertDialog.Builder(this);
 			d.setTitle(getResources().getString(R.string.dialog_title_voting));
 
 			// create view
 			LayoutInflater factory = LayoutInflater.from(this);
 			final View ratingView = factory.inflate(R.layout.rating_dialog, null);
 			d.setView(ratingView);
 
 			d.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					float visual = 0, price = 0, taste = 0;
 					boolean vis_set = false, prc_set = false, tst_set = false;
 
 					dialog.dismiss();
 
 					// get dialog data, only the new stuff
 					RatingBar r = (RatingBar) ratingView.findViewById(R.id.visual);
 					if (!r.isIndicator()) {
 						visual = r.getRating();
 						vis_set = true;
 						mVoteDialogData.putFloat(VOTE_DIALOG_VISUAL_ID, visual);
 					} else {
 						visual = mVoteDialogData.getFloat(VOTE_DIALOG_VISUAL_ID);
 					}
 					mVoteDialogData.putBoolean(VOTE_DIALOG_VISUAL_CHANGE_ID, vis_set);
 
 					r = (RatingBar) ratingView.findViewById(R.id.price);
 					if (!r.isIndicator()) {
 						price = r.getRating();
 						prc_set = true;
 						mVoteDialogData.putFloat(VOTE_DIALOG_PRICE_ID, price);
 					} else {
 						price = mVoteDialogData.getFloat(VOTE_DIALOG_PRICE_ID);
 					}
 					mVoteDialogData.putBoolean(VOTE_DIALOG_PRICE_CHANGE_ID, prc_set);
 
 					r = (RatingBar) ratingView.findViewById(R.id.taste);
 					if (!r.isIndicator()) {
 						taste = r.getRating();
 						tst_set = true;
 						mVoteDialogData.putFloat(VOTE_DIALOG_TASTE_ID, taste);
 					} else {
 						taste = mVoteDialogData.getFloat(VOTE_DIALOG_TASTE_ID);
 					}
 					mVoteDialogData.putBoolean(VOTE_DIALOG_TASTE_CHANGE_ID, tst_set);
 
 					if (vis_set || prc_set || tst_set) {
 						// Save data (DB)
 						long rowId = mVoteDialogData.getLong(VOTE_DIALOG_MEAL_ID);
 						mDbHelper.updateMealInternalVotes(rowId, visual, price, taste);
 
 						// Save data (Inet)
 						new VoteHelper(mVoteDialogData).start();
 
 						invokeVoteUpdater();
 					}
 				}
 			});
 
 			d.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					dialog.cancel();
 				}
 			});
 
 			dialog = d.create();
 		}
 
 		return dialog;
 	}
 
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		if (id == R.layout.rating_dialog) {
 			String voted = "";
 
 			// set prev vote
 			RatingBar r = (RatingBar) dialog.findViewById(R.id.visual);
 			if (mVoteDialogData.getFloat(VOTE_DIALOG_VISUAL_ID) != 0) {
 				r.setIsIndicator(true);
 				voted += getResources().getString(R.string.dialog_visual) + ", ";
 			} else {
 				r.setIsIndicator(false);
 			}
 			r.setRating(mVoteDialogData.getFloat(VOTE_DIALOG_VISUAL_ID));
 
 			r = (RatingBar) dialog.findViewById(R.id.price);
 			if (mVoteDialogData.getFloat(VOTE_DIALOG_PRICE_ID) != 0) {
 				r.setIsIndicator(true);
 				voted += getResources().getString(R.string.dialog_price) + ", ";
 			} else {
 				r.setIsIndicator(false);
 			}
 			r.setRating(mVoteDialogData.getFloat(VOTE_DIALOG_PRICE_ID));
 
 			r = (RatingBar) dialog.findViewById(R.id.taste);
 			if (mVoteDialogData.getFloat(VOTE_DIALOG_TASTE_ID) != 0) {
 				r.setIsIndicator(true);
 				voted += getResources().getString(R.string.dialog_taste) + ", ";
 			} else {
 				r.setIsIndicator(false);
 			}
 			r.setRating(mVoteDialogData.getFloat(VOTE_DIALOG_TASTE_ID));
 
 			// set already voted line
 			TextView txt = (TextView) dialog.findViewById(R.id.alreadyVoted);
 			if (voted.length() > 0) {
 				voted = voted.substring(0, voted.length() - 2) + ".";
 				txt.setText(String.format(getResources().getString(R.string.dialog_already_voted), voted));
 			} else {
 				txt.setText("");
 			}
 		}
 	}
 
 	@Override
 	public void onGroupCollapse(int groupPosition) {
 		// keep the Groups expanded
 		getExpandableListView().expandGroup(groupPosition);
 	}
 
 	public void onClickTodayButton(View v) {
 		mToday = Calendar.getInstance();
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 1);
 		}
 		updateButtonText();
 
 		fillData();
 	}
 
 	public void onClickNextButton(View v) {
 		// Setup date
 		mToday.add(Calendar.DAY_OF_YEAR, 1);
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		// next screen
 		updateButtonText();
 		fillData();
 	}
 
 	public void onClickPrevButton(View v) {
 		// Setup date
 		mToday.add(Calendar.DAY_OF_YEAR, -1);
 		if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, -1);
 		} else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			mToday.add(Calendar.DAY_OF_YEAR, -2);
 		}
 
 		// next screen
 		updateButtonText();
 		fillData();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		// close database
 		mDbHelper.close();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// check for database connection
 		// maybe canceled due to low memory, etc.
 		if (mDbHelper == null || !mDbHelper.isOpen()) {
 			if (!mDbHelper.isOpen()) {
 				mDbHelper.close();
 			}
 			mDbHelper = new MealsDbAdapter(this);
 			mDbHelper.open();
 		}
 
 		invokeVoteUpdater();
 
 		// expand groups
 		fillData();
 	}
 
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 		// show Type and legend information
 		Cursor c = mDbHelper.fetchMeal(id);
 		startManagingCursor(c);
 
 		// get info date
 		String info = c.getString(c.getColumnIndex(MealsDbAdapter.KEY_INFO));
 
 		if (!info.equals(""))
 			// Display Toast message
 			Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
 
 		return true;
 	}
 
 	/*
 	 * Returns Mensa Location String, if id not found in Values, the first
 	 * Location is returned.s
 	 */
 	private String getMensaLocationString(String id) {
 		int pos = 0;
 		boolean found = false;
 		for (String s : getResources().getStringArray(R.array.MensaLocationsValues)) {
 			if (s.equals(id)) {
 				found = true;
 				break;
 			} else
 				pos++;
 		}
 		if (!found)
 			pos = 0;
 
 		return getResources().getStringArray(R.array.MensaLocations)[pos];
 	}
 
 	private void updateButtonText() {
 		// Prepare times
 		Calendar cPrev = (Calendar) mToday.clone();
 		Calendar cNext = (Calendar) mToday.clone();
 		cPrev.add(Calendar.DAY_OF_YEAR, -1);
 		cNext.add(Calendar.DAY_OF_YEAR, 1);
 
 		// check weekends
 		if (cPrev.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			cPrev.add(Calendar.DAY_OF_YEAR, -1);
 		} else if (cPrev.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			cPrev.add(Calendar.DAY_OF_YEAR, -2);
 		}
 		if (cNext.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
 			cNext.add(Calendar.DAY_OF_YEAR, 2);
 		} else if (cNext.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			cNext.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		// // Update Text Prev
 		// Button buttonPrev = (Button) findViewById(R.id.btn_prev);
 		// String textPrev =
 		// DateFormat.getDateFormat(this).format(cPrev.getTime());
 		// buttonPrev.setText(textPrev.substring(0, textPrev.length() - 5));
 		//
 		// // Update Text Next
 		// Button buttonNext = (Button) findViewById(R.id.btn_next);
 		// String textNext =
 		// DateFormat.getDateFormat(this).format(cNext.getTime());
 		// buttonNext.setText(textNext.substring(0, textNext.length() - 5));
 
 		// Set new title + Update label
 		TextView labelDay = (TextView) findViewById(R.id.txt_date);
 		labelDay.setText(getMensaLocationString(mSettings.m_sMensaLocation) + "\n" + DateFormat.format("EEEE", mToday.getTime()) + ", " + DateFormat.getDateFormat(this).format(mToday.getTime()));
 	}
 
 	private boolean doMondayUpdate() {
 		Calendar oNow = Calendar.getInstance();
 
 		// time till last update
 		long lDiff = oNow.getTimeInMillis() - mSettings.m_lLastUpdate;
 
 		// Update is older then a week
 		if (lDiff / 86400000.0 > 7.0)
 			return true;
 
 		// get last Monday
 		Calendar oLastMonday = Calendar.getInstance();
 		oLastMonday.add(Calendar.DAY_OF_MONTH, -1);
 		while (oLastMonday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
 			oLastMonday.add(Calendar.DAY_OF_MONTH, -1);
 		}
 
 		// lastUpdate is older than last monday
 		if (mSettings.m_lLastUpdate < oLastMonday.getTimeInMillis()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	private void fillData() {
 		// prepare date string
 		String date = (String) DateFormat.format("yyyyMMdd", mToday);
 
 		// Get all of the notes from the database and create the item list
 		Cursor c = mDbHelper.fetchGroupsOfDay(mSettings.m_sMensaLocation, date);
 		startManagingCursor(c);
 		// if none found start a new query automatically, also on each monday
 		if (mSettings.m_bAutoUpdate && !mRestart && (c.getCount() == 0 || doMondayUpdate())) {
 			mRestart = true;
 			getData();
 			return;
 		}
 		// startManagingCursor(c);
 
 		String[] group_from = new String[] { MealsDbAdapter.KEY_COUNTER };
 		int[] group_to = new int[] { R.id.counter };
 
 		// Now create an array adapter and set it to display using our row
 		CustomCursorTreeAdapter meals;
 		if (mSettings.m_bEnableVoting) {
 			String[] child_from = new String[] { MealsDbAdapter.KEY_NAME, MealsDbAdapter.KEY_PRICE, MealsDbAdapter.KEY_TYPE, MealsDbAdapter.KEY_RESULT_VISUAL, MealsDbAdapter.KEY_RESULT_PRICE,
 					MealsDbAdapter.KEY_RESULT_TASTE };
 			int[] child_to = new int[] { R.id.meal, R.id.price, R.id.meal_type, R.id.vote_visual, R.id.vote_price, R.id.vote_taste };
 
 			meals = new CustomCursorTreeAdapter(this, c, R.layout.simple_expandable_list_item_1, group_from, group_to, R.layout.simple_expandable_list_item_2_rating, child_from, child_to);
 		} else {
 			String[] child_from = new String[] { MealsDbAdapter.KEY_NAME, MealsDbAdapter.KEY_PRICE, MealsDbAdapter.KEY_TYPE };
 			int[] child_to = new int[] { R.id.meal, R.id.price, R.id.meal_type };
 
 			meals = new CustomCursorTreeAdapter(this, c, R.layout.simple_expandable_list_item_1, group_from, group_to, R.layout.simple_expandable_list_item_2, child_from, child_to);
 		}
 		setListAdapter(meals);
 
 		// expand all items
 		for (int i = 0; i < c.getCount(); i++) {
 			getExpandableListView().expandGroup(i);
 		}
 	}
 
 	private void getData() {
 		mPDialog = ProgressDialog.show(this, null, getResources().getString(R.string.dialog_updating_text), true, true);
 
 		// get data
 		DataExtractor de = new DataExtractor(this, mSettings.m_sMensaLocation);
 		Thread t = new Thread(de);
 		t.start();
 	}
 
 	/**
 	 * 
 	 */
 	private void invokeVoteUpdater() {
 		// start getting new votes
 		if (voteUpdater == null || !voteUpdater.isAlive()) {
 			voteUpdater = new VoteHelper(mActivity, mDbHelper);
 			voteUpdater.start();
 		}
 	}
 }
