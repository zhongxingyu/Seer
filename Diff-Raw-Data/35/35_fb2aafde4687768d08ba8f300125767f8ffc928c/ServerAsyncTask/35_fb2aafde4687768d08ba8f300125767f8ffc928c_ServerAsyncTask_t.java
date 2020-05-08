 package com.hmml.api;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 
 import com.hmml.knightswatch_android.GameActivity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 public class ServerAsyncTask extends AsyncTask<String, Void, String> {
 	
 	private GameActivity gameActivity;
 	private Socket socket;
	private Boolean setupGame = false;
 	
 	final private static String DEFAULT_HOST = "192.168.1.116"; // "10.0.2.2";/
 	final private static int DEFAULT_PORT = 9999;
 	
 	public ServerAsyncTask(GameActivity gameActivity, Socket socket){
 		super();
 		this.gameActivity = gameActivity;
 		this.socket = socket;
 
 	}
 	
 	@Override
 	protected String doInBackground(String... params) {
 		try {
			if ( socket == null)
 			socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		String result = "";
 
 		// receive all messages and push them to GameActivity
		if(params[0] != null && params[0].equalsIgnoreCase("receiver")){
 			while(true){
 				try {
 					Scanner socketIn = new Scanner(socket.getInputStream());
 					result = socketIn.nextLine();
 					gameActivity.receiveMoveFromServer(result);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		// initialize connection with server
 		if(params[0] != null && params[0].equalsIgnoreCase("first")){
 			// register as player or watcher - defined in params[1]
 			
 			// start the game
			setupGame = true;
 		}
 		
 		// send a move
 		if(params[0] != null && params[1] != null && params[0].equalsIgnoreCase("send")){
 			// move to send defined in params[1]
 			try {
 				PrintStream socketOut = new PrintStream(socket.getOutputStream());
 				socketOut.println(params[1]);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return params.toString();
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
		Toast.makeText(gameActivity, "ServerAsyncTask Ending.. " + result, Toast.LENGTH_SHORT).show();
		if (setupGame)
			gameActivity.initGame(socket);
			
 	}
 
 }
