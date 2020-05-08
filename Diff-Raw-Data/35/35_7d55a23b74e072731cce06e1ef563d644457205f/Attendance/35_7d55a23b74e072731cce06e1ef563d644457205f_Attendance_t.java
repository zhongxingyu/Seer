 package com.ritchey.attendance.client;
 
 import java.util.Date;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.activity.shared.ActivityMapper;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.Window.Navigator;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.ritchey.attendance.client.mvp.AppActivityMapper;
 import com.ritchey.attendance.client.mvp.AppPlaceHistoryMapper;
 import com.ritchey.attendance.client.place.AttendancePlace;
 import com.ritchey.attendance.client.place.LoginPlace;
 import com.ritchey.attendance.client.view.AppConstants;
 import com.ritchey.attendance.domain.powercampus.SimpleLoginType;
 import com.ritchey.attendance.shared.Log;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Attendance implements EntryPoint {
 	
 	private Place defaultPlace = new AttendancePlace("login");
 	private SimplePanel appWidget = new SimplePanel();
 	
   /**
    * The message displayed to the user when the server cannot be reached or
    * returns an error.
    */
   private static final String SERVER_ERROR = "An error occurred while "
       + "attempting to contact the server. Please check your network "
       + "connection and try again.";
   
   public static final AttendanceResources images = GWT.create(
 		  AttendanceResources.class);
   public static final AppConstants constants = GWT.create(AppConstants.class);
 
   /**
    * Create a remote service proxy to talk to the server-side Greeting service.
    */
   private final AttendanceServiceAsync greetingService = GWT.create(AttendanceService.class);
 private ClientFactory clientFactory;
 
   /**
    * This is the entry point method.
    */
   public void onModuleLoad() {
 	    /*
 	     * Install an UncaughtExceptionHandler which will produce <code>FATAL</code> log messages
 	     */
 	    Log.setUncaughtExceptionHandler();
 		
 		clientFactory = GWT.create(ClientFactory.class);
 	
 
 	    // use deferred command to catch initialization exceptions in onModuleLoad2
 	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 	      @Override
 	      public void execute() {
 	        onModuleLoad2();
 	      }
 	    });
   }
   
   public void onModuleLoad2() {
 
 	  Log.debug("This is a 'DEBUG' test message");
 
 		final StringBuffer strInstructor = new StringBuffer("");
 		final StringBuffer name = new StringBuffer("");
 		final StringBuffer version = new StringBuffer("");
 		
 		final String strInfo = Cookies.getCookie(constants.cookieName());
 		Log.debug("get cookie " + constants.cookieName() + " is '" + strInfo + "'");
 		
 		if (strInfo != null) {
 			if (strInfo != null) {
 				String[] info = strInfo.split(":");
 				strInstructor.append(info[0]);
 				if (info.length > 1) {
 					name.append(info[1]);
 				}
 				
 				if (info.length > 2) {
 					version.append(info[2]);
 				}
 			}
 		}
 
 	
 		AsyncCallback<String> callback = new AsyncCallback<String>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("FAIL Initialize " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(String result) {
 				Log.debug("SUCCESS DONE");
 				String results[] = result.split("~");
 				Log.debug("results[0] = '" + results[0] + "'");
 				Log.debug("version = '" + version + "'");
 			    if(!results[0].equals(version.toString())){
 			    	Log.debug("NOT EQUAL");
 			    	String cookieInfo = strInstructor + ":" + name + ":" + results[0];
 			    	Log.debug("cookie is '" + cookieInfo + "'");
 					Date date = new Date();
 					date.setTime(date.getTime()+1000*60*60*24*120); // Add 120 days (4 month)
 			    	Cookies.setCookie(Attendance.constants.cookieName(), cookieInfo, date);
 			    	Window.Location.reload();
 			    	//Window.open("http://127.0.0.1:8888/attendance.html?gwt.codesvr=127.0.0.1:9997", "_self", "");	
 			    }
 			    else {
 			    	Log.debug("GO!.....");
 					
 					if (!strInstructor.toString().trim().equals("")) {
 						if (strInstructor.toString().equals(constants.backdoor())) {
 							defaultPlace = new AttendancePlace(new SimpleLoginType(constants.backdoorTranslate(), name.toString()));
 						}
 						else {
 							defaultPlace = new AttendancePlace(new SimpleLoginType(strInstructor.toString(), name.toString()));
 						}
 					}
 					else {
 						defaultPlace = new LoginPlace("login");
 					}
 					
 					EventBus eventBus = clientFactory.getEventBus();
 					PlaceController placeController = clientFactory.getPlaceController();
 
 					// Start ActivityManager for the main widget with our ActivityMapper
 					ActivityMapper activityMapper = new AppActivityMapper(clientFactory);
 					ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
 					activityManager.setDisplay(appWidget);
 
 					// Start PlaceHistoryHandler with our PlaceHistoryMapper
 					AppPlaceHistoryMapper historyMapper= GWT.create(AppPlaceHistoryMapper.class);
 					PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
 	
 					clientFactory.getHistoryHandler().register(placeController, eventBus, defaultPlace);
 	
 					RootPanel.get("content").add(appWidget);
 					
 					String userAgent = Navigator.getUserAgent();
 					boolean mobileApple = userAgent.contains("iPad;") || userAgent.contains("iPhone;");
 					
 					if (!mobileApple) {
 						AttendanceResources.INSTANCE.css().ensureInjected();
 					}
 					else {
 						AttendanceResources.INSTANCE.cssIpad().ensureInjected();
 					}
 					
					clientFactory.getPlaceController().goTo(defaultPlace);
 					clientFactory.getHistoryHandler().handleCurrentHistory();
 			    }
 
 				
 				
 			}
 		};
 		clientFactory.getRpcService().getContext(callback);
   }
 }
