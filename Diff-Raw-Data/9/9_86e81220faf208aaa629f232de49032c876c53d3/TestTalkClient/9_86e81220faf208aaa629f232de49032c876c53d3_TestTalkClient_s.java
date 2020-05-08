 package test;
 
 import java.nio.channels.*;
 import java.io.IOException;
 import com.zhuri.util.DEBUG;
 import com.zhuri.slot.SlotSlot;
 import com.zhuri.slot.SlotWait;
 import com.zhuri.slot.SlotTimer;
 import com.zhuri.net.STUNClient;
 import com.zhuri.talk.TalkClient;
 import com.zhuri.talk.UpnpRobot;
 import com.zhuri.talk.StunRobot;
 import com.zhuri.talk.protocol.Body;
 import com.zhuri.talk.protocol.Packet;
 import com.zhuri.talk.protocol.Message;
 
 public class TestTalkClient {
 	private TalkClient mClient;
 	final private SlotSlot mDisconnect = new SlotSlot();
 
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
 			if (cmd.equals("upnp")) {
 				UpnpRobot context =
 					new UpnpRobot(mClient, mDisconnect, packet, parts);
 				context.start();
 			} else if (cmd.equals("stun")) {
 				StunRobot context =
 					new StunRobot(mClient, mDisconnect, packet, parts);
 				context.start();
 			} else {
 				DEBUG.Print("MSG " + msg);
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
 		mClient = new TalkClient();
 		mClient.waitI(onReceive);
		mClient.start("1447754732", "uc.sina.com.cn", "GAkJoEtq75x9", "xmpp.uc.sina.com.cn:5222");
 		return;
 	}
 }
