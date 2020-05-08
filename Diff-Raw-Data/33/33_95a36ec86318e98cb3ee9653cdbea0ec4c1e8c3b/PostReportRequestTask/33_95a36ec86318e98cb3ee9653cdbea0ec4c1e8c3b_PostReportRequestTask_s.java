 package it.giacomos.android.osmer.widgets.map.report.network;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 /** 
  * This class uses PostReportAsyncTaskPool in order to be cancelled when the Activity
  * is destroyed.
  * It is important to register on creation and unregister in onCancelled and in
  * onPostExecute
  * 
  * @author giacomo
  *
  */
 public class PostReportRequestTask extends AsyncTask<String, Integer, String>{
 
 	private PostActionResultListener mReportPublishedListener;
 	private String mErrorMsg;
 	private String mUser, mLocality;
 	double mLatitude, mLongitude;
 	private String mDeviceId, mRegistrationId;
 	
 	private static String CLI = "afe0983der38819073rxc1900lksjd";
 	
 	public PostReportRequestTask(String user, String locality, double latitude,
 			double longitude, PostActionResultListener oActivity) 
 	{
 		mUser = user;
 		mLocality = locality;
 		mLatitude = latitude;
 		mLongitude = longitude;
 		mRegistrationId = "";
 		mReportPublishedListener = oActivity;
 		
 		PostReportAsyncTaskPool.Instance().registerTask(this);
 	}
 
 	public void setDeviceId(String id)
 	{
 		mDeviceId = id;
 	}
 	
 	public void setRegistrationId(String regId)
 	{
 		mRegistrationId = regId;
 	}
 	
 	@Override
 	protected String doInBackground(String... urls) 
 	{
 		mErrorMsg = "";
 		if(mLocality.length() < 2)
 			mLocality="";
 		String returnVal = "0";
 		HttpClient httpClient = new DefaultHttpClient();
         HttpPost request = new HttpPost(urls[0]);
         List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
         postParameters.add(new BasicNameValuePair("cli", CLI));
         postParameters.add(new BasicNameValuePair("n", mUser));
         postParameters.add(new BasicNameValuePair("d", mDeviceId));
         postParameters.add(new BasicNameValuePair("rid", mRegistrationId));
         postParameters.add(new BasicNameValuePair("l", mLocality));
         postParameters.add(new BasicNameValuePair("la", String.valueOf(mLatitude)));
         postParameters.add(new BasicNameValuePair("lo", String.valueOf(mLongitude)));
         
         UrlEncodedFormEntity form;
 		try {
 			form = new UrlEncodedFormEntity(postParameters);
 	        request.setEntity(form);
 	        HttpResponse response = httpClient.execute(request);
 	        StatusLine statusLine = response.getStatusLine();
 	        if(statusLine.getStatusCode() < 200 || statusLine.getStatusCode() >= 300)
 	        	mErrorMsg = statusLine.getReasonPhrase();
 	        else if(statusLine.getStatusCode() < 0)
 	        	mErrorMsg = "Server error";
 	        /* check the echo result */
 	        HttpEntity entity = response.getEntity();
 	        returnVal = EntityUtils.toString(entity);
	        if(Integer.parseInt(returnVal) < 0)
 	        	mErrorMsg = "Server error: the server returned " + returnVal;
 		} 
 		catch (UnsupportedEncodingException e) 
 		{
 			mErrorMsg = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			mErrorMsg = e.getLocalizedMessage();
 			e.printStackTrace();
 		} catch (IOException e) {
 			mErrorMsg = e.getLocalizedMessage();
 			e.printStackTrace();
 		}
 		return returnVal;
 		
 	}
 
 	@Override
 	protected void onCancelled (String result)
 	{
 		/* unregister the task from the PostReportAsyncTaskPool when finished */
 		PostReportAsyncTaskPool.Instance().unregisterTask(this);
 	}
 	
 	@Override
 	public void onPostExecute(String doc)
 	{
 		if(mErrorMsg.isEmpty())
 			mReportPublishedListener.onPostActionResult(false, doc, PostType.REQUEST);
 		else
 			mReportPublishedListener.onPostActionResult(true, mErrorMsg, PostType.REQUEST);
 		
 		PostReportAsyncTaskPool.Instance().unregisterTask(this);
 	}
 }
