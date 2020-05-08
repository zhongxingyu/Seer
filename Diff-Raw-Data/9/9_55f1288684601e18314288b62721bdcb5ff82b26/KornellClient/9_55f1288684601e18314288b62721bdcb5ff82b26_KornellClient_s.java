 package kornell.api.client;
 
 import kornell.api.client.data.Person;
 import kornell.core.shared.data.CourseTO;
 import kornell.core.shared.data.CoursesTO;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.storage.client.Storage;
 
 public class KornellClient {
 	Storage store = Storage.getLocalStorageIfSupported();
 
 	private String apiURL = null;
 	private Person currentUser;	
 
 	private KornellClient() {
 		discoverApiUrl();
 	}
 
 	private void discoverApiUrl() {
 		apiURL = KornellClient.getFromEnvironment();
 		if(apiURL == null || apiURL.length() == 0){ 
 			useDefaultUrl();
 		}else{
 			GWT.log("API url already discovered");
		}		
 	}
 
 	
 
 	private static native String getFromEnvironment() /*-{
	  console.debug($wnd.KornellConfig.apiEndpoint);
	  return ""; 
 	}-*/;
 
 	private void useDefaultUrl() {
 		apiURL = "http://localhost:8080";
 	}
 
 	public void login(
 			String username,
 			String password,
 			final Callback callback) {
 		final String auth = "Basic "+KornellClient.encode(username,password);				
 		
 		Callback wrapper = new Callback(){
 			protected void ok(Person person) {
 				setCurrentUser(person);
 				//TODO: https://github.com/Craftware/Kornell/issues/7
 				storeAuth(auth);
 				callback.ok(person);
 			}
 			
 			private void storeAuth(String auth) {				  				  
 				  if(store != null){
 					  store.setItem("Authorization", auth);
 				  }else GWT.log("Ooops, your browser does not support local storage");
 			}
 
 			@Override
 			protected void unauthorized() {
 				callback.unauthorized();
 			}
 		};
 				
 		createGET("/user")
 			.addHeader("Authorization", auth)
 			.sendRequest(null, wrapper);
 
 	}
 	
 	public void getCourses(Callback<CoursesTO> callback){
 		createGET("/courses").sendRequest(null, callback);	
 	}
 
 	
 
 	private void setCurrentUser(Person person) {
 		this.currentUser = person;
 		
 	};
 	
 	public void getCurrentUser(Callback cb){
 		createGET("/user").sendRequest(null, cb);	
 	}
 	
 	public void getCourseTO(String uuid, Callback<CourseTO> cb) {
 		createGET("/courses/"+uuid).sendRequest(null, cb);
 		
 	}
 
 	private ExceptionalRequestBuilder createGET(String path) {
 		
 		ExceptionalRequestBuilder reqBuilder =
 				new ExceptionalRequestBuilder(RequestBuilder.GET, getApiUrl()+path);
 		if(store != null){
 			String auth = store.getItem("Authorization");
 			if(auth != null && auth.length()>0)
 				reqBuilder.setHeader("Authorization", auth);
 		}
 		return  reqBuilder;
 	}
 	
 	
 	
 	
 	private String getApiUrl() {
 		while(apiURL == null){
 			GWT.log("Could not find API URL. Looking up again");
 			discoverApiUrl();
 		}
 		return apiURL;
 	}
 
 	private static String encode(String username, String password) {
 		return KornellClient.base64Encode(username+":"+password);
 	}
 
 	private static native String base64Encode(String plain) /*-{
 	  return window.btoa(plain);
 	}-*/;
 
 	public static KornellClient getInstance() {		
 		return new KornellClient();
 	}
 
 	
 	
 
 
 }	
