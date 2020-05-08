 package org.internetrt.sdk.util;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 
 public class HttpHelper {
 	public static String generatorParamString(Pair[] parameters) {
 		StringBuffer params = new StringBuffer();
 		String split = "";
 		
 		if (parameters != null) {
 			for (Pair pair : parameters) {
 				params.append(split);
 				split = "&";
 				
 				String name = pair._1();
 				String value = pair._2();
 				params.append(name + "=");
 				try {
 					params.append(URLEncoder.encode(value, "UTF-8"));
 				} catch (UnsupportedEncodingException e) {
 					throw new RuntimeException(e.getMessage(), e);
 				} catch (Exception e) {
 					String message = String.format("'%s'='%s'", name, value);
 					throw new RuntimeException(message, e);
 				}
 			}
 		}
 		return params.toString();
 	}
 	public static String generatorParamString(Map<String, String> parameters) {
 		if (parameters != null) {
 			Pair[] pairs = new Pair[parameters.size()];
 			int i =0;
 			for(Entry<String,String> entry :parameters.entrySet()){
 				pairs[i] = new Pair(entry.getKey(), entry.getValue());
 				i++;
 			}
 			return generatorParamString(pairs);
 		}
 		else
 			return "";
 	}
 
 	public static String httpClientGet(String requestUrl) {
 		byte[] responseBody = null;
 		HttpClient httpClient = new HttpClient();
 		GetMethod getMethod = new GetMethod(requestUrl);
 		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler());
 		try {
 			int statusCode = httpClient.executeMethod(getMethod);
 			if (statusCode != HttpStatus.SC_OK) {
 				System.err.println("Method failed: "
 						+ getMethod.getStatusLine());
 			}
 			responseBody = getMethod.getResponseBody();
 
 		} catch (HttpException e) {
 			System.out.println("Please check your provided http address!");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			getMethod.releaseConnection();
 		}
 		String result = new String(responseBody);
 		return result;
 	}
 
 	public static String httpClientPost(String url, NameValuePair[] data)
 	{
 		String response = null;
 		HttpClient client = new HttpClient();
 		PostMethod method = new PostMethod (url);
 		method.setRequestBody(data);
 		try{
 			client.executeMethod(method);
 			if(method.getStatusCode()== HttpStatus.SC_OK){
 				response = method.getResponseBodyAsString();
 			}
 		}catch (IOException e) {
 			// TODO: handle exception
			System.out.println("Exception happened when sending Http Post to:"+url+"\n"+e);
 		}finally{
 			method.releaseConnection();
 		}
 		
 		return response;
 	}
 }
