 /*
  * Copyright 2010 Armin Čoralić
  * 
  * 	http://blog.coralic.nl
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.coralic.beta.sms.utils.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import nl.coralic.beta.sms.R;
 import nl.coralic.beta.sms.log.Log;
 import nl.coralic.beta.sms.utils.constants.Const;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 
 import android.content.Context;
 
 public class HttpHandler
 {
 	private HttpClient client = null;
 	
 	private ArrayList<NameValuePair> params;
 	private ArrayList<NameValuePair> headers;
 
 	private String url;
 
 	private int responseCode;
	private String errMessage = null;
 	private Context context;
 	private Header[] allHeaders;
 
 	private String response;
 	
 	public Header[] getAllHeaders()
 	{
 		return allHeaders;
 	}
 
 	public String getResponse()
 	{
 		return response;
 	}
 
 	public String getErrorMessage()
 	{
 		return errMessage;
 	}
 
 	public int getResponseCode()
 	{
 		return responseCode;
 	}
 		public HttpHandler(String url, Context conx)
 	{
 		this.url = url;
 		params = new ArrayList<NameValuePair>();
 		headers = new ArrayList<NameValuePair>();
 	}
 
 	public void AddParam(String name, String value)
 	{
 		params.add(new BasicNameValuePair(name, value));
 	}
 
 	public void AddHeader(String name, String value)
 	{
 		headers.add(new BasicNameValuePair(name, value));
 	}
 
 	public void Execute(RequestMethod method)
 	{
 		try
 		{
 			switch (method)
 			{
 				case GET:
 				{
 					// add parameters
 					String combinedParams = "";
 					if (!params.isEmpty())
 					{
 						combinedParams += "?";
 						for (NameValuePair p : params)
 						{
 							String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
 							if (combinedParams.length() > 1)
 							{
 								combinedParams += "&" + paramString;
 							}
 							else
 							{
 								combinedParams += paramString;
 							}
 						}
 					}
 	
 					HttpGet request = new HttpGet(url + combinedParams);
 	
 					// add headers
 					for (NameValuePair h : headers)
 					{
 						request.addHeader(h.getName(), h.getValue());
 					}
 	
 					executeRequest(request, url);
 					break;
 				}
 				case POST:
 				{
 					HttpPost request = new HttpPost(url);
 	
 					// add headers
 					for (NameValuePair h : headers)
 					{
 						request.addHeader(h.getName(), h.getValue());
 					}
 	
 					if (!params.isEmpty())
 					{
 						request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
 					}
 	
 					executeRequest(request, url);
 					break;
 				}
 			}
 		}
 		catch(UnsupportedEncodingException e)
 		{
 			errMessage = context.getString(R.string.ERR_UEE);
 			Log.logit(Const.TAG_HTTP, e.getMessage());
 		}
 	}
 
 	private void executeRequest(HttpUriRequest request, String url)
 	{
 		Log.logit(Const.TAG_HTTP, "Exceute request url");
 
 			client = getHttpClient();
 	
 
 		HttpResponse httpResponse;
 
 		try
 		{
 			httpResponse = client.execute(request);
 			responseCode = httpResponse.getStatusLine().getStatusCode();
 			errMessage = httpResponse.getStatusLine().getReasonPhrase();
 
 			HttpEntity entity = httpResponse.getEntity();
 			allHeaders = httpResponse.getAllHeaders();
 			if (entity != null)
 			{
 					Log.logit(Const.TAG_HTTP, "Reading body");
 					InputStream instream = entity.getContent();
 					response = convertStreamToString(instream);
 	
 					// Closing the input stream will trigger connection release
 					instream.close();
 			}
 
 		}
 		catch (ClientProtocolException e)
 		{
 			client.getConnectionManager().shutdown();
 			errMessage = context.getString(R.string.ERR_PROT);
 			Log.logit(Const.TAG_HTTP, e.getMessage());
 		}
 		catch (IOException e)
 		{
 			client.getConnectionManager().shutdown();
 			errMessage = context.getString(R.string.ERR_IO);
 			Log.logit(Const.TAG_HTTP, e.getMessage());
 		}
 	}
 	
 	private HttpClient getHttpClient()
 	{
 		HttpParams httpParameters = new BasicHttpParams();
 		int timeoutConnection = 28000;
 		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 		int timeoutSocket = 28000;
 		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		return new DefaultHttpClient(httpParameters);
 	}
 
 	private String convertStreamToString(InputStream is)
 	{
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try
 		{
 			while ((line = reader.readLine()) != null)
 			{
 				sb.append(line + "\n");
 			}
 		}
 		catch (IOException e)
 		{
 			errMessage = context.getString(R.string.ERR_IO);
 			Log.logit(Const.TAG_HTTP, e.getMessage());
 		}
 		finally
 		{
 			try
 			{
 				is.close();
 			}
 			catch (IOException e)
 			{
 				errMessage = context.getString(R.string.ERR_IO);
 				Log.logit(Const.TAG_HTTP, e.getMessage());
 			}
 		}
 		return sb.toString();
 	}
 }
