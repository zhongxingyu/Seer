 package edu.ucsb.cs290.touch.to.chat.https;
 
 import info.guardianproject.onionkit.proxy.HttpManager;
 
 import java.util.Properties;
 
 import android.content.Context;
import android.util.Base64;
 import edu.ucsb.cs290.touch.to.chat.R;
 import edu.ucsb.cs290.touch.to.chat.remote.Helpers;
 import edu.ucsb.cs290.touch.to.chat.remote.messages.TokenAuthMessage;
 import edu.ucsb.cs290.touch.to.chat.remote.register.RegisterUser;
 
 public class TorProxy {
 
 	public static void postThroughTor(Context c, TokenAuthMessage tm) {
 		Properties p = new Properties();
		p.put(TokenAuthMessage.FIELD_NAME, Base64.encode(Helpers.serialize(tm), Base64.DEFAULT));
 		executeHttpsPost(c, TokenAuthMessage.FIELD_NAME, p);
 	}
 	
 	public  static void postThroughTor(Context c, RegisterUser ru) {
 		Properties p = new Properties();
		p.put(RegisterUser.FIELD_NAME, Base64.encode(Helpers.serialize(ru),Base64.DEFAULT));
 		executeHttpsPost(c, RegisterUser.FIELD_NAME, p);
 	}
 	
 	private static void executeHttpsPost(Context c, String directory, Properties p) {
 		try {
 			HttpManager.doPost(c, c.getString(R.string.service_url)+"/"+directory, p);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
