 package net.exiva.twitpic;
 
 import danger.app.Application;
 import danger.app.AppResources;
 import danger.app.Bundle;
 import danger.app.Event;
 import danger.app.EventType;
 import danger.app.SettingsDB;
 import danger.app.SettingsDBException;
 import danger.app.IPCIncoming;
 import danger.app.IPCMessage;
 import danger.app.Registrar;
 
 import danger.audio.Meta;
 
 import danger.mime.Base64;
 
 import danger.net.HTTPConnection;
 import danger.net.HTTPTransaction;
 
 import danger.ui.AlertWindow;
 import danger.ui.DialogWindow;
 import danger.ui.NotificationManager;
 import danger.ui.MarqueeAlert;
 
 import danger.util.DEBUG;
 import danger.util.StringUtils;
 import danger.util.MetaStrings;
 
 import java.io.StringReader;
 import java.io.IOException;
 
 import org.kxml2.io.KXmlParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 public class twitpic extends Application implements Resources, Commands {
 	public boolean firstLaunch = true;
 	private static boolean mIsAppForeground;
 	public static int rsz, svc;
 	MarqueeAlert mMarquee;
 	public static byte[] end;
 	public static SettingsDB twitpicPrefs;
 	static private String error, password, postStatus, tagName, text, upload = "/api/upload", uploadandpost = "/api/uploadAndPost", username, className, host, source = "hiptop";
 	static private String twitpic = "http://twitpic.com";
 	static private String twitgoo = "http://twitgoo.com";
 	static private String yfrog = "http://yfrog.com";
 	static private String yfrogAPIKey = "235KMOQUd4fa0560aafa8082876a2328a6c7ac59";
 	static public twitpicView mWindow;
 	static public twitpicLoginView mLogin;
 	AlertWindow aError, aPassword, aWelcome;
 
 	public twitpic() {
 		mWindow = twitpicView.create();
 		mLogin = twitpicLoginView.create();
 		className = getBundle().getClassName();
 		mMarquee = new MarqueeAlert("null", Application.getCurrentApp().getResources().getBitmap(ID_MARQUEE),1);
 		mLogin.show();
 		aWelcome = Application.getCurrentApp().getResources().getAlert(ID_WELCOME, this);
 		aPassword = Application.getCurrentApp().getResources().getAlert(ID_PASSWORD_ERROR, this);
 		aError = Application.getCurrentApp().getResources().getAlert(ID_SUBMIT_ERROR, this);
 		//register a provider to get into the photo picker
 		//4.6+
 		Registrar.registerProvider("send-via", this, 1, Application.getCurrentApp().getResources().getBitmap(ID_MARQUEE), "TwitPic", 'T', Registrar.DATA_TYPE_FLAG_PHOTO_RECORD);
 		//legacy
 		// Registrar.registerProvider("send-via", this, 1, Application.getCurrentApp().getResources().getBitmap(ID_MARQUEE), "TwitPic", 'T');
 	}
 
 	public void launch() {
 		firstLaunch=false;
 		restoreData();
 	}
 
 	public void resume() {
 		mIsAppForeground=true;
 	}
 
 	public void suspend() {
 		mIsAppForeground=false;
 	}
 
 	public void restoreData() {
 		if (SettingsDB.findDB("twitpicSettings") == false) {
 			twitpicPrefs = new SettingsDB("twitpicSettings", true);
 			twitpicPrefs.setAutoSyncNotifyee(this);
 			aWelcome.show();
 		} else {
 			twitpicPrefs = new SettingsDB("twitpicSettings", true);
 			username = twitpicPrefs.getStringValue("username");
 			password = twitpicPrefs.getStringValue("password");
 			try {
 			rsz = twitpicPrefs.getIntValue("resize");
 			svc = twitpicPrefs.getIntValue("service");
 			} catch (SettingsDBException exception) {}
 			mLogin.setLogin(username, password);
 			mWindow.restoreSettings(rsz, svc);
 			checkAuth(username, password);
 		}
 	}
 
 	public static void storeLogin(String inUser, String inPass) {
 		twitpicPrefs.setStringValue("username", inUser);
 		twitpicPrefs.setStringValue("password", inPass);
 		mLogin.setLogin(inUser,inPass);
 		username = inUser;
 		password = inPass;
 	}
 
 	public static void setSettings(int resize, int service) {
 		twitpicPrefs.setIntValue("resize", resize);
 		twitpicPrefs.setIntValue("service", service);
 		svc=service;
 	}
 	
 	public static void checkAuth(String inUser, String inPass) {
 		username = inUser;
 		password = inPass;
 		mLogin.disableInput();
 		String twitterLogin = Base64.encode((inUser+":"+inPass).getBytes());
 		HTTPConnection.get("http://twitter.com/account/verify_credentials.json", "Authorization: Basic "+twitterLogin, (short) 0, 3);
 	}
 
 	public static void uploadandpost(String body, byte[] oJPEG, String filename, int size, String mime) {
 		switch(svc) {
 			case 1:
 				host = twitgoo+uploadandpost;
 			break;
 			case 2:
 				host = yfrog+uploadandpost;
 			break;
 			default:
 				host = twitpic+uploadandpost;
 			break;
 		}
 		byte[] start = new String("--AaB03x\r\n" +
 		   					"Content-Disposition: form-data; name=\"username\"\r\n" +
 							"\r\n" +
 							username+"\r\n" +
 							"--AaB03x\r\n" +
 							"Content-Disposition: form-data; name=\"password\"\r\n" +
 							"\r\n" +
 							password+"\r\n" +
 							"--AaB03x\r\n" +
 							"Content-Disposition: form-data; name=\"message\"\r\n" +
 							"\r\n" +
 							body+"\r\n" +
 							"--AaB03x\r\n" +
 							"Content-Disposition: form-data; name=\"source\"\r\n" +
 							"\r\n" +
 							source+"\r\n" +
 							"--AaB03x\r\n" +
 							"content-disposition: form-data; name=\"media\"; filename=\""+filename+"\"\r\n" +
 							"Content-Type: "+mime+"\r\n" +
 							"\r\n").getBytes();
 		if (svc==2) {
 			end = new String("\r\n" +
 									"--AaB03x--"+
 									"Content-Disposition: form-data; name=\"key\"\r\n" +
 									"\r\n" +
 									yfrogAPIKey+"\r\n" +
 									"--AaB03x\r\n").getBytes();
 		} else {
 			end = new String("\r\n" +
 								"--AaB03x--").getBytes();
 		}
 												
 		byte[] body2 = new byte[start.length + oJPEG.length + end.length];
 		System.arraycopy(start, 0, body2, 0, start.length);
 		System.arraycopy(oJPEG, 0, body2, start.length, oJPEG.length);
 		System.arraycopy(end, 0, body2, start.length + oJPEG.length, end.length);
 		
 		String headers = "Content-type: multipart/form-data, boundary=AaB03x\r\n" +
 						"User-Agent: Danger Hiptop v1.0/30\r\n" +
 						"Content-length: " + body2.length;
 
 		HTTPConnection.post(host, headers, body2, (short) 0, 1);
 	}
 
 	public static void upload(byte[] oJPEG, String filename, int size, String mime) {
 		switch(svc) {
 			case 1:
 				host = twitgoo+upload;
 			break;
 			case 2:
 				host = yfrog+upload;
 			break;
 			default:
 				host = twitpic+upload;
 			break;
 		}
 		byte[] start = new String("--AaB03x\r\n" +
 		   					"Content-Disposition: form-data; name=\"username\"\r\n" +
 							"\r\n" +
 							username+"\r\n" +
 							"--AaB03x\r\n" +
 							"Content-Disposition: form-data; name=\"password\"\r\n" +
 							"\r\n" +
 							password+"\r\n" +
 							"--AaB03x\r\n" +
 							"Content-Disposition: form-data; name=\"source\"\r\n" +
 							"\r\n" +
 							source+"\r\n" +
 							"--AaB03x\r\n" +
 							"content-disposition: form-data; name=\"media\"; filename=\""+filename+"\"\r\n" +
 							"Content-Type: "+mime+"\r\n" +
 							"\r\n").getBytes();
 		if (svc==2) {
 			DEBUG.p("Service is yFrog.");
 			end = new String("\r\n" +
 									"--AaB03x--"+
 									"Content-Disposition: form-data; name=\"key\"\r\n" +
 									"\r\n" +
 									yfrogAPIKey+"\r\n" +
 									"--AaB03x\r\n").getBytes();
 		} else {
 			end = new String("\r\n" +
 								"--AaB03x--").getBytes();
 		}
 												
 		byte[] body2 = new byte[start.length + oJPEG.length + end.length];
 		System.arraycopy(start, 0, body2, 0, start.length);
 		System.arraycopy(oJPEG, 0, body2, start.length, oJPEG.length);
 		System.arraycopy(end, 0, body2, start.length + oJPEG.length, end.length);
 		
 		String headers = "Content-type: multipart/form-data, boundary=AaB03x\r\n" +
 						"User-Agent: Danger Hiptop v1.0/30\r\n" +
 						"Content-length: " + body2.length;
 
 		HTTPConnection.post(host, headers, body2, (short) 0, 2);
 	}
 	
 	public void parsePostResponse(String response, boolean copy) {
 		StringReader sr = new StringReader(response);
 		String postStatus, postMessage, tagName, text;
 		KXmlParser xpp = new KXmlParser();
 		try {
 			xpp.setInput(sr);
 		}
 		catch (XmlPullParserException ex) { }
 
 		try {
 			int eventType = xpp.getEventType();
  			while (eventType != xpp.END_DOCUMENT) {
 				if (eventType == xpp.START_TAG) {
 					tagName = xpp.getName();
 					if (tagName.equals("rsp")) {
 						postStatus = xpp.getAttributeValue(0);
 						if (postStatus.equals("ok")) {
 							//Close the sending post window, and clear the text.
 							mWindow.clearPostWindow();
 							if (!this.mIsAppForeground) { 
 								mMarquee.setText("Picture successfully posted.");
 								NotificationManager.marqueeAlertNotify(mMarquee);
 							}
 							Meta.play(Meta.BEEP_ACTION_SUCCESS);
 							if (copy) {
 								mWindow.copy();
 							}
 							mWindow.clear();
 						} else if (postStatus.equals("fail")) {
 							mWindow.clearPostWindow();
 						}
 					}
 					eventType = xpp.next();
 					if (eventType == xpp.TEXT) {
 						text = xpp.getText();
 						if (tagName.equals("mediaurl")) {
 							mWindow.setURL(text);
 						}	
 					}
 				}
 				eventType = xpp.next();
 				if (eventType == 2) {
 					text = xpp.getText();
 					tagName = xpp.getName();
 					if (tagName.equals("err")) {
 							if (xpp.getAttributeValue(0).equals("1001")) {
 								mLogin.show();
 								mLogin.enableInput();
 								aPassword.show();
 							} if (xpp.getAttributeValue(0).equals("1003")) {
 								aError.setMessage("Invalid image type. This error should never occur. Please contact support@crampedthumbs.com with Error 1003 in the Subject.");
 								aError.show();
 							} if (xpp.getAttributeValue(0).equals("1004")) {
 								aError.setMessage("The image you tried to post was larger than 4MB. Please enable resizing in the settings and try again.");
 								aError.show();
 							}
 						}
 					}
 				}
 			}
 		catch (XmlPullParserException ex) { }
 		catch (IOException ioex) { }
 	}
 
 	public void handleMessage(IPCMessage ipcmessage, int i) {
 		//4.6+
 		mWindow.showSelectedPhotos(ipcmessage.findGalleryItemIPCPayload("photo-records"));
 		//legacy
 		// mWindow.showSelectedPhotos(ipcmessage.findPhotoRecordIPCPayload("photo-records"));
 		Bundle twitpic = Bundle.findByClassName("net.exiva.twitpic.twitpic");
 		Registrar.bringToForeground(twitpic);
 	}
 	
 	public void networkEvent(Object object) {
 		if (object instanceof HTTPTransaction) {
 			HTTPTransaction t = (HTTPTransaction) object;
 			if((t.getSequenceID() == 1)) {
 				if (t.getResponse() == 200) {
 					parsePostResponse(t.getString(), false);
 				}
 			}
 			if((t.getSequenceID() == 2)) {
 				if (t.getResponse() == 200) {
 					parsePostResponse(t.getString(), true);
 				}
 			}
 			if (t.getSequenceID() == 3) {
 				if (t.getResponse() != 200) {
 					if (!this.mIsAppForeground) { 
 						mMarquee.setText("Login error. Check your username and password,");
 						NotificationManager.marqueeAlertNotify(mMarquee);
 					}
 					NotificationManager.playErrorSound();
 					mLogin.show();
 					mLogin.enableInput();
 					aPassword.show();
 				} else if (t.getResponse() == 200) {
 					storeLogin(username, password);
 					mLogin.hide();
 					mLogin.stopThrobber();
 					mWindow.show();
 				}
 			}
 			t = null;
 		}
 	}
 
 	public boolean receiveEvent(Event e) {
 		switch (e.type) {
 			case Event.EVENT_AUTO_SYNC_DONE: {
 				restoreData();
 				return true;
 			}
 			case EventType.EVENT_MESSAGE: {
 				switch(e.what) {
 					case 1 : {
 						handleMessage(((IPCIncoming)e.argument).getMessage(), e.what);
 						return true;
 					}
 				}
 			}
 		}
 		return (super.receiveEvent(e));
 	}
 }
