 package com.dary.xmpp;
 
 import java.util.Locale;
 
 import org.jivesoftware.smack.AndroidConnectionConfiguration;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.SmackAndroid;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 
 import com.dary.xmpp.cmd.CmdBase;
 import com.dary.xmpp.receivers.BatteryReceiver;
 import com.dary.xmpp.receivers.SMSReceiver;
 import com.dary.xmpp.ui.MainActivity;
 
 public class MainService extends Service {
 
 	public static Connection connection;
 	public static String notifiedAddress;
 	public static String loginAddress;
 	private String password;
 	private String serverHost;
 	private String serverPort;
 	private String serverDomain;
 	private String resource;
 	private boolean isAutoReconnect;
 	private boolean isDebugMode;
 	public SMSReceiver smsReceiver = new SMSReceiver();
 	private BatteryReceiver batteryReceiver = new BatteryReceiver();
 	private boolean isCustomServer;
 
 	public static Chat chat;
 
 	@Override
 	public void onCreate() {
 		// 启动InCallService
 		Intent incallserviceIntent = new Intent();
 		incallserviceIntent.setClass(MainService.this, IncallService.class);
 		startService(incallserviceIntent);
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (isSetComplete()) {
 			// 启动登录线程
 			LoginInThread loginInThread = new LoginInThread();
 			Thread thread = new Thread(loginInThread);
 			thread.setName("LoginThread");
 			thread.start();
 
 			// 尝试将登录的记录存储下来,先暂时只存储到普通的文本文件中
 			Tools.doLogAll("Login");
 			// 登录中,发送消息,更新UI
 
 			sendMsg(MainActivity.LOGGING);
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	// 登录线程
 	class LoginInThread implements Runnable {
 
 		public void run() {
 			ConnectionConfiguration config = null;
 			MyApp myApp = (MyApp) getApplication();
 			myApp.setIsShouldRunning(true);
 			SmackAndroid.init(myApp);
 			if (isCustomServer) {
 				config = new AndroidConnectionConfiguration(serverHost, Integer.parseInt(serverPort), serverDomain);
 			} else {
 				try {
					config = new AndroidConnectionConfiguration(serverHost);
				} catch (XMPPException e) {
 					e.printStackTrace();
 				}
 			}
 
 			// config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
 			// config.setReconnectionAllowed(false);
 			// config.setSendPresence(false);
 			// config.setCompressionEnabled(false);
 			// config.setSecurityMode(SecurityMode.enabled);
 			// config.setSASLAuthenticationEnabled(true);
 
 			connection = new XMPPConnection(config);
 			try {
 				Tools.doLogJustPrint("Connect to Server");
 				connection.connect();
 				// SASLAuthentication.supportSASLMechanism("PLAIN", 0);
 			} catch (Exception e) {
 				Tools.doLogAll("Connection Failed");
 				doNotification(MainService.this, "Connection Failed");
 				sendMsg(MainActivity.CONNECTION_FAILED);
 				e.printStackTrace();
 				return;
 			}
 			// 防止重新连接时多次登录
 			if (!connection.isAuthenticated() && connection.isConnected()) {
 				Tools.doLogPrintAndFile("Verify Password");
 				try {
 					// connection.login(loginAddress,password,resource);
 					connection.login(loginAddress, password, Tools.getTimeStr());
 				} catch (Exception e) {
 					Tools.doLogAll("Login Failed");
 					doNotification(MainService.this, "Login Failed");
 					sendMsg(MainActivity.LOGIN_FAILED);
 					e.printStackTrace();
 					return;
 				}
 				// 如果用户在登录只取消了登录,并且登录成功,需要Disconnect
 				if (!myApp.getIsShouldRunning()) {
 					if (connection.isConnected()) {
 						Presence presence = new Presence(Presence.Type.unavailable);
 						connection.sendPacket(presence);
 						connection.disconnect();
 					}
 				}
 				// 需要先判断用户是否已经停止登录
 				if (myApp.getIsShouldRunning()) {
 					// Tools.Vibrator(MainService.this, 500);
 					Tools.doLogAll("Login Successful");
 					doNotification(MainService.this, "Login Successful");
 
 					// 登录成功后发送消息通知Activity改变按钮状态
 					sendMsg(MainActivity.LOGIN_SUCCESSFUL);
 
 					ChatManager chatmanager = connection.getChatManager();
 
 					// 注册消息监听器
 					chat = chatmanager.createChat(notifiedAddress.toLowerCase(Locale.getDefault()), new MsgListener());
 
 					// 登录成功之后再在程序动态的注册电量改变,短信的广播接收器,注册电量改变的接收器时会设置Presence
 					registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 					registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
 
 					// 登录成功后发送消息,用于测试
 					if (isDebugMode) {
 						CmdBase.sendMessageAndUpdateView(chat, "Login Successful");
 					}
 				}
 			}
 		}
 	}
 
 	// 如果正在登录中时进行中断,并不会立即处理
 	@Override
 	public void onDestroy() {
 		Tools.doLogAll("Service Destroy");
 		MyApp myApp = (MyApp) getApplication();
 		myApp.setIsShouldRunning(false);
 		sendMsg(MainActivity.NOT_LOGGED_IN);
 		if (connection.isConnected()) {
 			Presence presence = new Presence(Presence.Type.unavailable);
 			connection.sendPacket(presence);
 			connection.disconnect();
 		}
 		Intent incallserviceIntent = new Intent();
 		incallserviceIntent.setClass(MainService.this, IncallService.class);
 		stopService(incallserviceIntent);
 		// 反注册广播接收器
 		try {
 			unregisterReceiver(batteryReceiver);
 			unregisterReceiver(smsReceiver);
 		} catch (Exception e) {
 			// 尚未注册
 			e.printStackTrace();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	private void getSetting() {
 		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		isCustomServer = mPrefs.getBoolean("isCustomServer", false);
 		Tools.doLogJustPrint("isCustomServer " + isCustomServer);
		serverHost = mPrefs.getString("serverHost", "jabber.org");
 		Tools.doLogJustPrint("serverHost " + serverHost);
 		serverPort = mPrefs.getString("serverPort", "5222");
 		Tools.doLogJustPrint("serverPort " + serverPort);
 		serverDomain = mPrefs.getString("serverDomain", "");
 		Tools.doLogJustPrint("serverDomain " + serverDomain);
 		loginAddress = mPrefs.getString("loginAddress", "");
 		Tools.doLogJustPrint("loginAddress " + loginAddress);
 		password = mPrefs.getString("password", "");
 		Tools.doLogJustPrint("password " + password);
 		notifiedAddress = mPrefs.getString("notifiedAddress", "");
 		Tools.doLogJustPrint("notifiedAddress " + notifiedAddress);
 		resource = mPrefs.getString("resource", "");
 		Tools.doLogJustPrint("resource " + resource);
 		isAutoReconnect = mPrefs.getBoolean("isAutoReconnect", true);
 		Tools.doLogJustPrint("isAutoReconnect " + isAutoReconnect);
 		isDebugMode = mPrefs.getBoolean("isDebugMode", false);
 		Tools.doLogJustPrint("isDebugMode " + isDebugMode);
 	}
 
 	public static void sendMsg(int tag) {
 		MyApp myApp = (MyApp) MyApp.getContext();
 		myApp.setStatus(tag);
 		// 登录中,发送消息,更新UI
 		if (null != MainActivity.MsgHandler) {
 			// 考虑修改为,当Activity启动的时候去读取状态
 			Message msg = new Message();
 			msg.what = tag;
 			MainActivity.MsgHandler.sendMessage(msg);
 		}
 	}
 
 	private void doNotification(Context context, String str) {
 		// 需要先判断用户是否已经停止登录
 		if (((MyApp) getApplication()).getIsShouldRunning()) {
 			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 			String notificationType = mPrefs.getString("notificationType", "none");
 			if (notificationType.equals("none")) {
 
 			} else if (notificationType.equals("notification")) {
 				Tools.makeNotification(context, str);
 			} else if (notificationType.equals("vibrate")) {
 				Tools.Vibrator(context, 1000);
 			} else if (notificationType.equals("sound")) {
 				Tools.makeSound(context);
 			} else if (notificationType.equals("notification,vibrate")) {
 				Tools.makeNotification(context, str);
 				Tools.Vibrator(context, 1000);
 			} else if (notificationType.equals("notification,sound")) {
 				Tools.makeNotification(context, str);
 				Tools.makeSound(context);
 			} else if (notificationType.equals("vibrate,sound")) {
 				Tools.Vibrator(context, 1000);
 				Tools.makeSound(context);
 			} else if (notificationType.equals("notification,vibrate,sound")) {
 				Tools.makeNotification(context, str);
 				Tools.Vibrator(context, 1000);
 				Tools.makeSound(context);
 			}
 		}
 	}
 
 	// 如果配置不全,显示Toast
 	// 如果是自定义服务器,判断应有区别
 	private boolean isSetComplete() {
 		getSetting();
 		if (isCustomServer) {
 			if (serverHost.equals("") || serverPort.equals("") || serverDomain.equals("")) {
 				sendMsg(MainActivity.SET_INCOMPLETE);
 				Tools.doLogAll("Set Incomplete");
 				return false;
 			}
 		}
 		if (loginAddress.equals("") || password.equals("") || notifiedAddress.equals("")) {
 			sendMsg(MainActivity.SET_INCOMPLETE);
 			Tools.doLogAll("Set Incomplete");
 			return false;
 		}
 		return true;
 	}
 }
