 package net.ednovak.nearby;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.XMPPException;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 
 public class xmppService extends Service {
 	
 	public static Connection conn;
 	IBinder xmppBinder = new LocalBinder();
 	
 	@Override
 	public void onCreate(){
 		// I think have nothing to do here.
 		super.onCreate();
 		//Toast.makeText(this, "service created", Toast.LENGTH_LONG).show();
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
		if ( conn != null ){
			conn.disconnect();
			conn = null;
		}
 		//Toast.makeText(this, "fb chat service destroyed", Toast.LENGTH_LONG).show();
 	}	
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startID){
 		// I think I should start a thread here
         // XMPP listener
 		
 		String user = intent.getStringExtra("user");
 		String pass = intent.getStringExtra("pass");
 		
         Runnable r = new xmppThread(user, pass, this);
         new Thread(r).start();
         //Toast.makeText(this, "thread created and ran", Toast.LENGTH_LONG).show();
         
         return START_REDELIVER_INTENT;
 	}
 	
 	
 	// Splits a long string up into several 'packets' so the fb does not filter them
 	// Each packet is 502 characters long.  A beginning @@<stage number>
 	// and, on the last packet in the stream, a trailing @@
 	private List<String> make_packets(String msg, int stage){
 		//Log.d("stage " + stage, "Dividing this string: " + msg);
 		List<String> packets = new ArrayList<String>();
 		int cur = 0;
 		int end = 0;
 		while (end < msg.length()){
 			end = Math.min(msg.length(), cur + 500);
 			//Log.d("xmpp", "Adding chunk from " + cur + " to " + end);
 			packets.add("@@" + stage + ":" + msg.substring(cur, end));
 			cur = end;
 		}
 		// I use the "@@" on the last packet to mark the end of a stream
 		packets.set(packets.size()-1, packets.get(packets.size()-1) + "@@"); // Put a "@@" on the last one
 		return packets;
 	}
 	
 	
 	// Send message that actually does the sending
 	private void real_send(Chat chat, String msg, int stage){
 		List<String> parts = make_packets(msg, stage);
 		for(int i = 0; i < parts.size(); i++){
 			//Log.d("xmpp", "part: " + parts.get(i));
 			try{
 				chat.sendMessage(parts.get(i));
 			}
 			catch (XMPPException e){
 				Log.d("xmpp", "Caught exception: " + e.toString());
 			}
 		}	
 		Log.d("xmpp", "Message sent");
 	}
 	
 	
 	// Send message exposed to real world
 	public void sendMessage(String rec, String msg, int stage, final Context context){
 		Log.d("xmpp", "Looking for: " + rec);
 		Collection<RosterEntry> entries = getRoster().getEntries();
 		if (entries != null){
 			for (RosterEntry entry : entries){
 				//Log.d("chat", ""+entry);
 				if (entry.getName().equals(rec)){
 					Log.d("xmpp", "found this person: " + entry.getName() + " " + entry.getUser() + " " + entry.getStatus());
 					Chat newChat = conn.getChatManager().createChat(entry.getUser(), null);
 					Log.d("xmpp", "sending message to: " + entry.getUser());
 					real_send(newChat, msg, stage);
 				}
 			}
 		}
 		else{
 			Log.d("xmpp", "entries was null");
 		}
 	}
 	
 	@Override
 	// I don't want to allow binding so the service keeps running in order to 
 	// accept xmpp messages in the future
 	public IBinder onBind(Intent intent){
 		//Toast.makeText(this, "service bound to", Toast.LENGTH_LONG).show();
 		
 		if ( conn == null ){
 			String user = intent.getStringExtra("user");
 			String pass = intent.getStringExtra("pass");
 			
 	        Runnable r = new xmppThread(user, pass, this);
 	        new Thread(r).start();
 	        //Toast.makeText(this, "thread created and ran", Toast.LENGTH_LONG).show();
 		}
 		
 		return xmppBinder;
 	}
 	
 	
 	// Use this to give the activity access to the public methods in this service.
 	public class LocalBinder extends Binder {
 		xmppService getService() {
 			return xmppService.this;
 		}
 	}
 	
 	public Roster getRoster(){
 		return conn.getRoster();
 	}
 }
