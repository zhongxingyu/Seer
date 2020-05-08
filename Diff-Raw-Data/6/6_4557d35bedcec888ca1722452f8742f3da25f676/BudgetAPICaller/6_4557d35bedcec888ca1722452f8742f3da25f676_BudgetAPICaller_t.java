 package org.obudget.client;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.jsonp.client.JsonpRequestBuilder;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 class BudgetAPICaller extends JsonpRequestBuilder {
 	private UrlBuilder url;
 
 	public BudgetAPICaller() {
 		url = new UrlBuilder();
		url.setHost(Window.Location.getHost());
		url.setPort(Integer.parseInt( Window.Location.getPort() ));
 		url.setPath("00");
 	}
 	
 	public void setCode( String code ) {
 		url.setPath(code);
 	}
 
 	public void setParameter( String key, String value ) {
 		url.setParameter(key, value);
 	}
 	
 	public void go( final BudgetAPICallback callback ) {
 		requestObject(url.buildString(), new AsyncCallback<JavaScriptObject>() {
 			@Override
 			public void onSuccess(JavaScriptObject result) {
 				JSONArray array = new JSONArray(result);
 				callback.onSuccess(array);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert("Failed to access API: "+caught.getMessage());
 			}
 			
 		});		
 	}
 }
