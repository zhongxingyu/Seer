 package com.android.iliConnect.dataproviders;
 
 import static android.content.Context.CONNECTIVITY_SERVICE;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.accounts.NetworkErrorException;
 import android.app.ProgressDialog;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 
 import com.android.iliConnect.MainActivity;
 import com.android.iliConnect.MainTabView;
 import com.android.iliConnect.MessageBuilder;
 import com.android.iliConnect.Exceptions.NetworkException;
 import com.android.iliConnect.message.IliOnClickListener;
 import com.android.iliConnect.ssl.HttpsClient;
 
 public class RemoteDataProvider extends AsyncTask<String, Integer, Exception> implements IliOnClickListener {
 
 	private Object instance = this;
 	private boolean doLogout = false;
 	private List<NameValuePair> nameValuePairs;
 	private ProgressDialog pDialog;
 
 	public RemoteDataProvider() {
 	}
 
 	public RemoteDataProvider(ProgressDialog pDialog) {
 		this.pDialog = pDialog;
 	}
 
 	public RemoteDataProvider(List<NameValuePair> nameValuePairs, ProgressDialog pDialog) {
 		this.pDialog = pDialog;
 		this.nameValuePairs = nameValuePairs;
 	}
 
 	public RemoteDataProvider(List<NameValuePair> nameValuePairs) {
 
 		this.nameValuePairs = nameValuePairs;
 	}
 
 	@Override
 	protected Exception doInBackground(String... sUrl) {
 
 		try {
 
 			HttpClient httpclient = new DefaultHttpClient();
 
 			HttpClient httpsClient = HttpsClient.createHttpsClient(httpclient);
 			HttpPost httppost = new HttpPost(sUrl[0]);
 
 			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 			nameValuePairs.add(new BasicNameValuePair("username", MainActivity.instance.localDataProvider.auth.user_id));
 			nameValuePairs.add(new BasicNameValuePair("password", MainActivity.instance.localDataProvider.auth.password));
 
 			if (this.nameValuePairs != null) {
 				nameValuePairs.addAll(this.nameValuePairs);
 			}
 			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 			// Execute HTTP Post Request
 			HttpResponse response = httpsClient.execute(httppost);
 			StatusLine status = response.getStatusLine();
 			
 			if(status.getStatusCode() == 404 || status.getStatusCode() == 401) {
 				throw new HttpException(status.getReasonPhrase());
 			}
 
 			HttpEntity entity = response.getEntity();
 
 			if (entity != null) {
 				InputStream instream = entity.getContent();
 
 				// String result = convertStreamToString(instream);
 				String targetName = MainActivity.instance.localDataProvider.remoteDataFileName;
 				if (sUrl.length > 1)
 					targetName = sUrl[1];
 
 				// download the file
 				InputStream input = instream;
 
 				String s = convertStreamToString(instream);
 
 				if (s.contains("ACCESS_DENIED"))
 					throw new AuthException(s);
 
 				BufferedWriter out = new BufferedWriter(new FileWriter(MainActivity.instance.getFilesDir() + "/" + targetName));
 				out.write(s);
 
 				out.flush();
 				out.close();
 				input.close();
 
 			}
 
 		} catch (Exception e) {
 			return e;
 
 		}
 		return null;
 
 	}
 
 	@Override
 	protected void onPreExecute() {
 		MainActivity.instance.localDataProvider.isUpdating = true;
 		if (pDialog != null)
 			pDialog.show();
 	};
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		// increment progress bar by progress value
 		if (pDialog != null)
 			pDialog.setProgress(values[0]);
 	}
 
 	@Override
 	protected void onPostExecute(Exception e) {
 		// super.onPostExecute(result);
 
 		if (e != null) {
 			String errMsg = null;
 			String errTtl = "Synchronisation fehlgeschlagen";
 			
			// Exceptions angepasst um DNS Fehler abzufangen.
			if (e instanceof UnknownHostException || e instanceof UnknownHostException || e instanceof NoHttpResponseException) {
 				ConnectivityManager connManager = (ConnectivityManager) MainActivity.instance.getSystemService(CONNECTIVITY_SERVICE);
 				NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 				NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 				boolean logout = false;
 				if (wifi != null && wifi.isConnected()) {
 					logout = true;
 				} else if (mobile != null && mobile.isConnected()) {
 					logout = true;
 				}
 
 				if (logout) {
 					doLogout = true;
 					errMsg = "Es konnte keine Verbindung zum ILIAS-Server hergestellt werden. Bitte überprüfen Sie" +
 							" die Serveradresse und versuchen Sie es erneut.";
 					errTtl = "Verbindung fehlgeschlagen";
 				}
 			} else if (e instanceof AuthException) {
 				// Logout soll nach Bestätigung durchgeführt werden
 				doLogout = true;
 				MainActivity.instance.localDataProvider.deleteAuthentication();
 				errMsg = "Ihr Benutzername oder Kennwort ist falsch.";
 			} else {
 				errMsg = "Es ist ein Fehler während der Synchronisation aufgetreten.";
 			}
 
 			if (errMsg != null) {
 				//TODO: connection_failed
 				MessageBuilder.sync_exception(MainTabView.instance, errTtl, errMsg, (IliOnClickListener) instance);
 			} 
 		}
 		else {
 			MainActivity.instance.localDataProvider.updateLocalData();
 		}
 
 
 		if (pDialog != null && pDialog.isShowing()) {
 			pDialog.dismiss();
 		}
 	}
 
 	public String convertStreamToString(InputStream inputStream) throws IOException {
 		if (inputStream != null) {
 			StringBuilder sb = new StringBuilder();
 			String line;
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
 				while ((line = reader.readLine()) != null) {
 					sb.append(line).append("\n");
 				}
 			} finally {
 				inputStream.close();
 			}
 			return sb.toString();
 		} else {
 			return "";
 		}
 	}
 
 	public void onClickCoursePassword(String refID, String password) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onClickJoinCourse(String refID, String courseName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onClickLeftCourse(String refID, String courseName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onClickMessageBox() {
 		// falls bei der Sync. die Benutzerdaten falsch sind, alte Daten löschen
 		if(doLogout) {
 			doLogout = true;
 			MainActivity.instance.logout();
 		}
 	}
 }
