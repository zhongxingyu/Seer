 package com.zhuri.talk;
 
 import java.nio.channels.*;
 import java.net.InetAddress;
 import java.io.IOException;
 import com.zhuri.util.DEBUG;
 import com.zhuri.slot.SlotSlot;
 import com.zhuri.slot.SlotWait;
 import com.zhuri.slot.SlotTimer;
 import com.zhuri.pstcp.AppFace;
 import com.zhuri.util.InetUtil;
 
 import com.zhuri.talk.TalkClient;
 import com.zhuri.talk.UpnpRobot;
 import com.zhuri.talk.StunRobot;
 import com.zhuri.talk.protocol.Body;
 import com.zhuri.talk.protocol.Packet;
 import com.zhuri.talk.protocol.Message;
 
 import android.net.Uri;
 import android.content.Intent;
 import android.content.Context;
 import android.content.ComponentName;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 import java.net.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 
 public class TalkRobot {
 	private Context mContext;
 	private TalkClient mClient;
 	final private SlotSlot mDisconnect = new SlotSlot();
 
 	public TalkRobot(Context context) {
 		mContext = context;
 	}
 
 	private Intent parseIntent(String[] args) {
 		Intent intent = new Intent();
 		intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 		for (int i = 2; i < args.length; i++) {
 			if (args[i].equals("-a")) {
 				if (++i < args.length)
 					intent.setAction(args[i]);
 			} else if (args[i].equals("-d")) {
 				if (++i < args.length)
 					intent.setData(Uri.parse(args[i]));
 			} else if (args[i].equals("-t")) {
 				if (++i < args.length)
 					intent.setType(args[i]);
 			} else if (args[i].equals("-c")) {
 				if (++i < args.length)
 					intent.addCategory(args[i]);
 			} else if (args[i].equals("-e")) {
 				i += 2;
 			} else if (args[i].equals("--es")) {
 				i += 2;
 			} else if (args[i].equals("--esn")) {
 				i += 1;
 			} else if (args[i].equals("--ez")) {
 				i += 2;
 			} else if (args[i].equals("--ei")) {
 				i += 2;
 			} else if (args[i].equals("-n")) {
 				if (++i < args.length) {
 					String[] parts = args[i].split("/");
 					if (parts.length == 2) {
 						ComponentName n = new ComponentName(parts[0], parts[1]);
 						intent.setComponent(n);
 					}
 				}
 			} else if (args[i].equals("-f")) {
 				++i;
 			} else if (args[i].equals("--grant-read-uri-permission")) {
 				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 			} else if (args[i].equals("--grant-write-uri-permission")) {
 				intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 			} else if (args[i].equals("--debug-log-resolution")) {
 				intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
 			} else if (args[i].equals("--activity-brought-to-front")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			} else if (args[i].equals("--activity-clear-top")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			} else if (args[i].equals("--activity-clear-when-task-reset")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 			} else if (args[i].equals("--activity-exclude-from-recents")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 			} else if (args[i].equals("--activity-launched-from-history")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
 			} else if (args[i].equals("--activity-multiple-task")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 			} else if (args[i].equals("--activity-no-animation")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 			} else if (args[i].equals("--activity-no-history")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 			} else if (args[i].equals("--activity-no-user-action")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
 			} else if (args[i].equals("--activity-previous-is-top")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
 			} else if (args[i].equals("--activity-reorder-to-front")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			} else if (args[i].equals("--activity-reset-task-if-needed")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 			} else if (args[i].equals("--activity-single-top")) {
 				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			} else if (args[i].equals("--receiver-registered-only")) {
 				intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
 			} else if (args[i].equals("--receiver-replace-pending")) {
 				intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
 			} else if (!args[i].startsWith("-")) {
 				intent.setData(Uri.parse(args[i]));
 			}
 		}
 
 		return intent;
 	}
 
 	private String amStart(String[] args) {
 		if (args.length < 3) {
 			return "inval argument";
 		}
 
 		try {
 			Intent intent = parseIntent(args);
 
 			if (args[1].equals("start")) {
 				mContext.startActivity(intent);
 			} else if (args[1].equals("startservice")) {
 				mContext.startService(intent);
 			} else if (args[1].equals("broadcast")) {
 				mContext.sendBroadcast(intent);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return e.getMessage();
 		}
 
 		return "OK";
 	}
 
 	private String doTcpForward(String[] parts) {
 
 		try {
 			if (parts.length >= 3) {
 				int port = Integer.parseInt(parts[2]);
 				InetAddress addr = InetUtil.getInetAddress(parts[1]);
 				AppFace.setForward(addr.getAddress(), port);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "doTcpForward: failure";
 		}
 
 		return "doTcpForward: OK";
 	}
 
 	private String doStunSend(String[] parts) {
 		if (parts.length >= 2)
 			AppFace.stunSendRequest(parts[1], 1);
 		return "doStunSend: OK";
 	}
 
 	private String getNetworkConfig(String[] parts) {
 		String config = "";
 
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface
 					.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				List<InterfaceAddress> ifaddrs = intf.getInterfaceAddresses();
 				for (InterfaceAddress ifaddr: ifaddrs) {
 					InetAddress iaddr = ifaddr.getAddress();
 					if (iaddr != null && !iaddr.isLoopbackAddress()) {
 						config += "ifconfig: " + ifaddr.getAddress().getHostAddress() + "\r\n";
 					}
 				}   
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "ifconfig: exception";
 		}
 
 		return config;
 	}
 
 	private void onMessage(Packet packet) {
 		Message message = new Message(packet);
 
 		if (message.hasBody()) {
 			String cmd;
 			String msg = message.getContent();
 			if (msg == null || msg.equals("")) {
 				DEBUG.Print("EMPTY Message");
 				return;
 			}
 
 			String[] parts = msg.split(" ");
 
 			cmd = parts[0];
 			if (cmd.equals("stun")) {
 				StunRobot context =
 					new StunRobot(mClient, mDisconnect, packet, parts);
 				context.start();
 			} else if (cmd.equals("upnp")) {
 				UpnpRobot context =
 					new UpnpRobot(mClient, mDisconnect, packet, parts);
 				context.start();
 			} else {
 				Message reply = new Message();
 				Message message1 = new Message(packet);
 				reply.setTo(message1.getFrom());
 
 				if (cmd.equals("am")) {
 					String title = amStart(parts);
 					reply.add(new Body(title));
 				} else if (cmd.equals("ifconfig")) {
 					String title = getNetworkConfig(parts);
 					reply.add(new Body(title));
 				} else if (cmd.equals("forward")) {
 					String title = doTcpForward(parts);
 					reply.add(new Body(title));
 				} else if (cmd.equals("stun-send")) {
 					String title = doStunSend(parts);
 					reply.add(new Body(title));
 				} else if (cmd.equals("stun-name")) {
 					reply.add(new Body("STUN: " + AppFace.stunGetName()));
 				} else if (cmd.equals("version")) {
 					reply.add(new Body("VERSION: V1.0"));
 				} else {
 					reply.add(new Body("unkown command"));
 				}
 
 				mClient.put(reply);
 			}
 		}
 	}
 
 	private final SlotWait onReceive = new SlotWait() {
 		public void invoke() {
 			Packet packet = mClient.get();
 
 			while (packet != Packet.EMPTY_PACKET) {
 				DEBUG.Print("INCOMING", packet.toString());
 				if (packet.matchTag("presence")) {
 					mClient.processIncomingPresence(packet);
 				} else if (packet.matchTag("message")) {
 					mClient.processIncomingMessage(packet);
 					onMessage(packet);
 				} else if (packet.matchTag("iq")) {
 					mClient.processIncomingIQ(packet);
 				} else {
 					DEBUG.Print("unkown TAG: " + packet.getTag());
 				}
 				mClient.mark();
 				packet = mClient.get();
 			}
 
 			if (!mClient.isStreamClosed()) {
 				mClient.waitI(onReceive);
 			   return;
 			}
 
 			/* release connect resource than retry */
 			mDisconnect.wakeup();
 			mClient.disconnect();
 			mDelay.reset(5000);
 			return;
 		}
 	};
 
 	public void close() {
 		if (mClient != null)
 			mClient.disconnect();
 		mDisconnect.wakeup();
 		mDelay.clean();
 		return;
 	}
 
 	final private SlotTimer mDelay = new SlotTimer() {
 		public void invoke() {
 			start();
 			return;
 		}
 	};
 
 	public void start() {
		int port;
 		SharedPreferences pref;
 		String user, domain, server, password;
 
 		mClient = new TalkClient();
 		mClient.waitI(onReceive);
 
 		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		port = pref.getInt("port", 5222);
 		user = pref.getString("user", "dupit8");
 		domain = pref.getString("domain", "gmail.com");
 		server = pref.getString("server", "xmpp.l.google.com");
 		password = pref.getString("password", "L8PaPUL1nfQT");
 
 		mClient.start(user, domain, password, server + ":" + port);
 		return;
 	}
 }
