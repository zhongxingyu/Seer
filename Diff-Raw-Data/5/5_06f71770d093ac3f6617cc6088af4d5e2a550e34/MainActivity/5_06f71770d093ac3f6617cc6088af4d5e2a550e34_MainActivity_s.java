 package com.dj.antispam;
 
 import android.app.Activity;
 import android.content.*;
 import android.database.Cursor;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import com.dj.antispam.dao.SmsDao;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 public class MainActivity extends Activity {
 	private static final String TAG = MainActivity.class.getSimpleName();
 	private SmsDao dao;
 	private BroadcastReceiver updater;
 	private Cursor cursor;
 
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		dao = new SmsDao(this);
 		setContentView(R.layout.main);
 		final ListView list = (ListView)findViewById(R.id.listView);
 		cursor = dao.getSpamCursor();
 		startManagingCursor(cursor);
 		final CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.sms_item, cursor,
 				new String[] {"from", "body", "sentAt"},
 				new int[] {R.id.from, R.id.body, R.id.date})
 		{
 			@Override
 			public void setViewText(TextView v, String text) {
 				super.setViewText(v, convText(v, text));
 			}
 			private String convText(TextView v, String text) {
 				if (v.getId() == R.id.date) {
 					return DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(text)));
 				}
 				return text;
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
 			private boolean swiping = false;
 
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
 				// TODO Auto-generated method stub
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
 						float deltaX = event.getRawX() - downX;
 						velocityTracker.addMovement(event);
 						velocityTracker.computeCurrentVelocity(1000);
 						float velocityX = Math.abs(velocityTracker.getXVelocity());
 						float velocityY = Math.abs(velocityTracker.getYVelocity());
 						boolean dismiss = false;
 						boolean dismissRight = false;
 						if (Math.abs(deltaX) > list.getWidth() / 2) {
 							dismiss = true;
 							dismissRight = deltaX > 0;
 						} else if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity
 								&& velocityY < velocityX) {
 							dismiss = true;
 							dismissRight = velocityTracker.getXVelocity() > 0;
 						}
 						if (dismiss) {
 							// dismiss
 							final int position = downPosition;
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
 								}
 
 								@Override
 								public void onAnimationEnd(Animation animation) {
 									if (fDismissRight) {
 										onDeleteMessage(downPosition);
 									} else {
 										onRestoreMessage(downPosition);
 									}
 									updater.onReceive(getApplicationContext(), new Intent(getResources().getString(R.string.update_action)));
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
 				try {
 					int col = cur.getColumnIndex("_id");
 					return cur.getInt(col);
 				} finally {
 					cur.close();
 				}
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
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {}
 				stopManagingCursor(cursor);
 				cursor = dao.getSpamCursor();
 				startManagingCursor(cursor);
 				adapter.changeCursor(cursor);
 			}
 		};
 
 		importFromExistingMessages();
 	}
 
 	private void importFromExistingMessages() {
 		SharedPreferences prefs = getSharedPreferences("antispamImport", Context.MODE_PRIVATE);
 		int value = prefs.getInt("antispamImport", 0);
 		if (value == 0) {
 			SharedPreferences.Editor editor = prefs.edit();
 /*
 			editor.putInt("antispamImport", 1);
 			editor.commit();
 */
 			startActivityForResult(new Intent(this, ImportActivity.class), ImportActivity.FIRST_IMPORT);
 		}
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
 
	@Override
	protected void onActivityResult(int code, int result, Intent intent) {
		Log.d(TAG, "Activity has closed");
	}

 	private void restoreMessage(int messageId) {
 		SmsModel message = dao.getMessage(messageId);
 		ContentValues values = new ContentValues();
 		values.put("address", message.from);
 		values.put("body", message.body);
 		values.put("read", true);
 		values.put("date", message.sentAt);
 		getContentResolver().insert(Uri.parse(Utils.URI_INBOX), values);
 		dao.deleteMessage(messageId);
 	}
 
 }
