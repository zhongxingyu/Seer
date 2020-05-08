 /**
  * @Title: MSTranslate.java
  * @Package org.mariotaku.twidere.extension.twitlonger
  * @Description: TODO(调用巨硬translate API)
  * @author shangjiyu@gmail.com
  * @date 2013-9-21 下午1:13:41
  * @version V1.0
  */
 
 package org.shangjiyu.twidere.extension.translator;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 /**
  * @ClassName: MSTranslate
  * @Description: TODO(发送POST请求，返回请求结果)
  * @author shangjiyu
  * @date 2013-9-21 下午1:13:41
  *
  */
 
 public class MSTranslate implements Constants {
 
	protected static String CLIEND_ID_STRING = Constants.MICROSOFT_CLIENT_ID;
	protected static String CLIEND_SECRET_STRING = Constants.MICROSOFT_CLIEN_SECRET;
 	private static final String SCOPE_STRING = Constants.SCOPE_STRING;
 	private static final String GRANT_TYPE_STRING = Constants.GRANT_TYPE_STRING;
 	private static final String DATAMARKETACCESSURL_STRING = Constants.DATAMARKETACCESSURL_STRING;
 	private static final String MSTRANSLATEURL_STRING = Constants.MSTRANSLATEURL_STRING;
 	private static final Pattern PATTERN_LINK = Pattern.compile(Constants.NONEED2TRANSLAETPORTION, Pattern.CASE_INSENSITIVE);
 	private static final Pattern PATTERN_ALPHA = Pattern.compile("(777)");
 	private final ArrayList<String> linkStrings = new ArrayList<String>();
 	private String ACCESS_TOKEN_STRING = null;
 	private int uneed2TranslateElementIndex = 0;
 	
 	public MSTranslate() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MSTranslateResponse postTranslate(String srcContent, String to) throws MSTranslateException, IllegalStateException, JSONException {
 		try {
 			String queryString = "";
 			final Matcher matcher = PATTERN_LINK.matcher(srcContent);
 			while (matcher.find()) {
 				if (matcher.group(1) != null) {
 					linkStrings.add(matcher.group(1));
 				}else if (matcher.group(5) != null) {
 					linkStrings.add(matcher.group(5));
 				}else if (matcher.group(12) != null) {
 					linkStrings.add(matcher.group(12));
 				}
 			}
 			queryString = PATTERN_LINK.matcher(srcContent).replaceAll("777");
 			final String srcString = queryString;
 			queryString = URLEncoder.encode(queryString,"UTF-8");
 			if (ACCESS_TOKEN_STRING == null) {
 				ACCESS_TOKEN_STRING = getAccessToken();
 			}
 			final String getURL = MSTRANSLATEURL_STRING+"?"+"appid="+"&text="+queryString+"&to="+to+"&contentType=text/plain";
 //			final HttpClient httpclient = new DefaultHttpClient();
 			final HttpClient httpclient = getNewHttpClient();
 			final HttpGet httpGet = new HttpGet();
 			httpGet.setURI(new URI(getURL));
 			httpGet.setHeader("Authorization", "Bearer "+ACCESS_TOKEN_STRING);
 			final HttpResponse response = httpclient.execute(httpGet);
 			return parseTranslateResponse(srcString,EntityUtils.toString(response.getEntity()));
 		} catch ( Exception e) {
 			throw new MSTranslateException(e);
 		}
 	}
 	
 	
 	/**
 	 * @Title: getAccessToken
 	 * @Description: TODO(获取巨硬data商店的accesstoken)
 	 * @param @return
 	 * @param @throws MSTranslateException
 	 * @param @throws ParseException
 	 * @param @throws JSONException    设定文件
 	 * @return String    返回类型
 	 * @throws
 	 */
 	public String getAccessToken() throws MSTranslateException, ParseException, JSONException {
 		try {
 //			final HttpClient httpclient = new DefaultHttpClient();
 			final HttpClient httpclient = getNewHttpClient();
 			final HttpPost httpPost = new HttpPost(DATAMARKETACCESSURL_STRING);
 			final ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 			args.add(new BasicNameValuePair("client_id", CLIEND_ID_STRING));
 			args.add(new BasicNameValuePair("client_secret", CLIEND_SECRET_STRING));
 			args.add(new BasicNameValuePair("scope", SCOPE_STRING));
 			args.add(new BasicNameValuePair("grant_type", GRANT_TYPE_STRING));
 			httpPost.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));
 			final HttpResponse response = httpclient.execute(httpPost);
 			JSONObject responseJsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
 			return responseJsonObject.getString("access_token");
 		} catch (final IOException e) {
 			// TODO: handle exception
 			throw new MSTranslateException(e);
 		}
 		
 	}
 	
 	/**
 	 * @throws MSTranslateException 
 	 * @throws IllegalStateException 
 	 * @throws IOException 
 	 * @throws XmlPullParserException 
 	 * @Title: parseTranslateResponse
 	 * @Description: TODO(解析巨硬返回的XML数据)
 	 * @param @param response
 	 * @param @throws JSONException    设定文件
 	 * @return TranslateResponse    返回类型
 	 * @throws
 	 */
 	public MSTranslateResponse parseTranslateResponse(String src,String response) throws JSONException, XmlPullParserException, IOException, IllegalStateException, MSTranslateException {
 		String from = "yangpi", to = "chinese", translateResult = "";
 		final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		final XmlPullParser parser = factory.newPullParser();
 		parser.setInput(new StringReader(response));
 		int eventType = parser.getEventType();
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			 if(eventType == XmlPullParser.TEXT) {
 	        	  translateResult = parser.getText();
 	          }
 			eventType = parser.next();
 		}
 		if (src.equals(translateResult)) {
 			return postTranslate(src, "en");
 		}else {
 			translateResult = replaceURL(PATTERN_ALPHA, translateResult);
 			return new MSTranslateResponse(from, to, translateResult);
 		}
 	}
 	
 	public String replaceURL(Pattern pattern,String toReplaceString) {
 		final Matcher matcher = pattern.matcher(toReplaceString);
 		if (matcher.find()) {
 			toReplaceString = matcher.replaceFirst(linkStrings.get(uneed2TranslateElementIndex));
 			uneed2TranslateElementIndex++;
 			return replaceURL(pattern, toReplaceString);
 		}else {
 			return toReplaceString;
 		}
 	}
 	
 	public static class MSTranslateException extends Exception {
 		
 		private static final long serialVersionUID = 1020016463204999157L;
 		
 		public MSTranslateException() {
 			super();
 		}
 		
 		public MSTranslateException(String detailMsg) {
 			super(detailMsg);
 		}
 		
 		public MSTranslateException(Throwable throwable) {
 			super(throwable);
 		}
 		
 		public MSTranslateException(String detailMsg, Throwable throwable) {
 			super(detailMsg, throwable);
 		}
 	}
 	
 	/**
 	 * @ClassName: TranslateResponse
 	 * @Description: TODO(翻译结果对象)
 	 * @author shangjiyu
 	 * @date 2013-9-21 下午4:18:58
 	 *
 	 */
 	public static class MSTranslateResponse {
 		public final String from, to, translateResult;
 
 		private MSTranslateResponse(String from, String to, String translateResult) {
 			this.from = from;
 			this.to = to;
 			this.translateResult = translateResult;
 		}
 	}
 	
 	/********************************************************
 	 * @Title: getNewHttpClient
 	 * @Description: TODO(get self httpClient that trust all of servers)
 	 * @param @return    设定文件
 	 * @return HttpClient    返回类型
 	 * @throws
 	 */
 	public static HttpClient getNewHttpClient() {
 		   try {
 		       KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
 		       trustStore.load(null, null);
 
 		       SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
 		       sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
 		       HttpParams params = new BasicHttpParams();
 		       HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		       HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 
 		       SchemeRegistry registry = new SchemeRegistry();
 		       registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		       registry.register(new Scheme("https", sf, 443));
 
 		       ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
 
 		       return new DefaultHttpClient(ccm, params);
 		   } catch (Exception e) {
 		       return new DefaultHttpClient();
 		   }
 		} 
 }
 
