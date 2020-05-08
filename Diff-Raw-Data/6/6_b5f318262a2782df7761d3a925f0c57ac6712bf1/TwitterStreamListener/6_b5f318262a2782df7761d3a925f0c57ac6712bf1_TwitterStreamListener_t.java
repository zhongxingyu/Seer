 package us.wmwm.tuxedo.services;
 
 import twitter4j.DirectMessage;
 import twitter4j.StallWarning;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.TwitterStream;
 import twitter4j.User;
 import twitter4j.UserList;
 import twitter4j.UserStreamListener;
 import us.wmwm.tuxedo.views.ChatViews;
 import us.wmwm.tuxedo.views.ChatViews.OnAction;
 import us.wmwm.tuxedo.views.TrashLayout;
 import us.wmwm.tuxedo.views.TwitterHead;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.WindowManager;
 
 public class TwitterStreamListener implements UserStreamListener {
 
 	AdvancedTwitterService service;
 	TwitterStream stream;
 	WindowManager windowManager;
 
 	Handler handler = new Handler();
 	private TrashLayout trashLayout;
 
 	public TwitterStreamListener(TwitterStream stream,
 			AdvancedTwitterService service) {
 		this.service = service;
 		this.stream = stream;
 		this.windowManager = (WindowManager) service
 				.getSystemService(Context.WINDOW_SERVICE);
 	}
 
 	@Override
 	public void onDeletionNotice(StatusDeletionNotice del) {
 		service.getApp().getSession().getStatusDAO()
 				.deleteStatus(del.getStatusId());
 		Intent i = new Intent();
 		i.setAction(AdvancedTwitterService.STATUS_DELETED);
 		i.putExtra("dn", del);
 		service.sendBroadcast(i);
 
 	}
 
 	@Override
 	public void onScrubGeo(long arg0, long arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStallWarning(StallWarning arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatus(final Status status) {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				final TwitterHead chatHead = new TwitterHead(service);
 				chatHead.update(status);
 				Log.i("PROFILE_IMAGE", status.getUser()
 						.getOriginalProfileImageURL());
 				final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
 						WindowManager.LayoutParams.WRAP_CONTENT,
 						WindowManager.LayoutParams.WRAP_CONTENT,
 						WindowManager.LayoutParams.TYPE_PHONE,
 						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
 						PixelFormat.RGBA_8888);
 
 				params.gravity = Gravity.NO_GRAVITY;
 				params.x = 0;
 				params.y = 100;
 				chatHead.setOnTouchListener(new View.OnTouchListener() {
 					private int initialX;
 					private int initialY;
 					private int initialTouchX;
 					private int initialTouchY;
 					private float lastRawX;
 					private float lastRawY;
 					private boolean moved;
 
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						switch (event.getAction()) {
 						case MotionEvent.ACTION_DOWN:
 							chatHead.startDrag();
 							moved = false;
 							showTrash();
 							initialX = params.x;
 							initialY = params.y;
 							initialTouchX = (int) event.getRawX();
 							initialTouchY = (int) event.getRawY();
 							return true;
 						case MotionEvent.ACTION_UP:
							if (checkTrash(event, chatHead))
                                return true;
                            chatHead.stopDrag();
 							int diffX = Math.abs(((int) event.getRawX())
 									- initialTouchX);
 							int diffY = Math.abs(((int) event.getRawY())
 									- initialTouchY);
 							ViewConfiguration config = ViewConfiguration
 									.get(service);
 							if (diffX < config.getScaledTouchSlop()
 									&& diffY < config.getScaledTouchSlop()) {
 								initialX = params.x;
 								initialY = params.y;
 								final ChatViews chatViews = new ChatViews(
 										service);
 								chatViews.setContext(service, status,
 										new OnAction() {
 											@Override
 											public void onBack() {
 												onSend();
 											}
 
 											@Override
 											public void onSend() {
 												windowManager
 														.removeView(chatViews);
 												params.x = initialX;
 												params.y = initialY;
 												windowManager.updateViewLayout(
 														chatHead, params);
 											}
 										});
 								final WindowManager.LayoutParams viewsParam = new WindowManager.LayoutParams(
 										WindowManager.LayoutParams.MATCH_PARENT,
 										windowManager.getDefaultDisplay()
 												.getHeight()
 												- chatHead.getHeight(),
 										WindowManager.LayoutParams.TYPE_PHONE,
 										0, PixelFormat.RGBA_8888);
 								viewsParam.gravity = Gravity.TOP | Gravity.LEFT;
 								viewsParam.x = 0;
 								viewsParam.y = chatHead.getHeight();
 								params.x = windowManager.getDefaultDisplay()
 										.getWidth() - chatHead.getWidth();
 								params.y = 0;
 								windowManager
 										.updateViewLayout(chatHead, params);
 								windowManager.addView(chatViews, viewsParam);
 							} else {
 								if (lastRawX > windowManager
 										.getDefaultDisplay().getWidth() / 2.0) {
 									params.x = windowManager
 											.getDefaultDisplay().getWidth()
 											- chatHead.getWidth();
 								} else {
 									params.x = 0;
 								}
 								windowManager
 										.updateViewLayout(chatHead, params);
 							}
 							return true;
 						case MotionEvent.ACTION_MOVE:
 							lastRawX = event.getRawX();
 							lastRawY = event.getRawY();
 							params.x = initialX
 									+ (int) (event.getRawX() - initialTouchX);
 							params.y = initialY
 									+ (int) (event.getRawY() - initialTouchY);
 							windowManager.updateViewLayout(chatHead, params);
 							moved = true;
 							return true;
 						}
 						return false;
 					}
 				});
 
 				windowManager.addView(chatHead, params);
 			}
 		});
 
 		try {
 			service.getApp().getSession().getStatusDAO()
 					.saveStatus(stream.getId(), status);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		Intent i = new Intent();
 		i.setAction(AdvancedTwitterService.STATUS_STREAMED);
 		i.putExtra("status", status);
 		service.sendBroadcast(i);
 		try {
 			service.updateStatusNotification(stream.getId(), status);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	private Boolean checkTrash(MotionEvent event, TwitterHead chatHead) {
 		Boolean isTrashed = false;
 		if (trashLayout.checkCollision(event)) {
 			windowManager.removeViewImmediate(chatHead);
 			isTrashed = true;
 		}
 		windowManager.removeViewImmediate(trashLayout);
 		return isTrashed;
 	}
 
 	private void showTrash() {
 		trashLayout = new TrashLayout(service);
 		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
 				WindowManager.LayoutParams.WRAP_CONTENT,
 				WindowManager.LayoutParams.WRAP_CONTENT,
 				WindowManager.LayoutParams.TYPE_PHONE,
 				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
 				PixelFormat.RGBA_8888);
 
 		params.gravity = Gravity.BOTTOM;
 		params.x = 0;
 		params.y = 0;
 
 		windowManager.addView(trashLayout, params);
 	}
 
 	public void onTrackLimitationNotice(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onException(Exception arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onBlock(User arg0, User arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onDeletionNotice(long arg0, long arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onDirectMessage(DirectMessage dm) {
 		// TODO Auto-generated method stub
 		service.getApp().getSession().getDirectMessageDAO()
 				.saveDirectMessage(dm);
 		Intent i = new Intent();
 		i.setAction(AdvancedTwitterService.DM_STREAMED);
 		i.putExtra("dm", dm);
 		service.sendBroadcast(i);
 
 	}
 
 	@Override
 	public void onFavorite(User arg0, User arg1, Status arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onFollow(User arg0, User arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onFriendList(long[] arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUnblock(User arg0, User arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUnfavorite(User arg0, User arg1, Status arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListCreation(User arg0, UserList arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListDeletion(User arg0, UserList arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListMemberAddition(User arg0, User arg1, UserList arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListMemberDeletion(User arg0, User arg1, UserList arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListSubscription(User arg0, User arg1, UserList arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListUnsubscription(User arg0, User arg1, UserList arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserListUpdate(User arg0, UserList arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUserProfileUpdate(User arg0) {
 		// TODO Auto-generated method stub
 
 	};
 
 }
