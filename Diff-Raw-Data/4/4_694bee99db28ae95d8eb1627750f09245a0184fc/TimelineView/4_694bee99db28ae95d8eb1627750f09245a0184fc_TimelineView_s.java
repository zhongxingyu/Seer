 package org.fukata.android.mytw;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.fukata.android.exandroid.loader.process.BaseRequest;
 import org.fukata.android.exandroid.loader.process.ProcessLoader;
 import org.fukata.android.exandroid.util.StringUtil;
 import org.fukata.android.mytw.TimelineActivity.LoadMode;
 import org.fukata.android.mytw.database.dto.TweetDto;
 import org.fukata.android.mytw.database.schema.TweetSchema.TweetType;
 import org.fukata.android.mytw.util.SettingUtil;
 import org.fukata.android.mytw.util.StringMatchUtils;
 import org.fukata.android.mytw.util.StringUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class TimelineView extends ListView implements View.OnClickListener, OnItemLongClickListener {
 	// 定期的に最新のツイートを取得するかどうかチェックする間隔
 	static final int UPDATE_TIMELINE_CHECK_INVERVAL = 10000;
 
 	private static final int NOTIFY_NEW_TWEET = 1;
 	
 	TimelineActivity parentActivity;
 	List<TimelineItem> items;
 	TimelineAdapter adapter;
 
 	Button more;
 	
 	int lastLoadCount;
 	String oldestStatusId;      //取得している一番古いtweetのstatusId
 	String latestStatusId;      //取得している一番新しいtweetのstatusId
 	long lastUpdateTimeline = System.currentTimeMillis();
 	Timer intervalUpdateTimer;
 
 	boolean isFirstLoad = true;
 	ItemDialog itemDialog;
 	NotificationManager notificationManager;
 	
 	public TimelineView(Context context, TimelineActivity activity) {
 		super(context);
 		parentActivity = activity;
 		items = new ArrayList<TimelineItem>();
 		adapter = newInstanceTimelineAdapter(context, items);
 		itemDialog = newInstanceItemDialog();
 		notificationManager = (NotificationManager)parentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
 		
         View footerView = activity.getLayoutInflater().inflate(R.layout.timeline_footer, null);
         more = (Button) footerView.findViewById(R.id.more);
         more.setOnClickListener(this);
         
         addFooterView(footerView);
         setOnItemLongClickListener(this);
         setAdapter(adapter);
 
         initTimeline();
         attachUpdateInterval();
 	}
 	
 	void initTimeline() {
 		adapter.clear();
 		List<TweetDto> tweets = parentActivity.tweetDao.findByType(getTweetType(), SettingUtil.getTimelineCount());
 		for (TweetDto tweet : tweets) {
 			TimelineItem item = generateTimelineItem(tweet);
 			adapter.add(item);
 		}
 		//キャッシュのtweetsの中での最新のstatusIdを取得する。
 		if (tweets.size() > 0) {
			this.oldestStatusId = tweets.get(0).statusId;
			this.latestStatusId = tweets.get(tweets.size() - 1).statusId;
 		}
 	}
 
 	TimelineItem generateTimelineItem(TweetDto tweet) {
 		TimelineItem item = new TimelineItem();
 		
 		item.setStatusId(tweet.statusId);
 		item.setStatus(tweet.status);
 		item.setUsername(tweet.username);
 		item.setUserId(tweet.userId);
 		item.setSource(tweet.source);
 		item.setCreatedAt(tweet.createdAt);
 		
 		return item;
 	}
 
 	TweetDto generateTweetDto(TimelineItem item) {
 		TweetDto dto = new TweetDto();
 		
 		dto.statusId = item.getStatusId();
 		dto.status = item.getStatus();
 		dto.username = item.getUsername();
 		dto.userId = item.getUserId();
 		dto.source = item.getSource();
 		dto.createdAt = item.getCreatedAt();
 		dto.tweetType = getTweetType();
 		
 		return dto;
 	}
 	
 	ItemDialog newInstanceItemDialog() {
 		return new ItemDialog(parentActivity);
 	}
 
 	void attachUpdateInterval() {
 		intervalUpdateTimer = new Timer();
 		TimerTask task = new TimerTask() {
 			@Override
 			public void run() {
 				long now = System.currentTimeMillis();
 				int interval = SettingUtil.getAutoInterval()*1000;
 				if (interval>0 && now>lastUpdateTimeline+interval) {
 					// 他に更新中の場合はエンキューを行わない。
 					if (parentActivity.timelineLoader.getLoaderQueue().size()==0) {
 						loadTimeline(LoadMode.NEW);
 					}
 				}
 			}
 		};
 		intervalUpdateTimer.schedule(task, UPDATE_TIMELINE_CHECK_INVERVAL, UPDATE_TIMELINE_CHECK_INVERVAL);
 	}
 
 	TimelineAdapter newInstanceTimelineAdapter(Context context, List<TimelineItem> items) {
 		return new TimelineAdapter(context, items);
 	}
 	
 	TweetType getTweetType() {
 		return TweetType.HOME;
 	}
 	
 	void loadTimeline(final LoadMode mode) {
 		preLoadTimeline(mode);
 		final List<TimelineItem> timeline = new ArrayList<TimelineItem>();
 		Runnable successCallback = generateSuccessCallback(timeline, mode);
 		processLoadTimeline(timeline, mode, successCallback);
 	}
 	
 	void preLoadTimeline(LoadMode mode) {
 		if (LoadMode.REFRESH==mode) {
 			more.setEnabled(false);
 			more.setText(R.string.retrieving);
 		} else if (LoadMode.NEW==mode) {
 		} else if (LoadMode.MORE==mode) {
 			more.setEnabled(false);
 			more.setText(R.string.retrieving);
 		}
 	}
 
 	void processLoadTimeline(final List<TimelineItem> timeline, final LoadMode mode, Runnable successCallback) {
 		parentActivity.timelineLoader.load(new BaseRequest(successCallback, null) {
 
 			@Override
 			public void processRequest(ProcessLoader loader) {
 				lastLoadCount = 0;
 				List<TimelineItem> list = null;
 				if (LoadMode.REFRESH==mode) {
 					list = getTimeline();
 					timeline.addAll(list);
 					// 1件でも取得できている場合のみリフレッシュ
 					if (list.size() > 0) {
 						refreshCache(timeline);
 					}
 				} else if (LoadMode.NEW==mode) {
 					list = getNewTimeline(latestStatusId);
 					lastLoadCount = list.size();
 					timeline.addAll(list);
 					addCache(list);
 				} else if (LoadMode.MORE==mode) {
 					list = getMoreTimeline(oldestStatusId);
 					timeline.addAll(list);
 					addCache(list);
 				}
 				super.processRequest(loader);
 			}
 			
 			void addCache(final List<TimelineItem> newLoadedItems) {
 				Log.d(getClass().getSimpleName(), "addCache");
 				List<TweetDto> tweets = new ArrayList<TweetDto>();
 				int count = SettingUtil.getTimelineCount();
 				int len = timeline.size() > count ? count : timeline.size();
 				for (int i=0; i<len; i++) {
 					TimelineItem item = timeline.get(i);
 					tweets.add( generateTweetDto(item) );
 				}
 				parentActivity.tweetDao.addTweets(tweets, getTweetType());
 			}
 
 			void refreshCache(final List<TimelineItem> timeline) {
 				Log.d(getClass().getSimpleName(), "refreshCache");
 				List<TweetDto> tweets = new ArrayList<TweetDto>();
 				int count = SettingUtil.getTimelineCount();
 				int len = timeline.size() > count ? count : timeline.size();
 				for (int i=0; i<len; i++) {
 					TimelineItem item = timeline.get(i);
 					tweets.add( generateTweetDto(item) );
 				}
 				parentActivity.tweetDao.refreshTweets(tweets, getTweetType());
 			}
 
 		});
 	}
 	
 	List<TimelineItem> getMoreTimeline(String lastStatuseId) {
 		return parentActivity.twitter.getMoreHomeTimeline(lastStatuseId);
 	}
 
 	List<TimelineItem> getNewTimeline(String latestStatuseId) {
 		return parentActivity.twitter.getNewHomeTimeline(latestStatuseId);
 	}
 
 	List<TimelineItem> getTimeline() {
 		return parentActivity.twitter.getHomeTimeline();
 	}
 	
 	Runnable generateSuccessCallback(final List<TimelineItem> timeline, final LoadMode mode) {
 		Log.d(getClass().getSimpleName(), "generateSuccessCallback");
 		Runnable callback = new Runnable() {
 			@Override
 			public void run() {
 				// 現在表示されている最上のツイート位置
 				int firstItemPosition = getFirstVisiblePosition();
 				int firstItemTop = getChildCount()==0 || getChildAt(0)==null ? 0 : getChildAt(0).getTop();
 				Log.d(getClass().getSimpleName(), "firstItemPosition="+firstItemPosition);
 				Log.d(getClass().getSimpleName(), "firstItemTop="+firstItemTop);
 				processUpdateTimeline(mode, timeline);
 				processFocusItem(mode,firstItemPosition,firstItemTop);
 				processNotification(mode);
 				lastUpdateTimeline = System.currentTimeMillis();
 			};
 		};
 		return callback;
 	}
 
 	void processUpdateTimeline(LoadMode mode, final List<TimelineItem> timeline) {
 		more.setText(R.string.more);
 		more.setEnabled(true);
 		if (timeline.size()>0) {
 			if (LoadMode.REFRESH==mode) {
 				latestStatusId = timeline.get(0).getStatusId();
 				oldestStatusId = timeline.get(timeline.size()-1).getStatusId();
 				adapter.clear();
 				for (TimelineItem item : timeline) {
 					adapter.add(item);
 				}
 				if (hasWindowFocus()) {
 					Toast.makeText(parentActivity.getApplicationContext(), R.string.update_successful, Toast.LENGTH_LONG).show();
 				}
 			} else if (LoadMode.NEW==mode) {
 				int insertAt = 0;
 				for (TimelineItem item : timeline) {
 					if (!StringUtil.equals(latestStatusId, item.getStatusId())) {
 						adapter.insert(item, insertAt);
 						insertAt++;
 					}
 				}
 				latestStatusId = timeline.get(0).getStatusId();
 			} else if (LoadMode.MORE==mode) {
 				for (TimelineItem item : timeline) {
 					if (!StringUtil.equals(oldestStatusId, item.getStatusId())) {
 						adapter.add(item);
 					}
 				}
 				oldestStatusId = timeline.get(timeline.size()-1).getStatusId();
 				if (hasWindowFocus()) {
 					Toast.makeText(parentActivity.getApplicationContext(), R.string.update_successful, Toast.LENGTH_LONG).show();
 				}
 			}
 		}
 	}
 	
 	void processNotification(LoadMode mode) {
 		if (hasWindowFocus() || !SettingUtil.isNotificationEnabled()) {
 			return;
 		}
 		
 		if (mode == LoadMode.NEW && lastLoadCount>0) {
 			notificationNewTweet();
 		}
 	}
 	
 	void notificationNewTweet() {
 		String ticker = parentActivity.getString(R.string.notify_new_tweet, lastLoadCount);
 		Intent intent = new Intent(parentActivity, TimelineActivity.class);
 		intent.setAction(Intent.ACTION_VIEW);
 		PendingIntent contentIntent = PendingIntent.getActivity(parentActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		Notification notification = new Notification(android.R.drawable.ic_menu_add, ticker, System.currentTimeMillis());
 		notification.setLatestEventInfo(parentActivity.getApplicationContext(), parentActivity.getString(R.string.app_name), ticker, contentIntent);
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		notificationManager.notify(NOTIFY_NEW_TWEET, notification);
 	}
 
 	void processFocusItem(LoadMode mode, int firstItemPosition, int firstItemTop) {
 		if (mode == LoadMode.NEW) {
 			//新しい選択位置を設定する。
 			setSelectionFromTop(firstItemPosition+lastLoadCount, firstItemTop);
 		}
 	}
 
 	void doResume() {
 		Log.d(getClass().getSimpleName(), "doResume");
 		boolean noCachedTimeline = this.latestStatusId == null; //キャッシュが無い場合にtrueとなる
 		if (isFirstLoad && noCachedTimeline) {
 			loadTimeline(LoadMode.REFRESH);
 		} else {
 			loadTimeline(LoadMode.NEW);
 		}
 		isFirstLoad = false;
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v.getId()==R.id.more) {
 			loadTimeline(LoadMode.MORE);
 		}
 	}
 	
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
 		if (items.size()<=position) {
 			return false;
 		}
 
 		TimelineItem item = items.get(position);
 		if (item==null) {
 			return false;
 		}
 
 		itemDialog.show(item);
 		return false;
 	}
 	
 //	CharSequence getTitle() {
 //		return parentActivity.getString(R.string.title_home);
 //	}
 	
 	void postRetweet(final TimelineItem item) {
 		Runnable successCallback = new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(parentActivity.getApplicationContext(), R.string.retweet_successful, Toast.LENGTH_LONG).show();
 				loadTimeline(LoadMode.NEW);
 			}
 		};
 		parentActivity.timelineLoader.load(new BaseRequest(successCallback, null) {
 			@Override
 			public void processRequest(ProcessLoader loader) {
 				parentActivity.twitter.postReTweet(item.getStatusId());
 				super.processRequest(loader);
 			}
 		});
 	}
 	
 	void postFavorites(final TimelineItem item) {
 		Runnable successCallback = new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(parentActivity.getApplicationContext(), R.string.favorites_successful, Toast.LENGTH_LONG).show();
 			}
 		};
 		parentActivity.timelineLoader.load(new BaseRequest(successCallback, null) {
 			@Override
 			public void processRequest(ProcessLoader loader) {
 				parentActivity.twitter.postFavorites(item.getStatusId());
 				super.processRequest(loader);
 			}
 		});
 	}
 
 	void deleteTweet(final TimelineItem item) {
 		//FIXME 未実装
 	}
 	
 	class ItemDialog extends AlertDialog.Builder {
 		ListView optionsView;
 		ArrayAdapter<String> optionsAdapter;
 		Activity activity;
 		List<String> urls;
 		static final int OPTION_RETWEET = 0;
 		static final int OPTION_RETWEET_WITH_COMMENT = 1;
 		static final int OPTION_REPLY = 2;
 		static final int OPTION_FAVORITES = 3;
 		static final int OPTION_URLS = 4;
 
 		final String[] options = {
 			parentActivity.getString(R.string.retweet), 
 			parentActivity.getString(R.string.retweet_with_comment),
 			parentActivity.getString(R.string.reply),
 			parentActivity.getString(R.string.favorites),
 		};
 
 		public ItemDialog(Activity activity) {
 			super(activity);
 			this.activity = activity;
 			setTitle(R.string.options);
 		}
 		
 		public void show(final TimelineItem item) {
 			String[] fixedOptions = options;
 			String source = item.getStatus();
 			urls = StringMatchUtils.getUrls(source);
 			if (urls.size() > 0) {
 				List<String> opList = new ArrayList<String>();
 				for (String elm : options) {
 					opList.add(elm);
 				}
 				opList.add(parentActivity.getString(R.string.urls));
 				fixedOptions = opList.toArray(new String[0]);
 			}
 			setItems(fixedOptions, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (OPTION_RETWEET==which) {
 						postRetweet(item);
 					} else if (OPTION_RETWEET_WITH_COMMENT==which) {
 						Intent intent = new Intent(Intent.ACTION_SEND);
 						intent.setClass(activity, UpdateStatusActivity.class);
 						intent.putExtra(Intent.EXTRA_TEXT, " RT @"+item.getUsername()+": "+item.getStatus());
 						intent.putExtra(MyTwitterApp.INTENT_EXTRA_SELECTION, MyTwitterApp.INTENT_EXTRA_SELECTION_HEAD);
 						parentActivity.startActivity(intent);
 					} else if (OPTION_REPLY==which) {
 						Intent intent = new Intent(Intent.ACTION_SEND);
 						intent.setClass(activity, UpdateStatusActivity.class);
 						intent.putExtra(Intent.EXTRA_TEXT, "@"+item.getUsername()+" ");
 						intent.putExtra(MyTwitterApp.INTENT_EXTRA_SELECTION, MyTwitterApp.INTENT_EXTRA_SELECTION_END);
 						parentActivity.startActivity(intent);
 					} else if (OPTION_FAVORITES == which) {
 						postFavorites(item);
 					} else if (OPTION_URLS == which) {
 						List<String> urls = ItemDialog.this.urls;
 						if (urls.size() == 1) {
 							//URLが一つだけなら、そのまま外部ブラウザで開く
 							openOnExternalWebBrowser(ItemDialog.this.urls.get(0));
 						} else if (urls.size() > 1) {
 							//URLが複数存在するなら、更に選択肢を表示する。
 							String[] urlMenus = new String[urls.size()];
 							for (int i = 0; i < urls.size(); i++) {
 								urlMenus[i] = parentActivity.getString(R.string.jump_to, StringUtils.strimwidth(urls.get(i), 40, "..."));
 							}
 							setItems(urlMenus, new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									openOnExternalWebBrowser(ItemDialog.this.urls.get(which));
 								}
 							});
 							create().show();
 						}
 					}
 				}
 
 				/**
 				 * 外部ブラウザでURLを開く。
 				 */
 				private void openOnExternalWebBrowser(String url) {
 					try {
 						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 						parentActivity.startActivity(intent);
 					} catch (Exception e) {
 						Toast.makeText(parentActivity.getApplicationContext(), R.string.update_unsuccessful, Toast.LENGTH_LONG).show();
 					}
 				}
 			});
 			create().show();
 		}
 	}
 }
