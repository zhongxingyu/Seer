 package com.dj.antispam;
 
 import android.content.*;
 import android.database.Cursor;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.dj.antispam.actionbarcompat.ActionBarActivity;
 import com.dj.antispam.dao.DbHelper;
 import com.dj.antispam.dao.SmsDao;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 public class MainActivity extends ActionBarActivity {
 	private static final String TAG = MainActivity.class.getSimpleName();
 	private SmsDao dao;
 	private Preferences prefs;
 	private BroadcastReceiver updater;
 	private Cursor cursor;
 	private long lastViewed;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_import:
 				openImportActivity();
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		dao = new SmsDao(this);
 		prefs = new Preferences(this);
 		lastViewed = prefs.getLastViewedTime();
 
 		setContentView(R.layout.main);
 		final ListView list = (ListView)findViewById(R.id.listView);
 		cursor = dao.getSpamCursor();
 		startManagingCursor(cursor);
 		final CursorAdapter adapter = new CursorAdapter(getApplicationContext(), cursor, true) {
 
 			@Override
 			public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
 				return getLayoutInflater().inflate(R.layout.sms_item, null, false);
 			}
 
 			@Override
 			public void bindView(View view, Context context, Cursor cursor) {
 				TextView from = (TextView) view.findViewById(R.id.from);
 				int colFrom = cursor.getColumnIndex(DbHelper.MESSAGES_FROM);
 				String strFrom = cursor.getString(colFrom);
 				from.setText(strFrom);
 				TextView body = (TextView) view.findViewById(R.id.body);
 				body.setText(cursor.getString(cursor.getColumnIndex(DbHelper.MESSAGES_BODY)));
 				((TextView)view.findViewById(R.id.date)).setText(formatTime(cursor.getLong(cursor.getColumnIndex(DbHelper.MESSAGES_DATETIME))));
 				if (cursor.getLong(cursor.getColumnIndex(DbHelper.MESSAGES_DATETIME)) < lastViewed) {
 					body.setTextColor(getResources().getColor(R.color.read_body));
 				} else {
 					body.setTextColor(getResources().getColor(R.color.unread_body));
 				}
 			}
 
 			private String formatTime(Long time) {
 				return DateFormat.getDateTimeInstance().format(new Date(time));
 			}
 		};
 		list.setAdapter(adapter);
 		list.setOnTouchListener(new View.OnTouchListener() {
 			private float downX;
 			private int downPosition;
 			private VelocityTracker velocityTracker;
 			private final int slop;
 			private final int minFlingVelocity;
 			private final int maxFlingVelocity;
 			private final int animationTime;
 			private boolean swiping;
 
 			private View swipingView = null;
 
 			{
 				ViewConfiguration vc = ViewConfiguration.get(list.getContext());
 				slop = vc.getScaledTouchSlop();
 				minFlingVelocity = vc.getScaledMinimumFlingVelocity();
 				maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
 				animationTime = list.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
 			}
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 					case MotionEvent.ACTION_DOWN:
 						int[] coords = new int[2];
 						list.getLocationOnScreen(coords);
 						swipingView = hitTest((int) (event.getRawX() - coords[0]), (int) (event.getRawY() - coords[1]));
 						if (swipingView != null) {
 							downX = event.getRawX();
 							downPosition = list.getPositionForView(swipingView);
 							velocityTracker = VelocityTracker.obtain();
 							velocityTracker.addMovement(event);
 						}
 						break;
 
 					case MotionEvent.ACTION_UP:
 						if (velocityTracker == null) break;
 						if (swipingView == null) break;
 						float deltaX = event.getRawX() - downX;
 						velocityTracker.addMovement(event);
 						velocityTracker.computeCurrentVelocity(1000);
 						float velocityX = Math.abs(velocityTracker.getXVelocity());
 						float velocityY = Math.abs(velocityTracker.getYVelocity());
 						boolean dismiss = false;
 						boolean dismissRight = false;
 						if (Math.abs(deltaX) > list.getWidth() / 2 &&
 								minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity
 								&& velocityY < velocityX)
 						{
 							dismiss = true;
 							dismissRight = deltaX > 0;
 						}
 						if (dismiss) {
 							// dismiss
 							final boolean fDismissRight = dismissRight;
 /*
 							downView.animate()
 									.translationX(dismissRight ? mViewWidth : -mViewWidth)
 									.alpha(0)
 									.setDuration(mAnimationTime)
 									.setListener(new AnimatorListenerAdapter() {
 										@Override
 										public void onAnimationEnd(Animator animation) {
 											performDismiss(downView, downPosition);
 										}
 									});
 */
 							TranslateAnimation ta = new TranslateAnimation(swipingView.getLeft(),
 									fDismissRight ? swipingView.getLeft() + swipingView.getWidth() :
 									swipingView.getLeft() - swipingView.getWidth(), 0, 0);
 							ta.setDuration(animationTime);
 							ta.setFillAfter(true);
 							ta.setAnimationListener(new Animation.AnimationListener() {
 								@Override
 								public void onAnimationStart(Animation animation) {
 									new Thread(new Runnable() {
 										@Override
 										public void run() {
 											if (fDismissRight) {
 												onDeleteMessage(downPosition);
 											} else {
 												onRestoreMessage(downPosition);
 											}
 											updater.onReceive(getApplicationContext(), new Intent(getResources().getString(R.string.update_action)));
 										}
 									}).start();
 								}
 
 								@Override
 								public void onAnimationEnd(Animation animation) {
 								}
 
 								@Override
 								public void onAnimationRepeat(Animation animation) {
 								}
 							});
 							swipingView.startAnimation(ta);
 						}
 						break;
 
 					case MotionEvent.ACTION_MOVE:
 						if (velocityTracker == null) {
 							break;
 						}
 
 						velocityTracker.addMovement(event);
 						float dX = event.getRawX() - downX;
 						if (Math.abs(dX) > slop) {
 							swiping = true;
 							list.requestDisallowInterceptTouchEvent(true);
 
 							// Cancel ListView's touch (un-highlighting the item)
 							MotionEvent cancelEvent = MotionEvent.obtain(event);
 							cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
 							list.onTouchEvent(cancelEvent);
 						}
 
 						break;
 
 					default:
 						return false;
 				}
 				return false;
 			}
 
 			private View hitTest(int x, int y) {
 				View child;
 				Rect rect = new Rect();
 				for (int i = 0; i < list.getChildCount(); i++) {
 					child = list.getChildAt(i);
 					child.getHitRect(rect);
 					if (rect.contains(x, y)) {
 						return child;
 					}
 				}
 				return null;
 			}
 
 			private void onRestoreMessage(int nItem) {
 				int id = getMessageId(nItem);
 				restoreMessage(id);
 			}
 
 			private int getMessageId(int nItem) {
 				Cursor cur = (Cursor)list.getAdapter().getItem(nItem);
				int col = cur.getColumnIndex("_id");
				return cur.getInt(col);
 			}
 
 			private void onDeleteMessage(int nItem) {
 				int id = getMessageId(nItem);
 				SmsModel message = dao.getMessage(id);
 				dao.deleteMessage(id);
 				dao.markSender(message.from, true);
 			}
 		});
 
 		updater = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				Log.d(TAG, "Update spam list intent has been received");
 				// Wait for record updated is complete or you'll get empty record
 				stopManagingCursor(cursor);
 				cursor = dao.getSpamCursor();
 				startManagingCursor(cursor);
 				adapter.changeCursor(cursor);
 				list.postInvalidate();
 			}
 		};
 
 		importFromExistingMessages();
 	}
 
 	private void importFromExistingMessages() {
 		if (prefs.showImportActivity()) {
 			openImportActivity();
 			prefs.setShowImportActivity(false);
 		}
 	}
 
 	private void openImportActivity() {
 		startActivityForResult(new Intent(this, ImportActivity.class), ImportActivity.FIRST_IMPORT);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(updater, new IntentFilter(getResources().getString(R.string.update_action)));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(updater);
 		prefs.updateLastViewedTime();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		if (dao != null) {
 			dao.close();
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int code, int result, Intent intent) {
 		Log.d(TAG, "Activity has closed");
 	}
 
 	private void restoreMessage(int messageId) {
 		SmsModel message = dao.getMessage(messageId);
 		ContentValues values = new ContentValues();
 		values.put(Utils.MESSAGE_ADDRESS, message.from);
 		values.put(Utils.MESSAGE_BODY, message.body);
 		values.put(Utils.MESSAGE_READ, true);
 		values.put(Utils.MESSAGE_TYPE, Utils.MESSAGE_TYPE_SMS);
 		values.put(Utils.MESSAGE_DATE, message.sentAt);
 		getContentResolver().insert(Uri.parse(Utils.URI_INBOX), values);
 		getContentResolver().delete(Uri.parse(Utils.URI_THREADS + "-1"), null, null);
 		dao.deleteMessage(messageId);
 	}
 
 }
