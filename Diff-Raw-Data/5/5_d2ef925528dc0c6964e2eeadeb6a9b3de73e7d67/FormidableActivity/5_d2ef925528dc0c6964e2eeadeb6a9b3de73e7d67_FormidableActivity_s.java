 package com.example.formidable;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
 
 public class FormidableActivity extends Activity {
 	
 	static {
 	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
 	}
 
 	//private static final String TAG = "MainActivity";
 	private EventSource eventSource;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         initialize();       
         RecordRepository repository = eventSource.getRepository();
         
         String recordId = newId();     
         repository.put(createSimpleEvent(3, recordId, "surname:Bhuwalka"));     
         repository.put(createSimpleEvent(2, recordId, "name:Angshu"));
         repository.put(createSimpleEvent(1, recordId, "name:Chris"));
         
         //check search functionality
         repository.put(createSimpleEvent(1, newId(), "name:Vivek", "surname:Singh"));
         		
 		Map<String, Object> record = repository.get(recordId);
 		System.out.println(String.format("Name: %s %s", record.get("name"), record.get("surname")));
         
         initializeView();
         eventSource.getSearchAgent().triggerSearch("name:vivek AND surname:singh", new SearchCallback() {
 			@Override
 			void onSearchSuccess(JSONObject[] results) {
 				for (JSONObject result : results) {
 					//we should probably create something Event.parse(JSOBObject) to get a Event object out of json
 					//and return an array of Event Objects. parse method can extract attributes like
 					//epoch, docid, revid, and all the elements in the data element recursively
 					System.out.println("search result record => %s".format(result.toString()));
 				}
 			}
 			
 			@Override
 			void onSearchError(Object resp) {
 				System.out.println("SearchAgent.triggerSearch => Error : " + resp.toString());
 			}
 		});
         
     }
 
 	private void initializeView() {
 		setContentView(R.layout.activity_main);
         WebView browser = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = browser.getSettings();
         webSettings.setJavaScriptEnabled(true);
         webSettings.setDatabaseEnabled(true);
         webSettings.setDomStorageEnabled(true);
         webSettings.setGeolocationDatabasePath(Configuration.getGeolocDbPath());
         webSettings.setGeolocationEnabled(true);
         
         browser.addJavascriptInterface(new JSRepoHandler(eventSource.getRepository()), "_eventRepo");
         browser.loadUrl(formateUrl("new_event.html"));
         //browser.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
 	}
 
 	private void initialize() {
 		this.eventSource = new EventSource(this);
 	}
 
 	private Event createSimpleEvent(int epoch, String recordId, String... arguments) {
         Map<String, Object> map = new HashMap<String, Object>();
         for (String arg : arguments) {
         	String[] parts = arg.split(":");
         	map.put(parts[0], parts[1]);
         }
 		return new Event(epoch, recordId, map);		
 	}
 
 		
 
 	private String newId() {
 		return UUID.randomUUID().toString();
 	}
 	
 	
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     
     private String formateUrl(String url) {
 		return "file:///android_asset/" + url;
 	}
     
     
     public class JSRepoHandler {
    	private RecordRepository repository;
 
 		public JSRepoHandler(RecordRepository repository) {
 			this.repository = repository;
    	}
 		
 		public void addEvent(String eventProps) {
 			//System.out.println("***** eventProps = " + eventProps);
 			try {
 				JSONObject json = new JSONObject(eventProps);
 				String epoch = json.optString("epoch");
 				String recordId = json.optString("recordId");
 				if (!validStr(epoch)) {
 					epoch = "1";
 				}
 				if (!validStr(recordId)) {
 					recordId = newId();
 				}
 				
 				Event event = new Event(Integer.valueOf(epoch), recordId);
 				
 				JSONArray names = json.names();
 				for (int i=0; i<names.length(); i++) {
 					String key = (String) names.get(i);
 					String value = (String) json.get(key);
 					event.addAttribute(key, value);
 					//System.out.println(String.format("%s = %s", key, value));
 				}
 				repository.put(event);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}		
 		
     }
     
     private boolean validStr(String value) {
 		return (value != null) && (!"".equals(value));
 	}
     
     
     
     	
 }
