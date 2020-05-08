 package org.dobots.dodedodo;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.jivesoftware.smack.AndroidConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionListener;
 //import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.SmackAndroid;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.*;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.filetransfer.FileTransferManager;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.RemoteException;
 //import android.os.Handler;
 import android.os.IBinder;
 //import android.os.Message;
 import android.os.Messenger;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class XMPPService extends Service {
 	private static final String TAG = "XMPPService";
 	private static final int PORT = 5222; // Not used anymore, since it does a dns srv lookup
 	public static final String ADMIN_JID = "hal9000@dobots.customers.luna.net";
 	
 	private String mBareJid;
 	private String mResource;
 
 	/** Target we publish for clients to send messages to IncomingHandler. */
 	final Messenger mMessenger = new Messenger(new MsgServiceHandler());
 	//final Messenger mModuleMessenger = new Messenger(new ModuleMsgHandler());
 	
 	private SharedPreferences mSharedPref;
 	
 //	class PortKey {
 //		public String moduleName;
 //		public int moduleId;
 //		public String portName;
 //		public boolean equals(Object obj) {}
 //		public int hashCode() {}
 //	}
 	
 //	HashMap<PortKey, Messenger> mPortsIn = new HashMap<PortKey, Messenger>();
 	
 //	public String getPortInKey(String module, int id, String port) {
 //		return new String("in." + module + "." + id + "." + port);
 //	}
 //	
 //	public String getPortOutKey(String module, int id, String port) {
 //		return new String("out." + module + "." + id + "." + port);
 //	}
 	
 	class PortIn {
 		public String mDevice;
 		public String mModuleName;
 		public int mModuleId;
 		public String mPortName;
 		public Messenger mMessenger;
 		public PortIn(String device, Messenger messenger, String module, int id, String port) {
 			mDevice = device;
 			mModuleName = module;
 			mModuleId = id;
 			mPortName = port;
 			mMessenger = messenger;
 		}
 	}
 	
 	class PortOut {
 		public String mDevice;
 		public String mModuleName;
 		public int mModuleId;
 		public String mPortName;
 		public Messenger mMessenger;
 		public PortOut(String device, Messenger messenger, String module, int id, String port) {
 			mDevice = device;
 			mModuleName = module;
 			mModuleId = id;
 			mPortName = port;
 			mMessenger = messenger;
 		}
 	}
 	
 	// Key is local port
 	// Ports that get data from local modules
 	HashMap<String, PortIn> mPortsIn = new HashMap<String, PortIn>();
 	// Ports that send data to local modules
 	HashMap<String, PortOut> mPortsOut = new HashMap<String, PortOut>();
 	
 //	// For showing and hiding our notification.
 //	NotificationManager mNM;
 	
 	Messenger mMsgService = null;
 	
 	
 	private SmackAndroid mSmackAndroid;
 	private XMPPConnection mXmppConnection;
 	private PacketListener mXmppMsgListener;
 	private PacketListener mXmppSubListener;
 //	private PacketListener mXmppAllListener;
 //	private PacketCollector mPacketCollector;
 	private FileTransferManager mFileTransferManager;
 
 	@Override
 	public IBinder onBind(final Intent intent) {
 //		return new LocalBinder<XMPPService>(this);
 //		return null; // No binding provided
 		return mMessenger.getBinder();
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		mSmackAndroid = SmackAndroid.init(this);
 		
 		mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		
 //        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 //		// Display a notification about us starting.
 //        showNotification();
 
 //		mXmppAllListener = new PacketListener() {
 //			public void processPacket(Packet packet) {
 //				Log.d(TAG, "Received msg: " + packet.toXML());
 //			}
 //		};
 		
 		mXmppMsgListener = new PacketListener() {
 			public void processPacket(Packet packet) {
 				Log.d(TAG, "Received msg: " + packet.toXML());
 				
 				
 //				Log.d(TAG, "debug_mode=" + mSharedPref.getBoolean("debug_mode", false));
 				if (!mSharedPref.getBoolean("debug_mode", false)) {
 					// Filter packet based on added buddies		
 					String from = org.jivesoftware.smack.util.StringUtils.parseBareAddress(packet.getFrom());
 					if (!from.equals(mBareJid) && mXmppConnection.getRoster().getEntry(from) == null) {
 						Log.w(TAG, "User is not added: " + packet.getFrom());
 						return;
 					}
 				}
 				
 				if (!packet.getTo().endsWith("/" + mResource) && !packet.getTo().equals(mBareJid)) {
 					Log.w(TAG, "to=" + packet.getTo() + " does not end with: /" + mResource);
 					return;
 				}
 				
 				Message message = (Message) packet;
 				String body = message.getBody();
 				
 				// Port data: parse and send to the local module
 				if (body.startsWith("AIM data ")) {
 					// 0   1    2              3      4  5    6        7       8     9
 					// AIM data int/float      module id port number
 					// AIM data int/floatarray module id port datatype nArrays nDims sizeDim1 sizeDim2 ...
 					// AIM data string         module id port data
 					String[] words = body.split(" ");
 					
 					String key = "out." + words[3] + "." + words[4] + "." + words[5];
 					Log.d(TAG, "portKey=" + key);
 					PortOut pOut = mPortsOut.get(key);
 					if (pOut == null) {
 						Log.i(TAG, "pOut == null");
 						return;
 					}
 					if (pOut.mMessenger == null) {
 						Log.i(TAG, "pOut.mMessenger == null");
 						return;
 					}
 					
 					String header = new String();
 					for (int i=0; i<6; ++i) {
 						header += words[i] + " ";
 					}
 					Log.d(TAG, "header=" + header);
 					
 					Bundle bundle = new Bundle();
 					int type = -1;
 					if (words[2].equals("string")) {
 						type = AimProtocol.DATATYPE_STRING;
 						bundle.putString("data", body.substring(header.length()));
 					}
 					else if (words[2].equals("int")) {
 						
 						type = AimProtocol.DATATYPE_INT;
 						int val;
 						try {
 							val = Integer.parseInt(words[6]);
 						} catch (NumberFormatException e) {
 							Log.w(TAG, "cannot convert " + words[6] + " to int");
 							return;
 						}
 						bundle.putInt("data", val);
 					}
 					else if (words[2].equals("intarray")) {
 							type = AimProtocol.DATATYPE_INT_ARRAY;
 							int[] val = new int[words.length-6];
 							try {
 								for (int i=6; i<words.length; ++i) {
 									val[i-6] = Integer.parseInt(words[i]);
 								}
 							} catch (NumberFormatException e) {
 								Log.w(TAG, "cannot convert " + body + " to intarray");
 								return;
 							}
 							bundle.putIntArray("data", val);
 						}
 					else if (words[2].equals("float")) {
 						type = AimProtocol.DATATYPE_FLOAT;
 						float val;
 						try {
 							val = Float.parseFloat(words[6]);
 						} catch (NumberFormatException e) {
 							Log.w(TAG, "cannot convert " + words[6] + " to float");
 							return;
 						}
 						bundle.putFloat("data", val);
 					}
 					else if (words[2].equals("floatarray")) {
 						type = AimProtocol.DATATYPE_FLOAT_ARRAY;
 						float[] val = new float[words.length-6];
 						try {
 							for (int i=6; i<words.length; ++i) {
 								val[i-6] = Float.parseFloat(words[i]);
 							} 
 						} catch (NumberFormatException e) {
 							Log.w(TAG, "cannot convert " + body + " to floatarray");
 							return;
 						}
 						bundle.putFloatArray("data", val);
 					}
 
 					if (type > -1) {
 						bundle.putInt("datatype", type);
 						android.os.Message portMsg = android.os.Message.obtain(null, AimProtocol.MSG_PORT_DATA);
 						//messengerMsg.replyTo = messenger;
 						portMsg.setData(bundle);
 						msgSend(pOut.mMessenger, portMsg);
 					}
 					
 				}
 				
 				// Command: send to msgService
 				else if (body.startsWith("AIM ")) {
 
 					android.os.Message msg = android.os.Message.obtain(null, AimProtocol.MSG_XMPP_MSG);
 					Bundle bundle = new Bundle();
 					bundle.putString("jid", packet.getFrom());
 					bundle.putString("body", body);
 					msg.setData(bundle);
 					msgSend(mMsgService, msg);
 				}
 			}
 		};
 		
 
 		mXmppSubListener = new PacketListener() {
 			public void processPacket(Packet packet) {
 				Log.i(TAG, "Received presence: " + packet.toXML());
 				Presence presence = (Presence) packet;
 				if (presence.getType().equals(Presence.Type.subscribe)) {
 					String jid = org.jivesoftware.smack.util.StringUtils.parseBareAddress(presence.getFrom());
 					if (!jid.equals(ADMIN_JID))
 						return;
 					
 					try {
 						mXmppConnection.getRoster().createEntry(jid, jid, null);
 						Log.i(TAG, "Created roster entry: " + jid);
 					} catch (XMPPException e) {
 						Log.e(TAG, "Error in roster entry creation: " + e);
 					}
 					
 					
 					
 					Presence presenceReply = new Presence(Presence.Type.subscribed);
 					presenceReply.setTo(presence.getFrom());
 					mXmppConnection.sendPacket(presenceReply);
 					
 				}
 			}
 		};
 
 	}
 
 	@Override
 	public int onStartCommand(final Intent intent, final int flags, final int startId) {
 		return Service.START_NOT_STICKY;
 	}
 
 	@Override
 	public boolean onUnbind(final Intent intent) {
 		return super.onUnbind(intent);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 //		// Cancel the persistent notification.
 //        mNM.cancel(R.string.remote_service_started);
 		
 		// TODO: cancel reconnect
 		if (mXmppConnection != null) {
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					mXmppConnection.disconnect();
 				}
 			}).start();
 		}
 		
 		if (mSmackAndroid != null)
 			mSmackAndroid.onDestroy();
 		
 		Log.d(TAG, "on destroy");
 	}
 
 	/** Handler of incoming messages from msg service. */
 	class MsgServiceHandler extends Handler {
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			switch (msg.what) {
 			case AimProtocol.MSG_REGISTER:
 //				Log.i(TAG, "client added");
 //				mClients.add(msg.replyTo);
 				Log.i(TAG, "msgService registered");
 				mMsgService = msg.replyTo;
 //				android.os.Message messengerMsg = android.os.Message.obtain(null, MsgService.MSG_SET_MESSENGER);
 //				messengerMsg.replyTo = mModuleMessenger;
 //				msgSend(mMsgService, messengerMsg);
 				break;
 			case AimProtocol.MSG_UNREGISTER:
 //				Log.i(TAG, "client removed");
 //				mClients.remove(msg.replyTo);
 				Log.i(TAG, "msgService unregistered");
 				mMsgService = null;
 				break;
 			case AimProtocol.MSG_XMPP_LOGIN:
 				Log.i(TAG, "login");
 				
 				if (mXmppConnection != null)
 					mXmppConnection.disconnect();
 				
 				new Thread(new Runnable() {
 					@Override
 					public void run() {
 						xmppConnect();
 					}
 				}).start();
 				break;
 			case AimProtocol.MSG_SET_MESSENGER: {
 				// Add in port of local module 
 				Log.i(TAG, "set messenger: " + msg.replyTo.toString());
 				String deviceOut = msg.getData().getString("otherDevice");
 				String moduleOut = msg.getData().getString("otherModule");
 				int idOut = msg.getData().getInt("otherID");
 				String portOut = msg.getData().getString("otherPort");
 				String key = msg.getData().getString("port");
 				Log.i(TAG, "key=" + key);
 				PortOut pOut = new PortOut(deviceOut, msg.replyTo, moduleOut, idOut, portOut);
 //				String key = makePortOutKey();
 //				String key = "in.test.1.test";
 				mPortsOut.put(key, pOut); // TODO: remove them again......
 				break;
 			}
 			case AimProtocol.MSG_GET_MESSENGER: {
 				// Add out port of local module
 //				PortKey key = new PortKey();
 //				key.moduleName = msg.getData().getString("module");
 //				key.moduleId = msg.getData().getInt("id");
 //				key.portName = msg.getData().getString("port");
 				
 				String deviceIn = msg.getData().getString("otherDevice");
 				String moduleIn = msg.getData().getString("otherModule");
 				int idIn = msg.getData().getInt("otherID");
 				String portIn = msg.getData().getString("otherPort");
 				
 				String key = msg.getData().getString("port");
 				Messenger messenger;
 				PortIn pIn = mPortsIn.get(key);
 				if (pIn != null) {
 					messenger = pIn.mMessenger;
 				}
 				else {
 					messenger = new Messenger(new ModuleMsgHandler(key));
 					pIn = new PortIn(deviceIn, messenger, moduleIn, idIn, portIn);
 					mPortsIn.put(key, pIn); // TODO: remove them again..
 				}
 				Log.i(TAG, "get messenger " + key + " to=" + deviceIn + "/" + moduleIn + "[" + idIn + "]:" + portIn);
 //				Log.i(TAG, "get messenger " + key.moduleName + "[" + key.moduleId + "]:" + key.portName + " " + messenger.toString());
 				
 				android.os.Message messengerMsg = android.os.Message.obtain(null, AimProtocol.MSG_SET_MESSENGER);
 				messengerMsg.replyTo = messenger;
 				messengerMsg.setData(msg.getData());
 				msgSend(mMsgService, messengerMsg);
 				
 				break;
 			}
 			case AimProtocol.MSG_UNSET_MESSENGER: {
 				Log.i(TAG, "unset messenger: " + msg.getData().getString("port"));
 				String portName = msg.getData().getString("port");
 				if (portName == null)
 					break;
 				if (portName.startsWith("in.")) {
 //					PortIn pIn = mPortsIn.get(portName);
 //					if (pIn != null)
 						mPortsIn.remove(portName);
 				}
 				
 				if (portName.startsWith("out.")) {
 //					PortOut pOut = mPortsOut.get(portName);
 //					if (pOut != null)
 						mPortsOut.remove(portName);
 				}
 				
 				break;
 			}
 			case AimProtocol.MSG_XMPP_MSG:
 				Log.i(TAG, "Sending xmpp msg to " + msg.getData().getString("jid") + ": " + msg.getData().getString("body"));
 				if (!xmppSend(msg.getData().getString("jid"), msg.getData().getString("body"))) {
 					Log.i(TAG, "Could not send xmpp msg");
 				}
 				break;
 			case AimProtocol.MSG_PORT_DATA:
 				Log.i(TAG, "port data on wrong messenger");
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 	
 	
 	/** Handler of incoming messages from modules. */
 	class ModuleMsgHandler extends Handler {
 		String portName;
 		public ModuleMsgHandler(String name) {
 			portName = name;
 		}
 		@Override
 		public void handleMessage(android.os.Message msg) {
 			switch (msg.what) {
 //			case MsgService.MSG_REGISTER_MODULE:
 //				Log.i(TAG, "module added");
 //				mClients.add(msg.replyTo);
 //				break;
 //			case MsgService.MSG_UNREGISTER_MODULE:
 //				Log.i(TAG, "client removed");
 //				mClients.remove(msg.replyTo);
 //				break;
 			case AimProtocol.MSG_PORT_DATA:
 				Log.d(TAG, "received data from " + portName + " datatype: " + msg.getData().getInt("datatype"));
 				
 				// AIM data int/float module id port nDims sizeDim1 sizeDim2 .. <data>
 				// AIM data string module id port <data>
 				StringBuffer xmppMsg = new StringBuffer("AIM data ");
 				
 				xmppMsg.append(AimProtocol.getXmppDataType(msg.getData().getInt("datatype")));
 				xmppMsg.append(" ");
 				//String[] portNameParts = portName.split("."); // "in.module.id.portname" 
 				//xmppMsg += portNameParts[1] + " " + portNameParts[2] + " " + portNameParts[3];
 				PortIn pIn = mPortsIn.get(portName);
 				if (pIn == null)
 					return;
 				xmppMsg.append(pIn.mModuleName);
 				xmppMsg.append(" ");
 				xmppMsg.append(pIn.mModuleId);
 				xmppMsg.append(" ");
 				xmppMsg.append(pIn.mPortName);
 				switch (msg.getData().getInt("datatype")) {
 				case AimProtocol.DATATYPE_FLOAT:
 					// 1 dimensions of size 1
 					xmppMsg.append(" ");
 					xmppMsg.append(msg.getData().getFloat("data"));
 					break;
 				case AimProtocol.DATATYPE_FLOAT_ARRAY:
 					//xmppMsg += " 1 " + msg.getData().getFloatArray("data").length;
 					for (float f : msg.getData().getFloatArray("data")) {
 						xmppMsg.append(" ");
 						xmppMsg.append(f);
 					}
 					break;
 				case AimProtocol.DATATYPE_INT:
 					// 1 dimensions of size 1
 					xmppMsg.append(" ");
 					xmppMsg.append(msg.getData().getInt("data"));
 					break;
 				case AimProtocol.DATATYPE_INT_ARRAY:
 					// xmppMsg += " 1 " + msg.getData().getIntArray("data").length;
 					for (int i : msg.getData().getIntArray("data")) {
 						xmppMsg.append(" ");
 						xmppMsg.append(i);
 					}
 					break;
 				case AimProtocol.DATATYPE_STRING:
 					xmppMsg.append(" ");
 					xmppMsg.append(msg.getData().getString("data"));
 					break;
 //				case AimProtocol.DATATYPE_IMAGE:
 //					break;
 //				case AimProtocol.DATATYPE_BINARY:
 //					break;
 				}
 				
 //				String jid = new String(mBareJid + "/" + pIn.mDevice);
 				String jid = new String(pIn.mDevice);
 				Log.d(TAG, "Sending to " + jid + ": " + xmppMsg);
 				if (!xmppSend(jid, xmppMsg.toString())) {
 //					android.os.Message reply = android.os.Message.obtain(null, XMPPService.MSG_NOT_LOGGED_IN);
 //					if (msg.replyTo == null) {
 //						Log.i(TAG, "msg.replyTo is null!!");
 //					}
 //					msgSend(msg.replyTo, reply);
 					// Do nothing
 					Log.w(TAG, "could not send xmpp msg!");
 				}
 				
 				
 				
 //				xmppSend(jid, xmppMsg);
 				
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 	
 	
 	private void msgSend(Messenger messenger, android.os.Message msg) {
 		if (messenger == null || msg == null) {
 			Log.e(TAG, "msgSend() - messenger or msg is null");
 			return;
 		}
 		try {
 			messenger.send(msg);
 		} catch (RemoteException e) {
 			// do nothing?
 		}
 	}
 	
 /*	private void msgBroadcast(android.os.Message msg) {
 		for (int i=mPortsOut.size()-1; i>=0; i--) {
 			try {
 				mPortsOut.get(i).send(msg);
 			} catch (RemoteException e) {
 				// The client is dead: remove it from the list.
 				// We are going through the list from back to front, so this is safe to do inside the loop.
 				mPortsOut.remove(i);
 			}
 		}
 	}*/
 	
 	private boolean xmppSend(String jid, String body) {
 		if (mXmppConnection == null)
 			return false;
 		if (!mXmppConnection.isConnected())
 			return false;
 		
 		Message xmppMsg = new Message();
 		xmppMsg.setType(Message.Type.chat);
 		xmppMsg.setTo(jid);
 		xmppMsg.setBody(body);
 		mXmppConnection.sendPacket(xmppMsg);
 		return true;
 	}
 	
 	private boolean xmppConnect() {
 		Log.i(TAG, "Connecting..");
 		
 		SharedPreferences sharedPref = getSharedPreferences("org.dobots.dodedodo.login", Context.MODE_PRIVATE);
 		String jid = sharedPref.getString("jid", "");
 		String password = sharedPref.getString("password", "");
 		
 		String resource = sharedPref.getString("resource", "");
 //		int resourcePostfix = sharedPref.getInt("resourcePostfix", 0); 
 		
 //		Log.i(TAG, "jid=" + jid + " pw=" + password);
 		
 		if (TextUtils.isEmpty(jid) || TextUtils.isEmpty(password))
 			return false;
 		
 		if (TextUtils.isEmpty(resource)) {
 			Random rand = new Random(); // Seeded by current time
 			int postfix = rand.nextInt(999) + 1; // number from 1 to 999
 			resource = "android_" + android.os.Build.MODEL.replaceAll(" ", "_") + "_" + postfix; 
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.putString("resource", resource);
 			editor.commit();
 		}
 				
 		String[] split = jid.split("@");
 		if (split.length != 2)
 			return false;
 		
 		String username = split[0];
 		String host = split[1];
 		mResource = resource;
 		
 //		Log.i(TAG, "host=" + host + " user=" + username + " pw=" + password + " resource=" + resource);
 		Log.i(TAG, "host=" + host + " user=" + username + " resource=" + mResource);
 	
 		String serviceName = host;
 		AndroidConnectionConfiguration connConfig;
 		try {
 			connConfig = new AndroidConnectionConfiguration(serviceName);
 		} catch (XMPPException e) {
 			return false;
 		}
 		connConfig.setSASLAuthenticationEnabled(true);
 		connConfig.setReconnectionAllowed(true);
 		connConfig.setCompressionEnabled(true);
 		connConfig.setRosterLoadedAtLogin(true);
 //		connConfig.setTruststoreType("BKS");
 		// http://stackoverflow.com/questions/10850300/using-the-android-truststore-for-asmack-in-android-4-ics
 		
 		
 //		ConnectionConfiguration connConfig = new ConnectionConfiguration(host, PORT);
 //		connConfig.setSASLAuthenticationEnabled(true);
 //		connConfig.setReconnectionAllowed(true);
 //		connConfig.setCompressionEnabled(true);
 //		connConfig.setRosterLoadedAtLogin(true);
 //		//connConfig.setKeystorePath(keystorePath)
 		
 		/**
 		If you want compressed XMPP streams you have to add
 		[jzlib-1.0.7](http://www.jcraft.com/jzlib/) to your project. Note that
 		every version higher then 1.0.7 wont work.
 		More Info: https://github.com/Flowdalic/smack/issues/12
 		*/
 		
 //		connConfig.setDebuggerEnabled(true);
 		mXmppConnection = new XMPPConnection(connConfig);
 
 		// Reject all presence subscription requests.
 		Roster roster = mXmppConnection.getRoster();
 		roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
 //		roster.setSubscriptionMode(Roster.SubscriptionMode.reject_all);
 		
 //		mXmppConnection.DEBUG_ENABLED = true;
 
 		try {
 			mXmppConnection.connect();
 			if (!mXmppConnection.isAuthenticated())
 				mXmppConnection.login(username, password, mResource);
 		}
 		catch (final XMPPException e) {
 			Log.e(TAG, "Could not connect to Xmpp server.", e);
 			android.os.Message failMsg = android.os.Message.obtain(null, AimProtocol.MSG_XMPP_CONNECT_FAIL);
 			msgSend(mMsgService, failMsg);
 			return false;
 		}
 		
 		if (!mXmppConnection.isConnected()) {
 			Log.e(TAG, "Could not connect to the Xmpp server.");
 			android.os.Message failMsg = android.os.Message.obtain(null, AimProtocol.MSG_XMPP_CONNECT_FAIL);
 			msgSend(mMsgService, failMsg);
 			return false;
 		}
 		
 		ConnectionListener connectionListener = new ConnectionListener() {
 
 			@Override
 			public void connectionClosed() {
 				Log.i(TAG, "xmpp connectionClosed");
 			}
 
 			@Override
 			public void connectionClosedOnError(Exception e) {
 				Log.i(TAG, "xmpp connectionClosedError: " + e.toString());
 			}
 
 			@Override
 			public void reconnectingIn(int seconds) {
 				Log.i(TAG, "xmpp reconnecting in " + seconds + "s");
 			}
 
 			@Override
 			public void reconnectionSuccessful() {
 				Log.i(TAG, "xmpp reconnected");
 			}
 
 			@Override
 			public void reconnectionFailed(Exception e) {
 				Log.i(TAG, "xmpp reconnection failed");
 			}
 			
 		};
 		
 		mXmppConnection.addConnectionListener(connectionListener);
 
 /*
 		// We need this to get the file transfer work
 		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mXmppConnection);
 		if (sdm == null)
 			sdm = new ServiceDiscoveryManager(mXmppConnection);
 		sdm.addFeature("http://jabber.org/protocol/disco#info");
 		sdm.addFeature("jabber:iq:privacy");
 				
 		mFileTransferManager = new FileTransferManager(mXmppConnection);
 		FileTransferNegotiator.setServiceEnabled(mXmppConnection, true);
 */		
 
 //		OutgoingFileTransfer transfer = mFileTransferManager.createOutgoingFileTransfer(mJid);
 //		try {
 //			transfer.sendFile(new File("/mnt/sdcard/DCIM/2011-11-27 02.11.06.jpg"), "test");
 //		} catch (XMPPException e) {
 //			// Do nothing?
 //		}
 		
 		// TODO: seperate thread please! (maybe all services should be on a seperate thread, since they may interfere with the UI)
 
 //		while (!transfer.isDone()) {
 //			if (transfer.getStatus().equals(Status.error)) {
 //				Log.i(TAG,"ERROR!!! " + transfer.getError());
 //			} else if (transfer.getStatus().equals(Status.cancelled) || transfer.getStatus().equals(Status.refused)) {
 //				Log.i(TAG,"Cancelled!!! " + transfer.getError());
 //			}
 //			try {
 //				Thread.sleep(100L);
 //			} catch (InterruptedException e) {
 //				e.printStackTrace();
 //			}
 //		}
 //		if (transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error) || transfer.getStatus().equals(Status.cancelled)) {
 //			Log.i(TAG,"refused cancelled error " + transfer.getError());
 //		} else {
 //			Log.i(TAG,"Success");
 //		}
 
 		
 		Log.i(TAG, "Connected to the XMPP server!");
 		mBareJid = jid;
 
 //		// Remove all roster entries
 //		for (RosterEntry entry : roster.getEntries()) {
 //			Log.i(TAG, "roster: " + entry);
 //			try {
 //				roster.removeEntry(entry);
 //			} catch (XMPPException e) {
 //				Log.e(TAG, " couldn't remove entry from roster: " + e);
 //			}
 //		}
 
 		PacketFilter fromMeFilter = new FromMatchesFilter(mBareJid);
 		PacketFilter fromDodedodoFilter = new FromMatchesFilter(ADMIN_JID);
 		PacketFilter fromFilter = new OrFilter(fromMeFilter, fromDodedodoFilter);
 //		PacketFilter msgFilter = new PacketTypeFilter(Message.class);
 		PacketFilter msgFilter = new MessageTypeFilter(Message.Type.chat);
 //		mPacketCollector = mXmppConnection.createPacketCollector(null); // Polling method
 
 //		Log.i(TAG, "Sent presence: " + mXmppConnection.isSendPresence());
 		mXmppConnection.addPacketListener(mXmppMsgListener, msgFilter);
 //		mXmppConnection.addPacketListener(mXmppMsgListener, new AndFilter(msgFilter, fromFilter));
 		
 		PacketFilter subFilter = new PacketTypeFilter(Presence.class);
 		mXmppConnection.addPacketListener(mXmppSubListener, subFilter);
 		
 //		mXmppConnection.addPacketListener(mXmppAllListener, null);
 		
 		try {
 			roster.createEntry(ADMIN_JID, ADMIN_JID, null);
 			Presence presence = new Presence(Presence.Type.subscribe);
 			presence.setTo(ADMIN_JID);
 	
 			Log.i(TAG, presence.toXML());
 			mXmppConnection.sendPacket(presence);
 		} catch (XMPPException e) {
 			Log.e(TAG, "xmppexception: " + e);
 		}
 		
 		android.os.Message loginMsg = android.os.Message.obtain(null, AimProtocol.MSG_XMPP_LOGGED_IN);
 		msgSend(mMsgService, loginMsg);
 		
 		return true;
 	}
 
 //	public static String getThreadSignature() {
 //		final Thread t = Thread.currentThread();
 //		return new StringBuilder(t.getName()).append("[id=").append(t.getId()).append(", priority=")
 //				.append(t.getPriority()).append("]").toString();
 //	}
 
 //	/**
 //	 * Show a notification while this service is running.
 //	 */
 //	private void showNotification() {
 //        // In this sample, we'll use the same text for the ticker and the expanded notification
 //        CharSequence text = "Dodedodo running";
 //
 //        // Set the icon, scrolling text and timestamp
 //        Notification notification = new Notification(R.drawable.stat_sample, text,
 //                System.currentTimeMillis());
 //
 //        // The PendingIntent to launch our activity if the user selects this notification
 //        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 //                new Intent(this, Controller.class), 0);
 //
 //        // Set the info for the views that show in the notification panel.
 //        notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
 //                       text, contentIntent);
 //
 //        // Send the notification.
 //        // We use a string id because it is a unique number.  We use it later to cancel.
 //        mNM.notify(R.string.remote_service_started, notification);
 //    }
 
 }
