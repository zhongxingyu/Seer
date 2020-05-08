 /**
  * 所有和後端伺服器連結的都寫在這兒～ （暫定）
  */
 package tw.edu.chu.csie.e_learning.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import tw.edu.chu.csie.e_learning.server.exception.HttpException;
 import tw.edu.chu.csie.e_learning.server.exception.LoginCodeException;
 import tw.edu.chu.csie.e_learning.server.exception.LoginException;
 import tw.edu.chu.csie.e_learning.server.exception.PostNotSameException;
 import tw.edu.chu.csie.e_learning.server.exception.ServerException;
 
 public class ServerAPIs {
 	
 	private BaseSettings baseSettings;
 	private ServerUtils utils;
 	
 	public ServerAPIs(BaseSettings bs) {
 		this.baseSettings = bs;
 		this.utils = new ServerUtils();
 	}
 
 	/**
 	 * 使用者登入
 	 * @param inputLoginId 登入帳號
 	 * @param inputLoginPasswd 登入密碼
 	 * @return 登入碼
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws JSONException
 	 * @throws LoginException
 	 * @throws PostNotSameException 
 	 * @throws HttpException 
 	 * @throws ServerException 
 	 */
 	public String userLogin(String inputLoginId, String inputLoginPasswd) 
 			throws ClientProtocolException, IOException, JSONException, LoginException, PostNotSameException, HttpException, ServerException
 	{
 		//傳送的資料要用NameValuePair[]包裝
 		List<NameValuePair> data = new ArrayList<NameValuePair>();
 		data.add(new BasicNameValuePair("uid",inputLoginId));
 		data.add(new BasicNameValuePair("upasswd", inputLoginPasswd));
 		
 		//與伺服端連線
 		String message = this.utils.getServerData(this.baseSettings.getApiUrl()+"Users/login.php?op=login", data);
 			
 		//若伺服端接到的uid與傳送的不合
		if(new JSONObject(message).getString("uid") != inputLoginId) {
 			throw new PostNotSameException();
 		}
 		//若傳送給的資料是否與伺服端接到的資料相同
 		else {
 			//如果伺服器傳回的狀態為正常
 			boolean status_ok = new JSONObject(message).getBoolean("status_ok");
 			if( status_ok ) {
 				String login_code = new JSONObject(message).getString("logincode");				
 				return login_code;
 			}
 			//若伺服器傳回為登入失敗
 			else {
 				//從伺服器取得錯誤代碼
 				String status = new JSONObject(message).getString("status");
 				if(status == "NoFound") {
 					throw new LoginException(LoginException.NO_FOUND);
 				}
 				else if(status == "NoActiveErr") {
 					throw new LoginException(LoginException.NO_ACTIVE);
 				}
 				else if(status == "PasswdErr") {
 					throw new LoginException(LoginException.PASSWORD_ERROR);
 				}
 				else {
 					throw new ServerException();
 				}
 			}				
 			
 		}
 	}
 	
 	/**
 	 * 使用者登出
 	 * @param inputLoginCode
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws HttpException
 	 * @throws JSONException
 	 * @throws PostNotSameException
 	 * @throws LoginCodeException
 	 * @throws ServerException 
 	 */
 	public void userLogout(String inputLoginCode)
 			throws ClientProtocolException, IOException, HttpException, JSONException, PostNotSameException, LoginCodeException, ServerException {
 		//傳送的資料要用NameValuePair[]包裝
 		List<NameValuePair> data = new ArrayList<NameValuePair>();
 		data.add(new BasicNameValuePair("ucode",inputLoginCode));
 		
 		//與伺服端連線
 		String message = this.utils.getServerData(this.baseSettings.getApiUrl()+"Users/login.php?op=logout", data);
 			
 		//若伺服端接到的uid與傳送的不合
		if(new JSONObject(message).getString("ucode") != inputLoginCode) {
 			throw new PostNotSameException();
 		}
 		//若傳送給的資料是否與伺服端接到的資料相同
 		else {
 			//如果伺服器傳回的狀態為正常
 			boolean status_ok = new JSONObject(message).getBoolean("status_ok");
 			if( status_ok ) {
 				String login_code = new JSONObject(message).getString("logincode");
 			}
 			//若伺服器傳回為登入失敗
 			else {
 				//從伺服器取得錯誤代碼
 				String status = new JSONObject(message).getString("status");
 				
 				if(status == "NoUserFound") {
 					throw new LoginCodeException();
 				}
 				else {
 					throw new ServerException();
 				}
 			}
 		}
 	}
 }
