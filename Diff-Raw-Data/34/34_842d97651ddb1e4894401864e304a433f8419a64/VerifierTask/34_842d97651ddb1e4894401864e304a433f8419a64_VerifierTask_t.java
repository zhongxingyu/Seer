 package edu.jhu.isi.hms.visavis;
 
 import org.jivesoftware.smack.Chat;
 
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 
 import com.google.gson.Gson;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class VerifierTask extends AsyncTask<String, Void, StdTestResult> {
 	private final String proverId;
 	private final String verifierId;
 	private final String chatServer;
 	private final String password;
 	private final String tag= VerifierTask.class.getName();
 	private final Activity activity;
 	private final Notifier<Message> mutex = new Notifier<Message>();
 	private final Gson gson = new Gson();
 	public VerifierTask(String proverId, String verifierId, String password,
 			String chatServer, Activity activity) {
 		super();
 		this.proverId = proverId;
 		this.verifierId = verifierId;
 		this.chatServer = chatServer;
 		this.password = password;
 		this.activity = activity;
 		
 	}
 
 	@Override
 	protected StdTestResult doInBackground(String... params) {
 		try {
 			Chat chat = setup();
 			try
 			{
 				return protocol(chat);
 			} catch (XMPPException e) {
 				Log.e(this.tag,"Unable to execute protocol",e);
 			}
 		} catch (XMPPException e) {
 			Log.e(this.tag,"Unable to set up chat",e);
 		}
 		return null;
 	}
 	
 	private StdTestResult protocol(Chat chat) throws XMPPException{
 		StdTestResult t = verify(chat);
 		//proof(char);
 		return t;
 	}
 	
 	private StdTestResult verify(Chat chat)throws XMPPException {
 		try{
 			chat.sendMessage("hello");
 			Log.v(tag,"done waiting");
 			// fix me handle null
 			Message m = mutex.waitForMessage();
 			return gson.fromJson(m.getBody(), StdTestResult.class);
 		}catch(XMPPException e){
 			Log.e(this.tag,"Verifier unable to send hello message",e);
 			throw e;
 		}
 	}
 	
 	private void proof(Chat chat) throws XMPPException{
 		try{
 			Log.v(tag,"Proover waiting on hello");
 			Message m = mutex.waitForMessage();
 			chat.sendMessage("sample std result");
 			return;
 		}catch(XMPPException e){
 			Log.e(this.tag,"Verifier unable to send hello message",e);
 			throw e;
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(final StdTestResult t){
 		Log.v("STD", "changing intents");
 		Intent i = new Intent(activity.getApplicationContext(), StatusActivity.class);
		if (t!=null){
			((TextView) activity.findViewById(R.id.t)).setText(t.name);
			Bundle b = new Bundle();
			b.putSerializable(VisavisActivity.data_tag, t);
			i.putExtras(b);
		}
 		activity.startActivity(i);
 	
 	}
 	
 
 	private  Chat setup() throws XMPPException  {
//		TODO handle down server
 		Connection con = new XMPPConnection(this.chatServer);
 		con.connect();
 		con.login(this.verifierId, this.password);
 		final Chat newChat = con.getChatManager().createChat(makeaddress(),
 				new MessageListener() {
 					public void processMessage(Chat chat, Message message) {
 						Log.v(tag,String.format("message recieved: %s", message));
 						mutex.messageArived(message);
 					}
 				});
 		return newChat;	
 	}
 	
 	private String makeaddress(){
 		return proverId +"@"+chatServer;
 	}
 
 }
