 package com.imrd.copy.translate;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import android.util.Log;
 
 import com.google.gson.GsonBuilder;
 import com.imrd.copy.dict.StarDict;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 public class TranslateClient {
 
 	public static final String TAG = TranslateClient.class.getSimpleName();
 	
 	private static TranslateClient client;
 	
 	
 	static final String GOOGLE_API = "http://translate.google.com/translate_a/t?client=j&text=%s&hl=en&sl=%s&tl=%s";
 	static final String TW = "zh-TW";
 	static final String EN = "en";
 	
 	public interface TranslateAware {
 		public void receiveTranslateText(Object transobj);
 	}
 	
 	private TranslateClient() {
 	}
 	
 	public static TranslateClient newInstance(){
 		if(client==null){
 			client = new TranslateClient();
 		}
 		return client;
 	}
 	
 	public void requestTranslate(String text, final TranslateAware callback){
 		
 		try {
 			String urlStr = String.format(GOOGLE_API,
 					URLEncoder.encode(text, "utf-8"), EN, TW);
 			
 			AsyncHttpClient client = new AsyncHttpClient();
 			client.get(urlStr, new AsyncHttpResponseHandler() {
 			    @Override
 			    public void onSuccess(String response) {
 			    	//Log.i(TAG, response);
 			    	
 			    	//GTStruct struct = new GsonBuilder().create().fromJson(response, GTStruct.class);
 			    	//Log.i(TAG, "output: "+struct.sentences.get(0).trans);
 			    	
 			    	Google fromJson = new GsonBuilder().create().fromJson(response, Google.class);
 			    	
 			    	callback.receiveTranslateText(fromJson);
 			    	
 			    }
 			});
 			
 		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "bad request: "+e.getMessage());
			requestTranslateLocal(text, callback);
 		}
 		
 	}
 	
 	public void requestTranslateLocal(String text, final TranslateAware callback){
 		
 		callback.receiveTranslateText(new StarDict().getExplanation2(text));
 		
 	}
 	
 }
