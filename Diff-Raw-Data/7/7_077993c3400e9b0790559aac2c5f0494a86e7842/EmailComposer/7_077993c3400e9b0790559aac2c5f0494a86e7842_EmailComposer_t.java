 package com.tricedesigns.hello;
 
 import org.apache.cordova.api.LOG;
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.PluginResult.Status;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.text.Html;
 
 import com.tricedesigns.hello.Mail;
  
 @SuppressLint("ParserError")
 public class EmailComposer extends Plugin {
 	public final String ACTION_SEND_EMAIL = "sendEmail";
 	public Context context;
 	
 	@Override
 	public PluginResult execute(String action, JSONArray arg1, String callbackId) {
 		PluginResult result = new PluginResult(Status.INVALID_ACTION);
 		if (action.equals(ACTION_SEND_EMAIL)) {
 			try {
 				String message = arg1.getString(0);
 				String attachement = arg1.getString(1);
 				String subject = arg1.getString(2);
 				this.sendEmailViaGmail(message,attachement,subject);
 				result = new PluginResult(Status.OK);
 			}
 			catch (JSONException ex) {
 				result = new PluginResult(Status.JSON_EXCEPTION, ex.getMessage());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 		}
 		return result;
 	}
 	
 	private void sendEmailViaGmail(String body, String file, String subject) throws Exception{
 		
 		Mail m = new Mail("a2dis.logistique@gmail.com", "b<-6v22Un3TMuJ&");
		String[] toArr = {"a2dis.logistique@gmail.com"};
 		m.set_to(toArr);
 		m.set_from("a2dis.logistique@gmail.com");
 		m.set_body(body);
 		m.set_subject(subject);
 		
		if(!file.equals("")) {
 			m.addAttachment(file);
		}
 		
 		boolean sendFlag = m.send();
 	}
 
 }
