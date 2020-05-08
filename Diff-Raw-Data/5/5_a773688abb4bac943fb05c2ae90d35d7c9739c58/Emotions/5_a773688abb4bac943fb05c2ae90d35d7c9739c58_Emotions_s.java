 package com.weibo.sdk.android.custom;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import weibo4android.org.json.JSONArray;
 import weibo4android.org.json.JSONException;
 import weibo4android.org.json.JSONObject;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 import com.weibo.sdk.android.Oauth2AccessToken;
 import com.weibo.sdk.android.WeiboException;
 import com.weibo.sdk.android.api.StatusesAPI;
 import com.weibo.sdk.android.api.WeiboAPI.EMOTION_TYPE;
 import com.weibo.sdk.android.api.WeiboAPI.LANGUAGE;
 import com.weibo.sdk.android.net.RequestListener;
 
 public class Emotions {
 
 	private Oauth2AccessToken accessToken = null;
 	private HashMap<String, String> emotions = null;
 	private String dir;
 	private Handler handler;
 	
 	final String NAMES_FILE = "names";
 	public final static String DATA_KEY = "data";
 	public final static int BEGIN = 0x20130400;
 	public final static int SAVING = 0x20130401;
 	public final static int FATAL_ERROR = 0x20130402;
 	
 	public Emotions(Oauth2AccessToken accessToken, String dir, Handler handler) {
 		this.accessToken = accessToken;
 		this.dir = dir;
 		this.handler = handler;
 	}
 	
 	public String getEmotion(String phrase) {
 		if (emotions == null) return null;
 		if (emotions.isEmpty()) return null;
 		if (emotions.containsKey(phrase)) return emotions.get(phrase);
 		return null;
 	}
 	
 	public int load() {
 		try {
 			File file = new File(dir + NAMES_FILE);
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			String s = null;
 			s = reader.readLine();
 			reader.close();
 			emotions = new HashMap<String, String>();
 			String ss[] = s.split(";");
 			if (ss.length == 0) {
 				emotions = null;
 				return 0;
 			}
 			for (int i = 0; i < ss.length; i++) {
 				String _ss[] = ss[i].split(",");
 				if (_ss.length != 2) {
 					emotions = null;
 					return -3;
 				}
 				emotions.put(_ss[0], _ss[1]);
 			}
 			return 1;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			emotions = null;
 			return -1;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			emotions = null;
 			return -2;
 		}
 	}
 	
 	public void update() {
 		if (accessToken == null) return;
 		StatusesAPI api = new StatusesAPI(accessToken);
 		api.emotions(EMOTION_TYPE.FACE, LANGUAGE.cnname, new RequestListener() {
 
 			@Override
 			public void onComplete(String response) {
 				// TODO Auto-generated method stub
 				try {
 					emotions = new HashMap<String, String>();
 					JSONArray jsons = new JSONArray(response);
 					//URL url;
 					//URLConnection connection;
 					//File file;
 					//InputStream is;
 					FileOutputStream fos;
 					//String path;
 					//byte[] buf;
 					//int fileSize;
 					fire(BEGIN, jsons.length());
 					for (int i = 0; i < jsons.length(); i++) {
 						JSONObject json = jsons.getJSONObject(i);
 						emotions.put(json.getString("value"), json.getString("url"));
 						fire(SAVING, i);
 						/*
 						try {
 							url = new URL(json.getString("url"));
 							connection = url.openConnection();
 							is = connection.getInputStream();
 							fileSize = connection.getContentLength();// get file size
 							if (fileSize <= 0) continue;
 							String[] fparts = url.getPath().split("/");
 							String fname = null;
 							if (fparts.length != 0) {
 								fname = fparts[fparts.length - 1];
 							}
 							if (fname == null) continue;
 							path = dir + fname;
 							if (AsyncSaver.probeFile(dir, fname) == -2) {
 								file = AsyncSaver.getSilentFile(dir, fname);
 								if (file == null) continue;
 							} else {
 								continue;
 							}
 							fos = new FileOutputStream(file);
 							buf = new byte[1024];
 							do {
 								int numread = is.read(buf);
 								if (numread == -1) {
 									break;
 								}
 								fos.write(buf, 0, numread);
 							} while (true);
 							fos.flush();
 							fos.close();
 							try {
 								is.close();
 							} catch (Exception ex) {
 								Log.e("tag", "error: " + ex.getMessage(), ex);
 							}
 							emotions.put(json.getString("value"), path);
 							fire(SAVING, i);
 						} catch (MalformedURLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 							continue;
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 							continue;
 						}
 						*/
 					}
 					try {
 						String ss = "";
 						Iterator<String> it = emotions.keySet().iterator();
 						while (it.hasNext()) {
 							String key = it.next();
 							ss += (key + "," + emotions.get(key) + ";"); 
 						}
 						fos = new FileOutputStream(dir + NAMES_FILE);
 						PrintStream ps = new PrintStream(fos);
 						ps.print(ss);
 						ps.close();
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						emotions = null;
 						fire(FATAL_ERROR, -1);
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					emotions = null;
 					fire(FATAL_ERROR, -2);
 				}
 			}
 
 			@Override
 			public void onIOException(IOException e) {
 				// TODO Auto-generated method stub
 				e.printStackTrace();
 				emotions = null;
 				fire(FATAL_ERROR, -3);
 			}
 
 			@Override
 			public void onError(WeiboException e) {
 				// TODO Auto-generated method stub
 				e.printStackTrace();
 				emotions = null;
 				fire(FATAL_ERROR, -4);
 			}
 			
 		});
 	}
 	
 	public ArrayList<Position> searchEmotions(String s) {
 		if (emotions == null) return null;
 		if (s == null) return null;
 		ArrayList<Position> es = new ArrayList<Position>();
 		int x, y;
 		String e;
 		for (int i = 0; i < s.length(); i++) {
 			x = s.indexOf("[", i);
 			if (x != -1) {
 				i++;
 				y = s.indexOf("]", i);
 				if (y != -1) {
					e = s.substring(x, y);
 					if (emotions.containsKey(e)) {
 						es.add(new Position(e, x, y));
 					}
					i += (y - x);
 				}
 			}
 		}
 		return es;
 	}
 	
 	private void fire(int what, int n) {
 		// TODO Auto-generated method stub
 		if (handler == null) return;
 		Message msg = new Message();
 		msg.what = what;
 		Bundle bundle = new Bundle();
 		bundle.putInt(DATA_KEY, n);
 		msg.setData(bundle);
 		handler.sendMessage(msg);
 	}
 	
 	public class Position {
 		public Position(String e, int x, int y) {
 			this.emotion = e;
 			this.x = x;
 			this.y = y;
 		}
 		public String emotion;
 		public int x, y;
 	}
 }
