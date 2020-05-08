 /**
  * Copyright (C) 2010-2012 Magnus Raaum, Lars Moland Eliassen, Christoffer Jun Marcussen, Rune Sætre
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * README:
  * - To compile:	javac -d . -cp "httpcomponents-client-4.2-beta1/lib/httpmime-4.2-beta1.jar:android.jar:." -encoding UTF-8 src/test/BusTUC/*.java
  * - To run:		java  -cp  .:httpcomponents-client-4.2-beta1/lib/httpmime-4.2-beta1.jar test/BusTUC/Main/BusTUCApp
  * 
  */
 
 package test.BusTUC.Speech;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import test.BusTUC.Calc.Calculate;
 
 import android.content.Context;
 import android.os.Environment;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 
 public class HTTP
 {
 	public void sendPostByteArray(byte[] buf)
 	{
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(
 				"http://vm-6114.idi.ntnu.no:1337/SpeechServer/sst");
 		
 		try
 		{
 			
 			MultipartEntity entity = new MultipartEntity();
 			// entity.addPart("speechinput", new FileBody((buf,
 			// "application/zip"));
 			entity.addPart("speechinput", new ByteArrayBody(buf, "Jun.wav"));
 			
 			httppost.setEntity(entity);
 			String response = EntityUtils.toString(httpclient.execute(httppost)
 					.getEntity(), "UTF-8");
 			System.out.println("RESPONSE: " + response);
 		} catch (ClientProtocolException e)
 		{
 		} catch (IOException e)
 		{
 		}
 	}
 	
 	public CBRAnswer blackList(double lat, double lon, String prevGuess, Context context)
 	{
 		HttpClient client = new DefaultHttpClient();
 		String response = "";
 		CBRAnswer answ = null;
 		Calculate calc = null;
 		try
 		{
 			final TelephonyManager tm = (TelephonyManager) context
 					.getSystemService(Context.TELEPHONY_SERVICE);
 			String t_id = tm.getDeviceId();
 			String tmp = "TABuss";
 			String p_id = Secure.getString(context.getContentResolver(),
 					Secure.ANDROID_ID);
 			HttpGet httpget = new HttpGet(
 					"http://vm-6114.idi.ntnu.no:1337/SpeechServer/cbrGuess?lat="+lat+"&lon="+lon+"&devID="+tmp+p_id+"&dest="+ URLEncoder.encode(prevGuess, "UTF-8")+"&blacklist=true");
 			long first = System.nanoTime();
 			response = EntityUtils.toString(client.execute(httpget)
 					.getEntity(), "UTF-8");
 			calc = new Calculate();
 			System.out.println("RESPONSE: " + response);
 			answ = calc.createCBRAnswer(response);
 			
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		} 
 		return answ;
 	}
 
 	public CBRAnswer getCBRGuess(double lat, double lon, Context context)
 	{
 		HttpClient client = new DefaultHttpClient();
 		String response = "";
 		CBRAnswer answ = null;
 		Calculate calc = null;
 		try
 		{
 			final TelephonyManager tm = (TelephonyManager) context
 					.getSystemService(Context.TELEPHONY_SERVICE);
 			String t_id = tm.getDeviceId();
 			String tmp = "TABuss";
 			String p_id = Secure.getString(context.getContentResolver(),
 					Secure.ANDROID_ID);
 			HttpGet httpget = new HttpGet(
 					"http://vm-6114.idi.ntnu.no:1337/SpeechServer/cbrGuess?lat="+lat+"&lon="+lon+"&devID="+tmp+p_id);
 			long first = System.nanoTime();
 			response = EntityUtils.toString(client.execute(httpget)
 					.getEntity(), "UTF-8");
 			calc = new Calculate();
 			System.out.println("RESPONSE: " + response);
 			answ = calc.createCBRAnswer(response);
 			
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		} 
 		return answ;
 
 	}
 	
 	
 	public void sendGetTTS(String input)
 	{
 		HttpClient client = new DefaultHttpClient();
 
 		try
 		{
 			HttpGet httpget = new HttpGet(
 					"http://vm-6114.idi.ntnu.no:1337/SpeechServer/tts?textInput="
 							+ URLEncoder.encode(input, "UTF-8"));
 			long first = System.nanoTime();
 
 			HttpResponse response = client.execute(httpget);
 			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
 			response.getEntity().writeTo(outstream);
 			long second = System.nanoTime() - first;
 			
 			System.out.println("TIME TTSGET: " + second/1000000000.0);
 			byte [] responseBody = outstream.toByteArray();
 			File sdCard = Environment.getExternalStorageDirectory();
 			File dir = new File (sdCard.getAbsolutePath() + "/tts");
 			if(!dir.exists())dir.mkdirs();
 			File file = new File(dir, "tmp.wav");
 			FileOutputStream fos = new FileOutputStream(file);
             fos.write(responseBody);
 			
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		} 
 
 	}
 
 	public DummyObj sendPost(String filePath, Context context, double lat, double lon)
 	{
 		String response = "Fant ikke noe";
 		long first = System.nanoTime();
 		Calc calc = new Calc();
 		DummyObj dummy = new DummyObj();
 		HttpClient httpclient = new DefaultHttpClient();
 		long second = System.nanoTime() - first;
 	//	File file = new File(Environment.getExternalStorageDirectory(),
 		//		filePath);
 		File file = new File(filePath);
 		HttpPost httppost = new HttpPost(
 				"http://vm-6114.idi.ntnu.no:1337/SpeechServer/sst");
 		final TelephonyManager tm = (TelephonyManager) context
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		String t_id = tm.getDeviceId();
 		String tmp = "TABuss";
 		String p_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
 
 		try
 		{
 			MultipartEntity entity = new MultipartEntity();
			entity.addPart("speechinput", new FileBody(file, "multipart/form-data;charset=\"UTF-8\""));
 			entity.addPart("lat",new StringBody(String.valueOf(lat)));
 			entity.addPart("lon",new StringBody(String.valueOf(lon)));
 			entity.addPart("devID",new StringBody(tmp+p_id));
 			httppost.setEntity(entity);
 			response = EntityUtils.toString(httpclient.execute(httppost)
 					.getEntity(), "UTF-8");
 			System.out.println("RESPONSE: " + response);
 			dummy = calc.parse(response);
 		} catch (ClientProtocolException e)
 		{
 		} catch (IOException e)
 		{
 		}
 		return dummy;
 
 	}
 	public StringBuffer executeHttpGet(String path) throws Exception
 	{
 		BufferedReader in = null;
 		StringBuffer sb = null;
 		try
 		{
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet();
 			request.setURI(new URI(path));
 			HttpResponse response = client.execute(request);
 			in = new BufferedReader(new InputStreamReader(response.getEntity()
 					.getContent(), "ISO8859-1"));
 			 sb = new StringBuffer();
 			String line = "";
 			String NL = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null)
 			{
 				sb.append(line + NL);
 			}
 		} finally
 		{
 			if (in != null)
 			{
 				try
 				{
 					System.out.println("STR: " + sb.length());
 					in.close();
 					return sb;
 					
 				} catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 }
