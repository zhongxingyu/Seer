 /**
  * 
  */
 package com.starbug1.android.mudanews;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.os.Binder;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.util.Xml;
 
 import com.starbug1.android.mudanews.data.DatabaseHelper;
 import com.starbug1.android.mudanews.data.NewsListItem;
 import com.starbug1.android.mudanews.utils.FileDownloader;
 import com.starbug1.android.mudanews.utils.InternetStatus;
 
 /**
  * @author smeghead
  *
  */
 public class FetchFeedService extends Service {
 	public static final String ACTION = "mudanews fetch feed Service";
 	static final String TAG = "FetchFeedService";
 	private boolean isRunning = false;
 	SharedPreferences sharedPreferences_ = null;
	
 
 	private enum Feeds {
 		labaq("らばQ", "http://labaq.com/index.rdf"),
 		itaNews("痛いニュース", "http://blog.livedoor.jp/dqnplus/index.rdf"),
 		gigazine("GIGAZINE", "http://gigazine.net/news/rss_2.0/");
 
 		public String name = "";
 		public String url = "";
 		
 		private Feeds(String name, String url) {
 			this.name = name;
 			this.url = url;
 		}
 	}
 	
 	@Override
 	public void onCreate() {
 		Log.d("FetchFeedService", "onCreate");
 		
 		sharedPreferences_ = PreferenceManager.getDefaultSharedPreferences(this);
 		int clowlIntervals = Integer.parseInt(sharedPreferences_.getString("clowl_intervals", "15"));
 		if (clowlIntervals != 0) {
 			AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 			Intent alarmIntent = new Intent(this, AlarmReceiver.class);
 			PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
 			GregorianCalendar calendar = new GregorianCalendar();
 			alarmManager.setInexactRepeating(
 					AlarmManager.RTC_WAKEUP,
 					calendar.getTimeInMillis() + 1000 * 60 * 5,
 					1000 * 60 * clowlIntervals,
 					sender); 
 		}
 		super.onCreate();
 	}
 
 	public void onStart(Intent intent, int startId) {
 		Log.d("FetchFeedService", "onStart");
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				fetchFeeds();
 			}
 		}).start();
 
 		super.onStart(intent, startId);
 	}
 
 	private void fetchFeeds() {
 		if (isRunning) return;
 		
 		if (!isRunning) {
 			try {
 				isRunning = true;
 				
 				Log.d("FetchFeedService", "fetch feeds start.....");
 				int count = updateFeeds();
 
 				if (count > 0) {
 					NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 					Notification notification = new Notification(R.drawable.notify,
 							"ニュースが入りました", System.currentTimeMillis());
 					if (sharedPreferences_.getBoolean("pref_notify_sound", true)) {
 						notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI;
 					}
 
 					Intent intent = new Intent(FetchFeedService.this,
 							MudanewsActivity.class);
 					PendingIntent contentIntent = PendingIntent.getActivity(
 							FetchFeedService.this, 0, intent, 0);
 					notification.setLatestEventInfo(getApplicationContext(), "無駄新聞",
 							String.valueOf(count) + "件の新着記事を追加しました", contentIntent);
 					manager.notify(R.string.app_name, notification);
 				}
 			} finally {
 				isRunning = false;
 			}
 		}
 	}
 	
 	private class FeedTask implements Callable<Integer> {
         
 		public String name = "";
 		public String url = "";
         
         public FeedTask(String name, String url) {
         	this.name = name;
         	this.url = url;
         }
         
         public Integer call() throws Exception {
 			return Integer.valueOf(registerNews(parseXml(this.name, this.url)));
         }
         
     }
 
 	private static boolean isWorking = false;
 	
 	public int updateFeeds() {
 		if (!InternetStatus.isConnected(getApplicationContext())) {
 			Log.i("FetchFeedService", "no connection.");
 			return 0;
 		}
 		if (isWorking) {
 			Log.i("FetchFeedService", "already working another task.");
 			return 0;
 		}
 		int count = 0;
 		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(10);
         final ThreadPoolExecutor exec = new ThreadPoolExecutor(5, 5, 0,
                 TimeUnit.MILLISECONDS, queue);
         
         // 結果を受け取るリスト
         final List<Future<Integer>> result = new ArrayList<Future<Integer>>();
 
         try {
     		isWorking = true;
 	        // キューへの順次追加
 	        try {
 	            for (Feeds f : Feeds.values()) {
 	                if (queue.remainingCapacity() > 0) {
 	                	Log.d("FetchFeedService", "register feedtask:" + f.name);
 	                    result.add(exec.submit(new FeedTask(f.name, f.url)));
 	                } else {
 	                	Log.d("FetchFeedService", "waiting:" + f.name);
 	                    Thread.sleep(1000);
 	                }
 	            }
 	        } catch (InterruptedException e) {
 	        	Log.e("FetchFeedService", "queue error " + e.getMessage());
 	            throw new RuntimeException(e);
 	        } finally {
 	            exec.shutdown();
 	        }
 	        
 	        // 結果表示
 	        for (int i = 0; i < result.size(); i++) {
 	            try {
                 	Log.d("FetchFeedService", "retrieve result");
 	                count += result.get(i).get();
 	            } catch (InterruptedException e) {
 		        	Log.e("FetchFeedService", "queue error " + e.getMessage());
 	                throw new RuntimeException(e);
 	            } catch (ExecutionException e) {
 		        	Log.e("FetchFeedService", "queue error " + e.getMessage());
 	                throw new RuntimeException(e);
 	            }
 	        }
 			Log.d("FetchFeedService", "fetched");
 		} catch(Exception e) {
 			Log.e("NewsParserTask", e.toString());
 			throw new AppException("failed to background task.", e);
 		} finally {
 			isWorking = false;
 		}
 		return count;
 	}
 
 	private List<NewsListItem> parseXml(String source, String urlString) {
 		XmlPullParser parser = Xml.newPullParser();
 		List<NewsListItem> list = new ArrayList<NewsListItem>(20);
 		
 		InputStream is = null;
 		try {
 			URL url = new URL(urlString);
 			URLConnection conn = url.openConnection();
 			conn.setConnectTimeout(1000 * 5);
 			is = conn.getInputStream();
 
 			parser.setInput(is, null);
 			int eventType = parser.getEventType();
 			NewsListItem currentItem = null;
 			DOC: while (eventType != XmlPullParser.END_DOCUMENT) {
 				String tag = null;
 				switch (eventType) {
 					case XmlPullParser.START_TAG:
 						tag = parser.getName();
 						if (tag.equals("item")) {
 							currentItem = new NewsListItem();
 							currentItem.setSource(source);
 						} else if (currentItem != null) {
 							if (tag.equals("title")) {
 								currentItem.setTitle(parser.nextText());
 							} else if (tag.equals("description")) {
 								currentItem.setDescription(parser.nextText());
 							} else if (tag.equals("link")) {
 								currentItem.setLink(parser.nextText());
 							} else if (tag.equals("subject")) {
 								currentItem.setCategory(parser.nextText());
 							} else if (tag.equals("encoded")) {
 								String imageUrl = pickupUrl(parser.nextText());
 								currentItem.setImageUrl(imageUrl);
 							} else if (tag.equals("date")) {
 								String dateExp = parser.nextText();
 								currentItem.setPublishedAt(parseDate(dateExp));
 							}
 						}
 						break;
 					case XmlPullParser.END_TAG:
 						tag = parser.getName();
 						if (tag.equals("item")) {
 							if (currentItem.getTitle().toString().indexOf("［PR］") == -1
 									&& currentItem.getTitle().toString().indexOf("PR:") == -1
 									&& currentItem.getTitle().toString().indexOf("ヘッドラインニュース") == -1) {
 								list.add(currentItem);
 								if (list.size() > 10) {
 									break DOC;
 								}
 							}
 						}
 						break;
 				}
 				eventType = parser.next();
 			}
 		} catch (Exception e) {
 			Log.e("NewsParserTask", e.getMessage());
 			//処理は継続する
 		} finally {
 			try {
 				if (is != null) is.close();
 			} catch (Exception e) {}
 		}
 		return list;
 	}
 	
 	private String pickupUrl(String src) {
 		Pattern p = Pattern.compile("<img.*src=\"([^\"]*)\"");
 		Matcher m = p.matcher(src);
 		if (!m.find()) {
 			return "";
 		}
 		return m.group(1);
 	}
 	
 	private long parseDate(String src) {
 		Date d = null;
 		try {
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 			d = format.parse(src);
 		} catch (Exception e) {
 			Log.e("FetchFeedService", e.getMessage());
 		}
 		if (d != null) {
 			return d.getTime();
 		}
 		try {
 			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
 			d = format.parse(src);
 		} catch (Exception e) {
 			Log.e("FetchFeedService", e.getMessage());
 		}
 		if (d != null) {
 			return d.getTime();
 		}
 		return 0;
 	}
 
 	private NewsListItem fetchImage(NewsListItem item) {
 			String imageUrl = item.getImageUrl();
 			if (imageUrl == null || imageUrl.length() == 0) {
 				try {
 					//GIGAZINE はfeedにimageのURLが無いので直に取得しにいく。
 					String content = FileDownloader.download(item.getLink());
 					Pattern p = Pattern.compile("class=\"preface\"(.*)$", Pattern.DOTALL);
 					Matcher m = p.matcher(content);
 					if (!m.find()) {
 						return item;
 					}
 					String mainPart = m.group(1);
 					p = Pattern.compile("<img.*?src=\"([^\"]*)\"", Pattern.MULTILINE);
 					m = p.matcher(mainPart);
 					if (!m.find()) {
 						return item;
 					}
 					imageUrl = m.group(1);
 				} catch (AppException e) {
 					Log.e("FetchFeedService", "failed to download content.");
 					return item;
 				}
 			}
 			URL url;
 			InputStream is = null;
 			try {
 				url = new URL(imageUrl);
 				is = url.openConnection().getInputStream();
 				Bitmap image = BitmapFactory.decodeStream(is);
 				
 				int heightOrg = image.getHeight(), widthOrg = image.getWidth();
 				int height = 0, width = 0;
 				int x = 0, y = 0;
 
 				height = width = Math.min(widthOrg, heightOrg);
 				
 				if (heightOrg > widthOrg) {
 					// 縦長
 					y = Math.abs(heightOrg - widthOrg) /  2;
 				} else {
 					//横長
 					x = Math.abs(heightOrg - widthOrg) /  2;
 				}
 				
 		        Matrix matrix = new Matrix(); 
 		        matrix.postScale(160f / width, 160f / height);
 		        Bitmap scaledBitmap = Bitmap.createBitmap(
 		        		image, 
 		        		x, 
 		        		y, 
 		        		width, 
 		        		height, 
 		        		matrix,
 		        		true);
 		        ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
 		        item.setImage(stream.toByteArray());
 			} catch (MalformedURLException e) {
 				Log.e("FetchFeedService", "[MalformedURLException] failed to get image." + e.getMessage() + " " + imageUrl);
 			} catch (IOException e) {
 				Log.e("FetchFeedService", "[IOException] failed to get image." + e.getMessage() + " " + imageUrl);
 			} catch (Exception e) {
 				Log.e("FetchFeedService", "[Exception] failed to get image." + e.getMessage() + " " + imageUrl);
 			} finally {
 				try {
 					if (is != null) is.close();
 				} catch (Exception e) {}
 			}
 			return item;
 	}
 	
 	private int registerNews(List<NewsListItem> list) {
 		int registerCount = 0;
 		DatabaseHelper helper = new DatabaseHelper(this);
		SQLiteDatabase db = null;
		synchronized (Lock.obj) {
			db = helper.getWritableDatabase();
		}
 		Date now = new Date();
 //		db.execSQL("delete from feeds");
 //		db.execSQL("delete from images");
 		
 		Cursor c = null;
 		try {
 			for (NewsListItem item : list) {
 				c = db.rawQuery("select id from feeds where link = ?", new String[]{item.getLink()});
 				int count = c.getCount();
 				c.close(); c = null;
 				if (count > 0) continue; //同じ リンクURLのエントリがあったら、取り込まない。
 				
 				item = fetchImage(item);
 
 				synchronized (Lock.obj) {
 					ContentValues values = new ContentValues();
 			        values.put("source", item.getSource());
 			        values.put("title", item.getTitle());
 			        values.put("link", item.getLink());
 			        values.put("description", item.getDescription());
 			        values.put("category", item.getCategory());
 			        values.put("published_at", item.getPublishedAt());
 			        values.put("created_at", now.getTime());
 			        long id = db.insert("feeds", null, values);
 
 			        registerCount++;
 
 			        if (item.getImage() == null) {
 			        	continue;
 			        }
 			        values = new ContentValues();
 			        values.put("feed_id", id);
 			        values.put("image", item.getImage());
 			        values.put("created_at", now.getTime());
 			        db.insert("images", null, values);
 				}
 			}
 			db.close();
 			return registerCount;
 		} finally {
 			if (c != null) c.close();
 			if (db != null && db.isOpen()) db.close();
 		}
 	}
 
     public class FetchFeedServiceLocalBinder extends Binder {
         //サービスの取得
         FetchFeedService getService() {
             return FetchFeedService.this;
         }
     }
     //Binderの生成
     private final IBinder binder_ = new FetchFeedServiceLocalBinder();
  
     @Override
     public IBinder onBind(Intent intent) {
         Log.i(TAG, "onBind" + ": " + intent);
         return binder_;
     }
  
     @Override
     public void onRebind(Intent intent){
         Log.i(TAG, "onRebind" + ": " + intent);
     }
  
     @Override
     public boolean onUnbind(Intent intent){
         Log.i(TAG, "onUnbind" + ": " + intent);
  
         //onUnbindをreturn trueでoverrideすると次回バインド時にonRebildが呼ばれる
         return true;
     }
 }
 
 class Lock {
 	static Object obj = new Object();
 }
