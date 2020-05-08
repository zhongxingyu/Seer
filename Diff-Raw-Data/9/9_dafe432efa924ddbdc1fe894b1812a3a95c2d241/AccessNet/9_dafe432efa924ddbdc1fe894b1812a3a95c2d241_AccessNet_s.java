 package com.example.payback;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 class AccessNet{
 
 	public String simpleServerCall(String urlstub, String params) throws InterruptedException{
 		Logger AXNLOG = Logger.getLogger(AccessNet.class .getName());
 		AXNLOG.setLevel(Level.INFO);
 		String url = "http://chase.mamatey.com/PayBack/"+urlstub;
 		//String retval = "default";
 		final String[] items = new String[2];
 		items[0] = "400 Bad Request";
 		items[1] = params;
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		try {
 			final URL addr = new URL(url);
 			new Thread (new Runnable() {
 				@Override
 				public synchronized void run(){
 					//view.invalidate();//uncommenting this will render the button into an app crasher
 					Logger CANLOG = Logger.getLogger(AccessNet.class .getName());
 					CANLOG.setLevel(Level.INFO);
 					HttpURLConnection connection;
 					try {
 						connection = (HttpURLConnection) addr.openConnection();
 						connection.setDoOutput(true);
 						connection.setDoInput(true);
 						connection.setInstanceFollowRedirects(false); 
 						connection.setRequestMethod("POST"); 
 						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 						connection.setRequestProperty("charset", "utf-8");
 						connection.setRequestProperty("Content-Length", "" + Integer.toString(items[1].getBytes().length));
 						connection.setUseCaches (false);
 						CANLOG.info("Able to set HTTP: "+connection);
 
 						try{
 							DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
 							wr.writeBytes(items[1]);
 							wr.flush();
 							wr.close();
 							CANLOG.info("Data send success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection send to server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 
 						try{
 							String line;
 							BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 							line = reader.readLine();
							if(line.equalsIgnoreCase("1")||line.equalsIgnoreCase("true")||line.equalsIgnoreCase("success")||line.equalsIgnoreCase("Login and query success")||line.equalsIgnoreCase("Sign in successful"))
 								items[0]="success";
 							CANLOG.info("Data in: "+line);
 
 							reader.close();
 							CANLOG.info("Data receive success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection receive from server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 
 						connection.disconnect();
 						CANLOG.info("Performed action and server returned: "+items[0]);
 						//Toast.makeText(getApplicationContext(), "Account Created!", Toast.LENGTH_SHORT).show();
 
 					} catch (IOException e) {
 						CANLOG.warning("IOException on HTTP connection creation");
 						e.printStackTrace();
 					}           
 					latch.countDown();
 				}
 			}).start();
 		} catch (MalformedURLException e) {
 			AXNLOG.warning("URL: \""+url+"\" could not be created.");
 			e.printStackTrace();
 		}
 		
 		latch.await();
 		AXNLOG.info("return value is: \""+items[0]+"\"");
 		return items[0];
 	}
 	
 	public JSONObject jsonServerCall(String urlstub, String params) throws InterruptedException, JSONException{
 		Logger AXNLOG = Logger.getLogger(AccessNet.class .getName());
 		AXNLOG.setLevel(Level.INFO);
 		String url = "http://chase.mamatey.com/PayBack/"+urlstub;
 		
 		final String[] items = new String[2];
 		items[0] = "400 Bad Request";
 		items[1] = params;
 		final JSONObject[] retval = new JSONObject[1];
 		retval[0].put("ServerResponse", 400);
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		try {
 			final URL addr = new URL(url);
 			new Thread (new Runnable() {
 				@Override
 				public synchronized void run(){
 					Logger CANLOG = Logger.getLogger(AccessNet.class .getName());
 					CANLOG.setLevel(Level.INFO);
 					HttpURLConnection connection;
 					try {
 						connection = (HttpURLConnection) addr.openConnection();
 						connection.setDoOutput(true);
 						connection.setDoInput(true);
 						connection.setInstanceFollowRedirects(false); 
 						connection.setRequestMethod("POST"); 
 						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 						connection.setRequestProperty("charset", "utf-8");
 						connection.setRequestProperty("Content-Length", "" + Integer.toString(items[1].getBytes().length));
 						connection.setUseCaches (false);
 						CANLOG.info("Able to set HTTP: "+connection);
 
 						try{
 							DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
 							wr.writeBytes(items[1]);
 							wr.flush();
 							wr.close();
 							CANLOG.info("Data send success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection send to server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 						
 						try{
 							String line;
 							BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 							StringBuilder sb = new StringBuilder();
 						    int cp;
 						    while ((cp = reader.read()) != -1) {
 						      sb.append((char) cp);
 						    }
 						    line = sb.toString();
 						    
 						    try {
 								retval[0] = new JSONObject(line);
 								items[0] = "success data received";
 							} catch (JSONException e) {
 								CANLOG.warning("JSONException on receive from server: "+connection.getResponseMessage());
 								e.printStackTrace();
 							}
 							/*
 							if(line.equalsIgnoreCase("1"))
 								items[0]="success";
 							CANLOG.info("Data in: "+line);*/
 							
 							reader.close();
 							CANLOG.info("Data receive success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection receive from server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 
 						connection.disconnect();
 						CANLOG.info("Executed process and server returned: "+items[0]);
 						//Toast.makeText(getApplicationContext(), "Account Created!", Toast.LENGTH_SHORT).show();
 
 					} catch (IOException e) {
 						CANLOG.warning("IOException on HTTP connection creation");
 						e.printStackTrace();
 					}           
 					latch.countDown();
 				}
 			}).start();
 		} catch (MalformedURLException e) {
 			AXNLOG.warning("URL: \""+url+"\" could not be created.");
 			e.printStackTrace();
 		}
 		
 		latch.await();
 		AXNLOG.info("The process was: \""+items[0]+"\"");
 		return retval[0];
 	}
 }
