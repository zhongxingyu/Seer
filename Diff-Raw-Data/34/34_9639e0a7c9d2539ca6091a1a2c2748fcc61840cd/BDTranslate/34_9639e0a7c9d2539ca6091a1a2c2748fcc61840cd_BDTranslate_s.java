 /**
  * @Title: BDTranslate.java
  * @Package org.mariotaku.twidere.extension.twitlonger
  * @Description: TODO(调用百度translate API)
  * @author shangjiyu@gmail.com
  * @date 2013-9-21 下午1:13:41
  * @version V1.0
  */
 
 package org.shangjiyu.twidere.extension.translator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * @ClassName: BDTranslate
  * @Description: TODO(发送POST请求，返回请求结果)
  * @author shangjiyu
  * @date 2013-9-21 下午1:13:41
  *
  */
 
 public class BDTranslate implements Constants {
 
	private static final String BDTRANSLATEKEY_STRING = Constants.BAIDU_CLIENT_ID;
 	private static final String BDTRANSLATEURL_STRING = Constants.BDTRANSLATEURL_STRING;
 	private static final Pattern PATTERN_LINK = Pattern.compile(Constants.NONEED2TRANSLAETPORTION, Pattern.CASE_INSENSITIVE);
 	private static final Pattern PATTERN_ALPHA = Pattern.compile("_____|_ _ _ _ _");
 	private final ArrayList<String> linkStrings = new ArrayList<String>();
 	private int uneed2TranslateElementIndex = 0;
 	
 	public BDTranslate() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public BDTranslateResponse postTranslate(String srcContent) throws BDTranslateException, IllegalStateException, JSONException {
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
 			queryString = PATTERN_LINK.matcher(srcContent).replaceAll("\r\n_____\r\n");
 			final HttpClient httpclient = new DefaultHttpClient();
 			final HttpPost httpPost = new HttpPost(BDTRANSLATEURL_STRING);
 			final ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 			args.add(new BasicNameValuePair("client_id", BDTRANSLATEKEY_STRING));
 			args.add(new BasicNameValuePair("from", "auto"));
 			args.add(new BasicNameValuePair("to", "auto"));
 			args.add(new BasicNameValuePair("q", queryString));
 			httpPost.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));
 			final HttpResponse response = httpclient.execute(httpPost);
 			return parseBDTranslateResponse(EntityUtils.toString(response.getEntity()));
 		} catch (final IOException e) {
 			throw new BDTranslateException(e);
 		}
 	}
 	
 	/**
 	 * @Title: parseTranslateResponse
 	 * @Description: TODO(解析百度返回的json数据)
 	 * @param @param response
 	 * @param @return
 	 * @param @throws JSONException    设定文件
 	 * @return TranslateResponse    返回类型
 	 * @throws
 	 */
 	public BDTranslateResponse parseBDTranslateResponse(String response) throws JSONException {
 		String from = null, to = null, translateResult = "";
 		JSONObject translateResponse = new JSONObject(response);
 		from = translateResponse.getString("from");
 		to = translateResponse.getString("to");
 		if (!translateResponse.isNull("error_code")) {
 			return new BDTranslateResponse(from, to, translateResponse.getString("error_message"));
 		}
 		JSONArray resultArray = translateResponse.getJSONArray("trans_result");
 		for (int i = 0; i < resultArray.length(); i++) {
 			translateResult += resultArray.getJSONObject(i).getString("dst")+"\r\n";
 		}
 		translateResult = replaceURL(PATTERN_ALPHA, translateResult);
 		translateResult = translateResult.replaceAll("\\r\\n", " ");
 		return new BDTranslateResponse(from, to, translateResult);
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
 	
 	public static class BDTranslateException extends Exception {
 		
 		private static final long serialVersionUID = 1020016463204999157L;
 		
 		public BDTranslateException() {
 			super();
 		}
 		
 		public BDTranslateException(String detailMsg) {
 			super(detailMsg);
 		}
 		
 		public BDTranslateException(Throwable throwable) {
 			super(throwable);
 		}
 		
 		public BDTranslateException(String detailMsg, Throwable throwable) {
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
 	public static class BDTranslateResponse {
 		public final String from, to, translateResult;
 
 		private BDTranslateResponse(String from, String to, String translateResult) {
 			this.from = from;
 			this.to = to;
 			this.translateResult = translateResult;
 		}
 	}
 }
 
