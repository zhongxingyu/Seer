 package org.canthack.tris.oyver;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Queue;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 public class Voter implements Runnable {
 	private static final String TAG = "OyVer Voter";
 	private static final String SERIAL_FILENAME = "votes";
 	public static final int MAX_ATTEMPTS = 10;
 
 	private Context ctx = null;
 	private boolean running = true;
 	private OyVerApp app;
 
 	public Voter(Context c, OyVerApp app){
 		this.ctx = c;		
 		this.app = app;
 		deserialiseVotes();
 	}
 
 	public void queueVote(Vote v) {
 		app.votes.add(v);
 	}
 
 	public void stop(){
 		Log.v(TAG, "Voter Stopping");
 		running = false;
 	}
 
 	public void setContext(Context c){
 		this.ctx = c;
 	}
 
 	private void deserialiseVotes(){
 		synchronized(Voter.class){
 			boolean found = false;
 			for(String s: ctx.fileList()){
 				if(s.equals(SERIAL_FILENAME)){
 					found = true;
 					break;
 				}
 			}
 			if(found){
 				Log.v(TAG, "DeSerialising votes");
 
 				FileInputStream fis;
 				try {
 					fis = ctx.openFileInput(SERIAL_FILENAME);
 				} catch (FileNotFoundException e1) {
 					Log.v(TAG, "Couldnt find deserialise file.");
 					return;
 				}
 
 				try {
 					ObjectInputStream os = new ObjectInputStream(fis);
 					try {
 						app.votes = (Queue<Vote>) os.readObject();
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 						Log.v(TAG, "Couldnt deserialise file.");
 					}
 
 					os.close();
 					ctx.deleteFile(SERIAL_FILENAME);
 
 				} catch (IOException e) {
 					e.printStackTrace();
 					Log.v(TAG, "Couldnt deserialise file.");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		Log.v(TAG, "Voter Run Starting");
 
 		while(running){
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			if(!app.votes.isEmpty()){
 				ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
 				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 
 				boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();
 
 				if(isConnected){
 					if(sendVote(app.votes.peek())){
 						Log.v(TAG, "Vote sent successfully " + app.votes.peek().getUrl());
 						app.votes.poll();
 						notifyVoteQueueChanged();
 					}		
 				}
 			}
 		}
 		//stopped, serialise what we haven't sent yet
 		serialiseVotes();
 	}
 
 	private void notifyVoteQueueChanged() {
 		Log.d("Voter", "Broadcasting changed message");
 		Intent intent = new Intent("voteQueueUpdated");
 		LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
 	}
 
 	private void serialiseVotes(){
 		synchronized(Voter.class){
 			if(!app.votes.isEmpty()){
 				Log.v(TAG, "Serialising votes");
 
 				FileOutputStream fos = null;
 				try { 
 					ctx.deleteFile(SERIAL_FILENAME);
 					fos = ctx.openFileOutput(SERIAL_FILENAME, Context.MODE_PRIVATE);
 				} catch (FileNotFoundException e) {
 					Log.v(TAG, "Could not serialise votes");
 					e.printStackTrace();
 					return;
 				}
 
 				try {
 					ObjectOutputStream os = new ObjectOutputStream(fos);
 					os.writeObject(app.votes);
 					os.flush();
 					os.close();
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			else{
 				ctx.deleteFile(SERIAL_FILENAME);
 			}
 		}
 	}
 
 	private boolean sendVote(Vote v) {
 		InputStream s = CustomHTTPClient.retrieveStream(v.getUrl());	
 		if(s == null) return false;
 
 		try {
 			s.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return true;	
 	}
 }
