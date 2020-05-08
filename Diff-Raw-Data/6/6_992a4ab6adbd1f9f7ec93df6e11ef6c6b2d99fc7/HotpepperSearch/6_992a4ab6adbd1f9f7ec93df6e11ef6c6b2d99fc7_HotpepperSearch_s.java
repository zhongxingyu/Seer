 package jp.mumoshu.webapi.hotpepper;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import jp.mumoshu.json.JsonHttp;
 import jp.mumoshu.maps.BasicMap;
 
 import org.json.JSONException;
 
 import android.net.Uri;
 import android.net.Uri.Builder;
 
 public class HotpepperSearch {
 	String API_URL = "http://webservice.recruit.co.jp/hotpepper/gourmet/v1/";
 	HotpepperCondition condition;
 	private String apiKey;
 
 	public HotpepperSearch(HotpepperCondition condition, String apiKey){
 		this.condition = condition;
 		this.apiKey = apiKey;
 	}
 	public Uri requestUri(){
 		Builder b = new Builder();
		b.appendPath(API_URL);
		b.appendQueryParameter("key", apiKey);
 		condition.appendQueryParametersTo(b);
 		return b.build();
 	}
 	public String requestUriString(){
 		return requestUri().toString();
 	}
 	public URL requestURL() throws MalformedURLException{
 		return new URL(requestUriString());
 	}
 	public HotpepperData execute() throws MalformedURLException, IOException, JSONException{
 		return HotpepperData.fromJsonObject(JsonHttp.get(requestURL()));
 	}
 	public HotpepperSearch nextPage(){
 		return new HotpepperSearch(condition.nextPage(), apiKey);
 	}
 	public static HotpepperSearch fromMap(BasicMap map, String apiKey){
 		return new HotpepperSearch(HotpepperCondition.fromMap(map), apiKey);
 	}
 }
