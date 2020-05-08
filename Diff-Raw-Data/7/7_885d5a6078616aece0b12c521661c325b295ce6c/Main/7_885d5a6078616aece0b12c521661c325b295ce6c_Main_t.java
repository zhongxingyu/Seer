 package com.macaroon.piztor;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.baidu.location.BDLocation;
 import com.baidu.location.BDLocationListener;
 import com.baidu.location.LocationClient;
 import com.baidu.location.LocationClientOption;
 import com.baidu.mapapi.map.ItemizedOverlay;
 import com.baidu.mapapi.map.LocationData;
 import com.baidu.mapapi.map.MapView;
 import com.baidu.mapapi.map.OverlayItem;
 import com.baidu.mapapi.map.PopupClickListener;
 import com.baidu.mapapi.map.PopupOverlay;
 import com.baidu.platform.comapi.basestruct.GeoPoint;
 
 import com.macaroon.piztor.BMapUtil;
 
 public class Main extends PiztorAct {
 	final static int SearchButtonPress = 1;
 	final static int FocuseButtonPress = 3;
 	final static int SuccessFetch = 4;
 	final static int FailedFetch = 5;
 	final static int Fetch = 6;
 	final static int mapViewtouched = 7;
 
 	MapMaker mapMaker = null;
 	MapView mMapView;
 
 	/**
 	 * Locating component
 	 */
 	LocationClient mLocClient;
 	LocationData locData = null;
 	public MyLocationListener myListener = new MyLocationListener();
 
 	ImageButton btnSearch, btnFetch, btnFocus, btnSettings;
 	//Timer autodate;
 	MapInfo mapInfo;
 	/*
 	 * @SuppressLint("HandlerLeak") Handler fromGPS = new Handler() {
 	 * 
 	 * @Override public void handleMessage(Message m) { if (m.what != 0) {
 	 * Location l = (Location) m.obj; if (l == null)
 	 * System.out.println("fuck!!!"); else { ReqUpdate r = new
 	 * ReqUpdate(Infomation.token, Infomation.username, l.getLatitude(),
 	 * l.getLongitude(), System.currentTimeMillis(), 1000);
 	 * AppMgr.transam.send(r); } } } };
 	 */
 
 	@SuppressLint("HandlerLeak")
 	Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message m) {
 			switch (m.what) {
 			case 1:// 上传自己信息成功or失败
 				ResUpdate update = (ResUpdate) m.obj;
 				if (update.s == 0)
 					System.out.println("update success");
 				else {
 					System.out.println("update failed");
 					actMgr.trigger(AppMgr.errorToken);
 				}
 				break;
 			case 2:// 得到别人的信息
 				ResLocation location = (ResLocation) m.obj;
 				if (location.s == 0) {
 					mapInfo.clear();
 					for (Rlocation i : location.l) {
 						System.out.println(i.i + " : " + i.lat + " " + i.lot);
 						UserInfo info = new UserInfo(i.i);
 						info.setLocation(i.lat, i.lot);
 						mapInfo.addUserInfo(info);
 					}
 					actMgr.trigger(SuccessFetch);
 				} else {
 					System.out.println("resquest for location failed!");
 					actMgr.trigger(AppMgr.errorToken);
 				}
 				break;
 			case 3:// 得到用户信息
 				ResUserinfo r = (ResUserinfo) m.obj;
 				if (r.s == 0) {
 					System.out.println("id : " + r.uid + " sex :  " + r.sex
 							+ " group : " + r.gid);
 					if (r.uid == Infomation.myInfo.uid) {
 						Infomation.myInfo.gid = r.gid;
 						try {
 							//autodate.schedule(new AutoUpdate(), 0, 5000);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					} else {
 						UserInfo user = mapInfo.getUserInfo(r.uid);
 						if (user != null)
 							user.setInfo(r.gid, r.sex);
 						else
 							System.out.println("fuck!!!!");
 					}
 					flushMap();
 				} else {
 					System.out.println("reqest for userInfo must be wrong!!!");
 					actMgr.trigger(AppMgr.errorToken);
 				}
 				break;
 			case 4:// 登出
 				Toast toast = Toast.makeText(getApplicationContext(),
 						"logout failed", Toast.LENGTH_LONG);
 				toast.show();
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	String cause(int t) {
 		switch (t) {
 		case SearchButtonPress:
 			return "Search Button Press";
 		case Fetch:
 			return "Fetch ";
 		case FocuseButtonPress:
 			return "Focuse Button Press";
 		case SuccessFetch:
 			return "Success Fetch";
 		case FailedFetch:
 			return "Failed Fetch";
 		default:
 			return "Fuck!!!";
 		}
 	}
 
 	// TODO flush map view
 	void flushMap() {
 		if (mapMaker != null)
 			mapMaker.UpdateMap(AppMgr.mapInfo);
 	}
 
 	public class MyLocationListener implements BDLocationListener {
 		@Override
 		public void onReceiveLocation(BDLocation location) {
 			Log.d("GPS", "Gotten");
 			if (location == null) {
 				return;
 			}
 
 			locData.latitude = location.getLatitude();
 			locData.longitude = location.getLongitude();
 			locData.accuracy = location.getRadius();
 			locData.direction = location.getDerect();
 
 			mapMaker.UpdateLocationOverlay(locData, false);
 			if (Infomation.token != null)
 				AppMgr.transam.send(new ReqUpdate(Infomation.token, Infomation.username, locData.latitude, locData.longitude, System.currentTimeMillis(), 2000));
 		}
 
 		@Override
 		public void onReceivePoi(BDLocation poiLocation) {
 			if (poiLocation == null) {
 				return;
 			}
 		}
 	}
 
 	class StartStatus extends ActStatus {
 
 		@Override
 		void enter(int e) {
 			System.out.println("enter start status!!!!");
 			if (e == ActMgr.Create) {
 				System.out.println(Infomation.token + "  "
 						+ Infomation.username + "   " + Infomation.myInfo.uid);
 				AppMgr.transam.send(new ReqUserinfo(Infomation.token,
 						Infomation.username, Infomation.myInfo.uid, System
 								.currentTimeMillis(), 5000));
 				// TODO flush mapinfo.myinfo
 			}
 
 			if (e == Fetch) {
 				requesLocation(Infomation.myInfo.gid);
 			}
 			if (e == SuccessFetch)
 				flushMap();
 		}
 
 		@Override
 		void leave(int e) {
 			System.out.println("leave start status!!!! because" + cause(e));
 		}
 
 	}
 
 	class FetchStatus extends ActStatus {
 
 		@Override
 		void enter(int e) {
 			System.out.println("enter Fetch status!!!!");
 			if (e == Fetch) {
 				requesLocation(Infomation.myInfo.gid);
 			}
 			if (e == SuccessFetch) {
 				flushMap();
 			}
 		}
 
 		@Override
 		void leave(int e) {
 			System.out.println("leave fetch status!!!! because" + cause(e));
 		}
 
 	}
 
 	class FocusStatus extends ActStatus {
 
 		@Override
 		void enter(int e) {
 			// TODO
 			switch (e) {
 			case Fetch:
 				requesLocation(Infomation.myInfo.gid);
 				break;
 			case FocuseButtonPress:
				if( locData != null) {
					mapMaker.UpdateLocationOverlay(locData, true);
				} else mapMaker.InitMap();
 				break;
 			case SuccessFetch:
 				flushMap();
 				break;
 			default:
 				break;
 			}
 			System.out.println("enter focus status!!!!");
 		}
 
 		@Override
 		void leave(int e) {
 			// TODO leave focus
 			System.out.println("leave focus status!!!! because" + cause(e));
 		}
 
 	}
 
 	void requesLocation(int gid) {
 		ReqLocation r = new ReqLocation(Infomation.token, Infomation.username,
 				gid, System.currentTimeMillis(), 2000);
 		System.out.println("get others infomation!!!");
 		AppMgr.transam.send(r);
 	}
 
 	class AutoUpdate extends TimerTask {
 		@Override
 		public void run() {
 			actMgr.trigger(Main.Fetch);
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		id = "Main";
 		super.onCreate(savedInstanceState);
 		mapInfo = AppMgr.mapInfo;
 		ActStatus[] r = new ActStatus[3];
 		ActStatus startStatus = r[0] = new StartStatus();
 		ActStatus fetchStatus = r[1] = new FetchStatus();
 		ActStatus focusStatus = r[2] = new FocusStatus();
 		AppMgr.transam.setHandler(handler);
 		actMgr = new ActMgr(this, startStatus, r);
 		actMgr.add(startStatus, FocuseButtonPress, focusStatus);
 		actMgr.add(startStatus, Fetch, fetchStatus);
 		actMgr.add(startStatus, SuccessFetch, startStatus);
 		actMgr.add(startStatus, Fetch, startStatus);
 		actMgr.add(fetchStatus, Fetch, startStatus);
 		actMgr.add(fetchStatus, FailedFetch, startStatus);
 		actMgr.add(fetchStatus, SuccessFetch, startStatus);
 		actMgr.add(focusStatus, FocuseButtonPress, startStatus);
 		actMgr.add(focusStatus, mapViewtouched, startStatus);
 		actMgr.add(focusStatus, SuccessFetch, focusStatus);
 		actMgr.add(focusStatus, Fetch, focusStatus);
 		//autodate = new Timer();
 		flushMap();
 		setContentView(R.layout.activity_main);
 		mMapView = (MapView) findViewById(R.id.bmapView);
 		mapMaker = new MapMaker(mMapView, getApplicationContext());
 		mapMaker.InitMap();
 		mLocClient = new LocationClient(this);
 		locData = new LocationData();
		locData.latitude = 31.032247;
		locData.longitude = 121.445937;
 		mLocClient.registerLocationListener(myListener);
 		LocationClientOption option = new LocationClientOption();
 		option.setOpenGps(true);
 		option.setCoorType("bd09ll");
 		option.setScanSpan(5000);
 		mLocClient.setLocOption(option);
 		mLocClient.start();
 		mapMaker.UpdateLocationOverlay(locData, false);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		btnFetch = (ImageButton) findViewById(R.id.footbar_btn_fetch);
 		btnFocus = (ImageButton) findViewById(R.id.footbar_btn_focus);
 		btnSearch = (ImageButton) findViewById(R.id.footbar_btn_search);
 		btnSettings = (ImageButton) findViewById(R.id.footbar_btn_settings);
 		btnFetch.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				actMgr.trigger(Fetch);
 			}
 		});
 		btnFocus.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				actMgr.trigger(FocuseButtonPress);
 			}
 		});
 		btnSettings.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				actMgr.trigger(AppMgr.toSettings);
 			}
 		});
 
 	}
 
 	@Override
 	protected void onResume() {
 		mapMaker.onResume();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		mapMaker.onPause();
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		//autodate.cancel();
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mLocClient != null) {
 			mLocClient.stop();
 		}
 		mapMaker.onDestroy();
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			AppMgr.exit();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return false;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		mMapView.onSaveInstanceState(outState);
 
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		mMapView.onRestoreInstanceState(savedInstanceState);
 	}
 
 }
