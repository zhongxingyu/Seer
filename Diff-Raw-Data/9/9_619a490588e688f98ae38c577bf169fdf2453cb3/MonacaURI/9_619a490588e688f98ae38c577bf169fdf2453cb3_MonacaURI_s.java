 package mobi.monaca.framework;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mobi.monaca.framework.util.MyLog;
 
 import org.json.JSONObject;
 
 /**
  * URI that contains queryData as ArrayList and has script inserted html getter.
  * also able to convert query containing uri into noQueryURI
  */
 
 public class MonacaURI {
 	public static final String URL_ENCODE = "UTF-8";
 	private static final String TAG = MonacaURI.class.getSimpleName();
 	private URI originalUri;
 	private ArrayList<QueryParam> queryParamsArrayList;
 
 	private boolean hasUnusedFragment;
 
 	public MonacaURI(String url) {
 		try {
 			this.originalUri = new URI(url);
 			this.parseQuery();
 
 			hasUnusedFragment = (originalUri.getFragment() != null);
 		} catch (URISyntaxException e) {
 			MyLog.e(TAG, "URISyntacException! : " + url);
 		}
 	}
 
 	/**
 	 * this is for processing fragment in push transition.
 	 *  Not checks whether there is a fragment. only unused fragment is checked
 	 * @return
 	 */
 	public boolean hasUnusedFragment() {
 		return hasUnusedFragment;
 	}
 
 	/**
 	 * getFragment and set this fragment USED.
 	 * this is for processing fragment in push transition
 	 * @return
 	 */
 	public String popFragment() {
 		if (hasUnusedFragment) {
 			hasUnusedFragment = false;
 			return originalUri.getFragment();
 		} else {
 			return null;
 		}
 	}
 
 	public static String buildUrlWithQuery(String baseUrl, JSONObject queryJson) {
 
 		MyLog.d(TAG, "buildUrl :" + baseUrl);
 		if (queryJson == null || (queryJson != null && queryJson.length() == 0)) {
 			MyLog.d(TAG, "no query");
 			return baseUrl;
 		}
 
 		Iterator<?> iterator = queryJson.keys();
 		String newUrl = new String(baseUrl);
 		String key;
 
 		try {
 			if (new URI(baseUrl).getQuery() != null) {
 				// if already baseUrl has queryParams,url building starts with &
 				newUrl += "&";
 			} else {
 				newUrl += "?";
 			}
 		} catch (URISyntaxException e) {
 			return baseUrl;
 		}
 
 		while (iterator.hasNext()) {
 			key = (String)iterator.next();
 			try {
 				if (key != null && queryJson.isNull(key)) {
 					newUrl += URLEncoder.encode(key, URL_ENCODE).replace(".", "%2e") + "&";
 				} else if (key != null ) {
 					//URLEncoder.encode does not encode dot, so replace manually (String#replace(".", "%2e"))
 					newUrl += URLEncoder.encode(key, URL_ENCODE).replace(".", "%2e") + "="
 							+ URLEncoder.encode(queryJson.optString(key), URL_ENCODE).replace(".", "%2e")  + "&";
 				}
 			} catch (UnsupportedEncodingException e) {
 				MyLog.e(TAG, e.getMessage());
 			}
 		}
 		// remove last &
 		newUrl = trimLastChar(newUrl);
 
 		return newUrl;
 	}
 
 	public String getOriginalUrl() {
 		return originalUri.toString();
 	}
 
 	public String getUrlWithoutOptions() {
 		if (originalUri.getRawQuery() == null && originalUri.getFragment() == null) {
 			return getOriginalUrl();
 		}else {
 			String url =originalUri.toString().replaceFirst("(#" + originalUri.getFragment() + ")$", "");
			url = url.toString().replaceFirst("(\\?" + originalUri.getRawQuery() + ")$", "");
 			return url;
 		}
 	}
 
 	public boolean hasQueryParams() {
 		return queryParamsArrayList != null && !queryParamsArrayList.isEmpty();
 	}
 
 	public static String trimLastChar(String target) {
 		StringBuffer sb = new StringBuffer(target);
 		sb.deleteCharAt(target.length() - 1);
 		target = sb.toString();
 		return target;
 	}
 
 	public static String removeSpecialChar(String target) {
 		String result;
 		//MyLog.d(TAG,"removeSpecialChar");
 		result = target.replace("\\","\\\\").replace("\"", "\\\\\"").replace("\'", "\\\\\'").replace("/", "\\/").replace("}", "\\}");
 		MyLog.d(TAG, target);
 		MyLog.d(TAG, result);
 		return result;
 	}
 
 	public String getQueryParamsContainingHtml(String baseHtml) {
 		String paramsString;
 
 		paramsString = "<script type=\"text/javascript\">"
 				+ "window.monaca = window.monaca || {};"
 				+ "monaca.queryParams = monaca.queryParams || {";
 
 		for (QueryParam q : this.queryParamsArrayList) {
 			String script;
 			if (q.hasValue()) {
 				script = "\"" + removeSpecialChar(q.getDecodedKey()) + "\":"  + "\"" + removeSpecialChar(q.getDecodedValue()) + "\",";
 			} else {
 				script = "\"" + removeSpecialChar(q.getDecodedKey()) + "\":" + "null,";
 			}
 			paramsString += script;
 		}
 
 		paramsString = trimLastChar(paramsString);
 		paramsString += "};</script>";
 
 		String targetHtml = new String(baseHtml);
 		Pattern pattern = Pattern.compile("<head.*?>", Pattern.CASE_INSENSITIVE);
 		Matcher matcher = pattern.matcher(targetHtml);
 
 		if (matcher.find()) {
 		//	MyLog.d(TAG, "matches");
 			return matcher.replaceFirst(matcher.group() + paramsString);
 		}else {
 		//	MyLog.d(TAG, "no matches");
 			return paramsString + targetHtml;
 		}
 	}
 
 	public void parseQuery() {
 		if (originalUri.getRawQuery() != null) {
 			//MyLog.d(TAG, "hasQuery");
 			MyLog.d(TAG, "rawQuery:"+ originalUri.getRawQuery());
 			String[] params = originalUri.getRawQuery().split("&");
 			queryParamsArrayList = new ArrayList<QueryParam>();
 
 			for (int i = 0; i < params.length; i++) {
 				QueryParam p = new QueryParam(params[i]);
 				if (!p.isEmpty()) {
 					queryParamsArrayList.add(p);
 				}
 			}
 		} else {
 			//MyLog.d(TAG, "noQuery");
 			queryParamsArrayList = null;
 		}
 	}
 
 	public class QueryParam {
 		private String key;
 		private String value;
 
 		public QueryParam(String baseParam) {
 			String[] keyAndValue = baseParam.split("=");
 			if (keyAndValue == null || keyAndValue.length < 2) {
 				MyLog.d(TAG, "length < 2");
 				this.key = baseParam;
 				this.value = null;
 
 				if (key.equals("")) {
 					key = null;
 				}
 			} else {
 				this.key = keyAndValue[0];
 				this.value = keyAndValue[1];
 			}
 		}
 
 		public boolean hasValue() {
 			return (value != null);
 		}
 
 		public boolean isEmpty() {
 			return (key == null && value == null);
 		}
 
 		public String getDecodedKey() {
 			try {
 				return URLDecoder.decode(key, MonacaURI.URL_ENCODE);
 			} catch (UnsupportedEncodingException e) {
 				return key;
 			}
 		}
 
 		public String getDecodedValue() {
 			try {
 				return URLDecoder.decode(value, MonacaURI.URL_ENCODE);
 			} catch (UnsupportedEncodingException e) {
 				return value;
 			}
 		}
 	}
 
 }
