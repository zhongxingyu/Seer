 /*
  * 無所不在學習架構與學習導引機制
  * A Hybrid Ubiquitous Learning Framework and its Navigation Support Mechanism
  * 
  * FileName:	AccountUtils.java
  * 
  * Description: 帳號控制管理（登入、驗證、登入狀況...）的程式
  * 
  */
 package tw.edu.chu.csie.e_learning.util;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import tw.edu.chu.csie.e_learning.R;
 import tw.edu.chu.csie.e_learning.config.Config;
 import tw.edu.chu.csie.e_learning.provider.ClientDBProvider;
 import tw.edu.chu.csie.e_learning.server.BaseSettings;
 import tw.edu.chu.csie.e_learning.server.ServerAPIs;
 import tw.edu.chu.csie.e_learning.server.ServerUser;
 import tw.edu.chu.csie.e_learning.server.exception.HttpException;
 import tw.edu.chu.csie.e_learning.server.exception.LoginCodeException;
 import tw.edu.chu.csie.e_learning.server.exception.LoginException;
 import tw.edu.chu.csie.e_learning.server.exception.PostNotSameException;
 import tw.edu.chu.csie.e_learning.server.exception.ServerException;
 
 public class AccountUtils {
 	
 	private Context context;
 	private ClientDBProvider clientdb;
 	private ServerAPIs server;
 	private SettingUtils settingUtils;
 	
 	public AccountUtils(Context context) {
 		this.context = context;
 		clientdb = new ClientDBProvider(this.context);
 		settingUtils = new SettingUtils(this.context);
 		// 伺服端連線物件建立
 		BaseSettings srvbs = new BaseSettings();
 		srvbs.setBaseUrl(settingUtils.getRemoteURL());
 		server = new ServerAPIs(srvbs);
 	}
 	
 	/**
 	 * 是否是已登入狀態
 	 */
 	public boolean islogin(){
 		if(this.getLoginId() != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * 察看已登入的ID
 	 */
 	public String getLoginId() {
 		String query[] = clientdb.search("chu_user", "UID", null);
 		
 		// 如果有任何東西的話
 		if(query.length > 0) {
 			String getedID = query[0];
 			return getedID;
 		}
 		else {
 			return null;
 		}
 	}
 	
 	/**
 	 * 取得登入碼
 	 */
 	public String getLoginCode() {
 		String query[] = clientdb.search("chu_user", "ULogged_code", null);
 		
 		// 如果有任何東西的話
 		if(query.length > 0) {
 			return query[0];
 		}
 		else {
 			return null;
 		}
 	}
 	
 	/**
 	 * 登入帳號
 	 * @param inputLoginId 使用者輸入的ID
 	 * @param inputLoginPasswd 使用者輸入的密碼
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 * @throws JSONException 
 	 * @throws PostNotSameException 
 	 * @throws HttpException 
 	 * @throws ServerException 
 	 * 
 	 * TODO ClientProtocolException, IOException, JSONException 例外整理
 	 * @throws LoginCodeException 
 	 */
 	public void loginUser(String inputLoginId, String inputLoginPasswd) 
 			throws ClientProtocolException, IOException, JSONException, LoginException, PostNotSameException, HttpException, ServerException, LoginCodeException
 	{
		//清除登入資訊
		clientdb.delete(null, "chu_user");
		clientdb.delete(null, "chu_target");
		
		// 登入這個使用者
 		String loginCode = this.server.userLogin(inputLoginId, inputLoginPasswd);
 		ServerUser userinfo = this.server.userGetInfo(loginCode);
 		String nickName = userinfo.getNickName();
 		String loginTime = userinfo.getLoginTime();
 		
 		Log.d("nickName",nickName );
 		Log.d("loginTime", loginTime);
 		Log.d("loginCode", loginCode);
 		Log.d("ID", userinfo.getID());
 		
 		//將傳回來的資料寫入SQLite裡
 		this.clientdb.user_insert(userinfo.getID(), nickName, loginCode, loginTime);
 	}
 	
 	/**
 	 * 登出帳號
 	 * @param loginCode
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws HttpException
 	 * @throws JSONException
 	 * @throws PostNotSameException
 	 * @throws LoginCodeException
 	 * @throws ServerException
 	 */
 	public void logoutUser() throws ClientProtocolException, IOException, HttpException, JSONException, PostNotSameException, LoginCodeException, ServerException{
 		// 取得目前登入的登入碼
 		String loginCode = this.getLoginCode();
 		
 		//清除登入資訊
 		clientdb.delete(null, "chu_user");
		clientdb.delete(null, "chu_target");
 		
 		//將使用者的學習狀態傳送至後端
 		this.server.userLogout(loginCode);
 	}
 	
 }
