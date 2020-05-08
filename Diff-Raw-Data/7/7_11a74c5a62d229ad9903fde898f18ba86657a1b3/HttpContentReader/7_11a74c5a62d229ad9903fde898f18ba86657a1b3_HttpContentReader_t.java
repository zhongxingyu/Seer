 /**
  * Copyright (c) 2005-2008 CancerGrid Consortium <http://www.cancergrid.org/>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of 
  * this software and associated documentation files (the "Software"), to deal in the 
  * Software without restriction, including without limitation the rights to use, copy, 
  * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,  
  * and to permit persons to whom the Software is furnished to do so, subject to the 
  * following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies 
  * or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
  * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
  * DEALINGS IN THE SOFTWARE.
  */
 
 package org.cancergrid.ws.util;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.log4j.Logger;
 
 /**
  * Utility for reading HTML content from a given URL. Uses Apache HttpClient.
  * 
  * @author <a href="mailto:Andrew.Tsui@comlab.ox.ac.uk">Andrew Tsui</a> (<a
  *         href="http://www.cancergrid.org">CancerGrid Consortium</a>)
  * @version 1.0
  */
 public class HttpContentReader
 {
 	/**
 	 * Log4J Logger
 	 */
 	private static Logger LOG = Logger.getLogger(HttpContentReader.class);
 
 	public static String getHttpContent(String httpUrl)
 	{
 		if (httpUrl.contains("&amp;"))
 		{
 			httpUrl = httpUrl.replace("&amp;", "&");
 		}
 		if (httpUrl.contains("?"))
 		{
 			return getHttpContent(httpUrl.substring(0, httpUrl.indexOf("?")), httpUrl.substring(httpUrl.indexOf("?")+1), Method.GET);
 		} 
 		else
 		{
 			return getHttpContent(httpUrl, null, Method.GET);
 		}
 	}
 
 	public static String getHttpContent(String httpUrl, String query, Method method)
 	{
 		LOG.debug("getHttpContent(httpUrl): " + httpUrl);
 		LOG.debug("getHttpContent(query): " + query);
 		LOG.debug("getHttpContent(method): " + method);
 		
 		HttpMethod httpMethod = null;
 		if (httpUrl.contains("&amp;"))
 		{
 			httpUrl = httpUrl.replace("&amp;", "&");
 		}
 		
 		if (query != null && query.length() > 0 && query.startsWith("?") && query.contains("&amp;"))
 		{
 			query = query.replace("&amp;", "&");
 		}
 
 		try
 		{
			//LOG.debug("Querying: " + httpUrl);
 			if (method == Method.GET)
 			{
 				httpMethod = new GetMethod(httpUrl);
 				if (query != null && query.length() > 0)
 				{
                    httpMethod.setQueryString(query);
 				}
 			} else if (method == Method.POST)
 			{
 				httpMethod = new PostMethod(httpUrl);
 				if (query != null && query.length() > 0)
 				{
 					RequestEntity entity = new StringRequestEntity(query, "text/xml", "UTF-8");
 					((PostMethod)httpMethod).setRequestEntity(entity);
 				}
 			}
 
 			httpMethod.setFollowRedirects(true);
 		
 			httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
 
 			Protocol.registerProtocol("https", new Protocol("https", new org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory(), 443));
 			HttpClient client = new HttpClient();
 			int statusCode = client.executeMethod(httpMethod);
 			if (statusCode != HttpStatus.SC_OK)
 			{
 				LOG.error("Method failed: " + httpMethod.getStatusLine());
 				LOG.error("Error querying: " + httpMethod.getURI().toString());
 				throw new Exception("Method failed: " + httpMethod.getStatusLine());
 			}
 			
 			byte[] responseBody = httpMethod.getResponseBody();
 			return new String(responseBody, "UTF-8");
 		}
 		catch (HttpException e)
 		{
 			LOG.error("Fatal protocol violation: " + e.getMessage());
     	}
 		catch (IOException e)
 		{
 			LOG.error("Fatal transport error: " + e.getMessage());
 		} 
 		catch (Exception e)
 		{
 			LOG.error(e.getMessage());
 		} 
 		finally
 		{
 			httpMethod.releaseConnection();
 		}
 		return null;
 	}
 	
 	public static enum Method {GET, POST};
 }
