 package controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import models.*;
 
 import play.*;
 import play.libs.Json;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.mvc.*;
 import play.mvc.WebSocket.Out;
 import scala.Int;
 
 import views.html.*;
 
 public class DisplayManagerController extends Controller {
   
   // appName
   public static String appName = "DManager"; //internal app name - do not change!
   public static String appDisplayName = "Usi Displays";
 	
  //public static String urlBase = "46.252.16.134";
  public static String urlBase = "uc-dev.inf.usi.ch";
  
   // wsAddress
   public static String wsAddress = "ws://"+urlBase+":9009/manager/socket/";
   
   //windowAddress
   public static String winAddress = "http://"+urlBase+":9009/display/?id=";
   
   // levels of debug messages: 0-none, 1-events, 2-method calls, 3-in method code 
   public static int verbose = 3;
 	
   //counter of connected display managers
   public static int plannerId = 0;
   
   //----------------------------------------------------------------
   //---- HTTP Request ----------------------------------------------
   //display size can be: small(600x1080), big(1320x1080), fullscreen(1920x1080)
 	
   public static Result defaultManager() {
 	  if(verbose == 1 || verbose == 2 || verbose == 3)
 		  Logger.info(appName+": a new manager is connecting");
 	  DLogger.addNew(new DLogger(appName, "manager-http-connect", "manager", "http-connect", "null"));
 	  return ok(views.html.displayIManager.render(appDisplayName, wsAddress, winAddress));
 	  
   }// defaultPlanner()
   
   //display connected
   public static void displayConnected(String displayId){
 	  if(verbose == 1 || verbose == 2 || verbose == 3)
 		  Logger.info("PLANNER: display connected id: "+displayId);
 	  
 	  //send display connect message to all display planners
 	  Set<?> set = plannerSockets.entrySet();
 	  // Get an iterator
 	  Iterator<?> i = (Iterator<?>) set.iterator();
 	  while(i.hasNext()) {
 			Map.Entry planner = (Map.Entry)i.next();
 			if(verbose == 1 || verbose == 2 || verbose == 3)
 				Logger.info("PLANNER: send display connect message did: "+displayId+" to planner pid: "+planner.getKey());
 			//send connection message
 			ObjectNode msg = Json.newObject();
 			msg.put("kind", "displayConnected");
 			msg.put("did", displayId);
 			plannerSockets.get(planner.getKey()).wOut.write(msg);
 	  }//while 
 	  
   }
   
   //display disconnected
   public static void displayDisconnected(String displayId){
 	  if(verbose == 1 || verbose == 2 || verbose == 3)
 		  Logger.info("PLANNER: display disconnected id: "+displayId);
 	  
 	  //send display disconnect message to all display planners
 	  Set<?> set = plannerSockets.entrySet();
 	  // Get an iterator
 	  Iterator<?> i = (Iterator<?>) set.iterator();
 	  while(i.hasNext()) {
 			Map.Entry planner = (Map.Entry)i.next();
 			if(verbose == 1 || verbose == 2 || verbose == 3)
 				Logger.info("PLANNER: send display disconnect message did: "+displayId+" to planner pid: "+planner.getKey());
 			//send connection message
 			ObjectNode msg = Json.newObject();
 			msg.put("kind", "displayDisconnected");
 			msg.put("did", displayId);
 			plannerSockets.get(planner.getKey()).wOut.write(msg);
 	  }//while 
 	  
   }
   
   //----------------------------------------------------------------
   //---- initialize the database if empty --------------------------
   public static void initDb(){
 	  initDbApps();
 	  initDbDisplays();
 	  
 	  initDbLayouts();
 	  initDbDisplayRegions();
 	  
 	  initDbArduinos();
 	  initDbSensors();
   }//initDb
   
   public static void initDbApps(){
 	  if(App.all().isEmpty()){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 			  Logger.info("Manager.initDbApps(): -------init apps db-------: ");
 		  DLogger.addNew(new DLogger(appName, "manager-init-db-apps", "manager", "init-db-apps", "null"));
 		  
 		  App app1 = new App("Twitter", "USI twitter2 application", 
 				  			 "http://46.252.16.134:7001/twitter2",
 				  			 "ws://46.252.16.134:7001/twitter2/socket", 
 				  			 //"http://46.252.16.134:7001/assets/stylesheets/apps/twitter/twitter-icon.png",
 				  			 "http://pdnet.inf.unisi.ch:/usiapps/Twitter/appicon/appicon",
 				  			 "", "1", "Free", "S");
 		  App app2 = new App("Google Maps", "Google Maps application", 
 				  			 "http://46.252.16.134:7001/googleMap",
 				  			 "ws://46.252.16.134:7001/googleMap/socket", 
 				  			 "http://46.252.16.134:7001/assets/stylesheets/apps/googleMap/googleMap-icon.png", 
 				  			 "", "1", "Free", "M");
 		  App app3 = new App("MomentsMachine", "Moments machine app", 
 	 		 		 		 "https://pdnet.inf.unisi.ch/MomentMachine2/",
 	 		 		 		 "ws://pdnet.inf.unisi.ch/MomentMachine2/socket/", 
 	 		 		 		 "http://46.252.16.134:9002/mm2assets/buttons/mm_logo_small.png", 
 	 		 		 		 "", "1", "Free", "S");
 		  App app4 = new App("MomentsGallery", "Moments gallery application", 
 	 		 		 		 "http://pdnet.inf.unisi.ch:9004",
 	 		 		 		 "", 
 	 		 		 		 "http://pdnet.inf.unisi.ch:9004/assets/images/mg_logo.png", 
 	 		 		 		 "", "1", "Free", "M");
 		  App app5 = new App("USI Channels", "USI Channels application", 
 				  			 "http:"+urlBase+":9015/",
 				  			 "", 
 				  			 "http://pdnet.inf.unisi.ch/usiapps/usiicon.png", 
 				  			 "", "1", "Free", "M,F");
 		  App app6 = new App("USI Academic Calendar", "USI Academic Calendar application", 
 				  			"http:"+urlBase+":9015/?app=USI%20Academic%20Calendar",
 					 		 "", 
 					 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Academic%20Calendar/appicon/appicon", 
 					 		 "", "1", "Free", "M,F");
 		  App app7 = new App("USI Bike Sharing", "USI Bike Sharing application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Bike%20Sharing",
 		  			 		 "", 
 		  			 		 "http://pdnet.inf.unisi.ch/usiapps/USI%20Bike%20Sharing/appicon/appicon",
 		  			 		 "", "1", "Free", "M,F");
 		  App app8 = new App("USI Mensa", "USI Mensa", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Mensa",
 		  			 		 "", 
 		  			 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Mensa", 
 		  			 		 "", "1", "Free", "M,F");
 		  App app9 = new App("USI Go Social", "USI Go Social application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Go%20Social",
 					 		 "", 
 					 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Go%20Social/appicon/appicon", 
 					 		 "", "1", "Free", "M,F");
 		  App app10 = new App("USI Info", "USI Info application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Info",
 					 		 "", 
 					 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Info/appicon/appicon", 
 					 		 "", "1", "Free", "M,F");
 		  App app11 = new App("USI Map", "USI Map application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Map",
 			 		  		 "", 
 			 		  		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Map/appicon/appicon", 
 			 		  		 "", "1", "Free", "M,F");
 		  App app12 = new App("USI News and Events", "USI News and Events application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20News%20and%20Events",
 		  			 		 "", 
 		  			 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20News%20and%20Events/appicon/appicon", 
 		  			 		 "", "1", "Free", "M,F");
 		  App app13 = new App("USI Offices", "USI Offices application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Offices",
 			 		 		 "", 
 			 		 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Offices/appicon/appicon", 
 			 		 		 "", "1", "Free", "M,F");
 		  App app14 = new App("USI on the Spot", "USI on the Spot application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20on%20the%20Spot",
 			 		 		 "", 
 			 		 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20on%20the%20Spot/appicon/appicon", 
 			 		 		 "", "1", "Free", "M,F");
 		  App app15 = new App("USI Public Transportation", "USI Public Transportation application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Public%20Transportation",
 			 		 		 "", 
 			 		 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Public%20Transportation/appicon/appicon", 
 			 		 		 "", "1", "Free", "M,F");
 		  App app16 = new App("USI Career", "USI Career application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Career",
 			 		 		 "", 
 			 		 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Career/appicon/appicon", 
 			 		 		 "", "1", "Free", "M,F");
 		  App app17 = new App("USI Housing", "USI Housing application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Housing",
 			 		 		 "", 
 			 		 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Housing/appicon/appicon", 
 			 		 		 "", "1", "Free", "M,F");
 		  App app18 = new App("USI Sport", "USI Sport application", 
 				  			 "http:"+urlBase+":9015/?app=USI%20Sport",
 			 		 		 "", 
 			 		 		 "http://pdnet.inf.unisi.ch:/usiapps/USI%20Sport/appicon/appicon", 
 			 		 		 "", "1", "Free", "M,F");
 		
 		  App.addNew(app1);
 		  App.addNew(app2);
 		  App.addNew(app3);
 		  App.addNew(app4);
 		  App.addNew(app5);
 		  App.addNew(app6);
 		  App.addNew(app7);
 		  App.addNew(app8);
 		  App.addNew(app9);
 		  App.addNew(app10);
 		  App.addNew(app11);
 		  App.addNew(app12);
 		  App.addNew(app13);
 		  App.addNew(app14);
 		  App.addNew(app15);
 		  App.addNew(app16);
 		  App.addNew(app17);
 		  App.addNew(app18);
 	  }//if
   }//initDbApps
   
   public static void initDbDisplays(){
 	  if(Display.all().isEmpty()){
 		 if(verbose == 1 || verbose == 2 || verbose == 3)
 			 Logger.info("Manager.initDbDisplays(): -------init displays db-------: ");
 		 DLogger.addNew(new DLogger(appName, "manager-init-db-displays", "manager", "init-db-displays", "null"));
 		  
 		 Display d1 = new Display("OrientaTi14sc", "Sotto Cubo", "1", 	//name, description, owner
 				 				 "1920px", "1080px", 					//size
 				 				 "46.010697", "8.958135", "E", "2m", 	//location, orientation, radius
 				 				 "2", 									//layout
 				 				 "3:3,4:2",								//applications 
 				 				 "1", 									//sensors
 				 				 "none");								//constraints
 		 
 		 Display d2 = new Display("OrientaTi14am", "AMagna", "1", 		//name, description, owner
 				 				 "1920px", "1080px",					//size
 				 				 "46.010974", "8.957697", "E", "2m", 	//location, orientation, radius
 				 				 "2", 									//layout
 				 				 "3:3,4:2",								//applications
 				 				 "2",									//sensors
 				 				 "none");								//constraints
 		 
 		 Display d3 = new Display("UD-1-Inf-2", "Inf-2", "1", 			//name, description, owner
 				 				 "1920px", "1080px",					//size
 				 				 "46.010974", "8.957697", "E", "2m", 	//location, orientation, radius
 				 				 "2",									//layout
 				 				 "3:3,1:3,4:2,6:2,7:2,8:2,9:2,10:2,11:2,12:2,13:2,14:2,15:2,16:2,17:2,18:2,5:2", //applications
 				 				 "1",									//sensors
 				 				 "none");								//constraints
 		 
 		 Display d4 = new Display("UD-2-Inf-2", "Inf-2", "1", 			//name, description, owner
 				 				 "1920px", "1080px",					//size
 				 				 "46.010974", "8.957697", "E", "2m", 	//location, orientation, radius
 				 				 "2",									//layout
 				 				 "3:3,1:3,4:2,6:2,7:2,8:2,9:2,10:2,11:2,12:2,13:2,14:2,15:2,16:2,17:2,18:2,5:2", //applications
 				 				 "2",									//sensors
 				 				 "none");								//constraints
 
 		 Display d5 = new Display("UD-3-Inf-2", "Inf-2", "1", 			//name, description, owner
 				 				 "1920px", "1080px",					//size
 				 				 "46.010974", "8.957697", "E", "2m", 	//location, orientation, radius
 				 				 "2",									//layout
 				 				 "3:3,1:3,4:2,6:2,7:2,8:2,9:2,10:2,11:2,12:2,13:2,14:2,15:2,16:2,17:2,18:2,5:2", //applications
 				 				 "3",									//sensors
 				 				 "none");								//constraints
 
 		 Display d6 = new Display("UD-4-Inf-2", "Inf-2", "1", 			//name, description, owner
 				 				 "1920px", "1080px",					//size
 				 				 "46.010974", "8.957697", "E", "2m", 	//location, orientation, radius
 				 				 "2",									//layout
 				 				 "3:3,1:3,4:2,6:2,7:2,8:2,9:2,10:2,11:2,12:2,13:2,14:2,15:2,16:2,17:2,18:2,5:2", //applications
 				 				 "4",									//sensors
 				 				 "none");								//constraints
 
 		 Display d7 = new Display("Test screen S", "Mensa", "1", 		//name, description, owner
 				 				 "1920px", "1080px",					//size
 				 				 "46.010697", "8.958135", "E", "2m", 	//location, orientation, radius
 				 				 "2", 									//layout
 				 				 "3:3,4:2,6:2,7:2,8:2,9:2,10:2,11:2,12:2,13:2,14:2,15:2,16:2,17:2,18:2,5:2", //applications
 				 				 "1",									//sensors
 				 				 "none");								//constraints
 		 
 		 Display d8 = new Display("Test screen F", "Mensa", "1", 		//name, description, owner
 				 				 "1920px", "1080px", 					//size
 				 				 "46.010974", "8.957697", "E", "2m", 	//location, orientation, radius
 				 				 "1",									//layout
 				 				 "6:1,7:1,8:1,9:1,10:1,11:1,12:1,13:1,14:1,15:1,16:1,17:1,18:1,5:1",	//applications
 				 				 "1", 									//sensors
 				 				 "none");								//constraints
 		 
 		 Display.addNew(d1);
 		 Display.addNew(d2);
 		 Display.addNew(d3);
 		 Display.addNew(d4);
 		 Display.addNew(d5);
 		 Display.addNew(d6);
 		 Display.addNew(d7);
 		 Display.addNew(d8);
 	  }//if
   }//initDbApps
   
   public static void initDbArduinos(){
 	  if(Arduino.all().isEmpty()){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 				 Logger.info("Manager.initDbArduinos(): -------init arduinos db-------: ");
 		  DLogger.addNew(new DLogger(appName, "manager-init-db-arduinos", "manager", "init-db-arduinos", "null"));
 		  
 		  Arduino a1 = new Arduino("1");
 		  Arduino a2 = new Arduino("2");
 		  Arduino.addNew(a1);
 		  Arduino.addNew(a2);
 	  }//if
   }//initDbArduinos
   
   public static void initDbSensors(){
 	  if(Sensor.all().isEmpty()){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 				 Logger.info("Manager.initDbSensors(): -------init sensors db-------: ");
 		  DLogger.addNew(new DLogger(appName, "manager-init-db-sensors", "manager", "init-db-sensors", "null"));
 		  
 		  Sensor s1 = new Sensor("rfid", "3D0048D946EA"); //blue key chain
 		  Sensor s2 = new Sensor("rfid", "2E003053B8F5"); //green card
 		  Sensor s3 = new Sensor("rfid", "64B5C379"); //ivan
 		  Sensor s4 = new Sensor("rfid", "8BFD9853"); //green card
 		  Sensor s5 = new Sensor("rfid", "2470C479"); //green card
 		  Sensor s6 = new Sensor("rfid", "FBD29853"); //green card
 		  Sensor s7 = new Sensor("rfid", "8D1BA57E"); //green card
 		  Sensor s8 = new Sensor("rfid", "CDBBD6D1"); //green card
 		  Sensor s9 = new Sensor("rfid", "44B2C379"); //green card
 		  
 		  Sensor.addNew(s1);
 		  Sensor.addNew(s2);
 		  Sensor.addNew(s3);
 		  Sensor.addNew(s4);
 		  Sensor.addNew(s5);
 		  Sensor.addNew(s6);
 		  Sensor.addNew(s7);
 		  Sensor.addNew(s8);
 		  Sensor.addNew(s9);
 	  }//if
   }//initDbSensors
   
   public static void initDbDisplayRegions(){
 	  if(DisplayRegion.all().isEmpty()){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 				 Logger.info("Manager.initDbDisplayRegions(): -------init display derions db-------: ");
 		  DLogger.addNew(new DLogger(appName, "manager-init-db-regions", "manager", "init-db-regions", "null"));
 		  
 		  DisplayRegion dr1 = new DisplayRegion("fullscreen", "fullscreen region", "0", "0", "1080", "1920");
 		  DisplayRegion dr2 = new DisplayRegion("mainarea", "main area on the display", "0", "600px", "1080", "1320");
 		  DisplayRegion dr3 = new DisplayRegion("sidebar", "sidebar region", "0", "0", "1080", "600");
 		  DisplayRegion dr4 = new DisplayRegion("tickertape", "tickertape region", "1035px", "0", "45", "1920");
 		  
 		  DisplayRegion.addNew(dr1);
 		  DisplayRegion.addNew(dr2);
 		  DisplayRegion.addNew(dr3);
 		  DisplayRegion.addNew(dr4);
 	  }//if
   }//initDbDisplayRegions
   
   public static void initDbLayouts(){
 	  if(Layout.all().isEmpty()){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 				 Logger.info("Manager.initLayouts(): -------init layouts db-------: ");
 		  DLogger.addNew(new DLogger(appName, "manager-init-db-layouts", "manager", "init-db-layouts", "null"));
 		  
 		  Layout l1 = new Layout("fullscreen", "show one app in fullscreen", "1080", "1920", "1");
 		  Layout l2 = new Layout("sidebar", "show second app on the left side", "1080", "1920", "3,2");
 		  Layout l3 = new Layout("tickertape", "show tird app on the bottom", "1080", "1920", "3,2,4");
 		  Layout l4 = new Layout("all", "all possible combinations", "1080", "1920", "3,2,4,1");
 		  
 		  Layout.addNew(l1);
 		  Layout.addNew(l2);
 		  Layout.addNew(l3);
 		  Layout.addNew(l4);
 	  }//if
   }//initDbLayouts
   
   
 
   //----------------------------------------------------------------
   //---- WS send messages ------------------------------------------
   
   //---- apps ----
   public static void sendAppsToClient(WebSocket.Out<JsonNode> out){
 	  if(verbose == 1 || verbose == 2 || verbose == 3)
 			Logger.info("PLANNER.WS():		send apps to client: "+plannerSocketReverter.get(out));
 	  //get all apps
 	  List<App> appList = new ArrayList<App>();
 	  appList = App.all();
 	  
 	  //send each app to the client
 	  for(int i=0; i < appList.size(); i++){
 		  sendAppToClient(appList.get(i), out);
 	  }//for
   }//sendAppsToClient
   
   public static void sendAppToClients(App app){
 	  if(verbose == 1 || verbose == 2 || verbose == 3)
 		  Logger.info("PLANNER.WS():		send app: "+app.name+" to clients");
 	  //send to all clients
 	  Set<?> set = plannerSockets.entrySet();
 	  // Get an iterator
 	  Iterator<?> i = (Iterator<?>) set.iterator();
 	  // Display elements
 	  while(i.hasNext()) { //send image to all connected clients
 		  Map.Entry ds = (Map.Entry)i.next();
 		  sendAppToClient(app, plannerSockets.get(ds.getKey()).wOut);
 	  }//while 
 	  
   }//sendAppToClients
   
   public static void sendAppToClient(App app, WebSocket.Out<JsonNode> out){
 	 if(app != null){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 				Logger.info("PLANNER.WS():			send app: "+app.name+" to client: "+plannerSocketReverter.get(out));
 		  DLogger.addNew(new DLogger(appName, "manager-ws-send-app", "manager", plannerSocketReverter.get(out) , new Long(app.id).toString()));
 			
 		  ObjectNode msg = Json.newObject();
 				msg.put("kind", "addApp");
 				msg.put("did", plannerSocketReverter.get(out));
 				msg.put("icon", app.iconAddress);
 				msg.put("aid", app.id);
 				msg.put("name", app.name);
 				msg.put("description", app.description);
 				msg.put("developer", app.developer);
 				msg.put("size", app.size);
 				msg.put("rank", app.ranking);
 				msg.put("price", app.price);
 		out.write(msg);
 	}//if
   }//sendAppToClient()
   
   //---- displays ----
   public static void sendDisplaysToClient(WebSocket.Out<JsonNode> out){
 	  if(verbose == 1 || verbose == 2 || verbose == 3)
 			Logger.info("PLANNER.WS():		send displays to client: "+plannerSocketReverter.get(out));
 	  //get all displays
 	  List<Display> displayList = new ArrayList<Display>();
 	  displayList = Display.all();
 	  
 	  //send each each to the client
 	  for(int i=0; i < displayList.size(); i++){
 		  sendDisplayToClient(displayList.get(i), out);
 	  }//for
   }//sendDisplaysToClient
   
   public static void sendDisplayToClient(Display display, WebSocket.Out<JsonNode> out){
 	  if(display != null){
 		  if(verbose == 1 || verbose == 2 || verbose == 3)
 			Logger.info("PLANNER.WS():			send display: "+display.name+" to client: "+plannerSocketReverter.get(out));
 		  DLogger.addNew(new DLogger(appName, "manager-ws-send-display", "manager", plannerSocketReverter.get(out) , new Long(display.id).toString()));
 			
 		  ObjectNode msg = Json.newObject();
 				msg.put("kind", "addDisplay");
 				msg.put("did", display.id);
 				msg.put("name", display.name);
 				msg.put("description", display.description);
 				msg.put("width", display.width);
 				msg.put("height", display.height);
 				msg.put("latitude", display.latitude);
 				msg.put("longitude", display.longitude);
 				msg.put("orientation", display.orientation);
 				msg.put("radius", display.radius);
 				msg.put("layout", display.layout);
 				msg.put("apps", display.apps);
 				msg.put("sensors", display.sensors);
 				msg.put("cons", display.cons);
 		out.write(msg);
 	}//if
   }//sendAppToClient()
   
   public static void sendInfoToClient(WebSocket.Out<JsonNode> out){
 	  sendAppsToClient(out);
 	  sendDisplaysToClient(out);
   }//sendInfoToClients
   
   //----------------------------------------------------------------
   //---- WS --------------------------------------------------------
  
   public static HashMap<String, Sockets> plannerSockets = new HashMap<String, Sockets>();
   public static HashMap<WebSocket.Out<JsonNode>, String> plannerSocketReverter = new HashMap<WebSocket.Out<JsonNode>, String>();
   
   public static WebSocket<JsonNode> webSocket() { 
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out){
 
 				// For each event received on the socket 
 				in.onMessage(new Callback<JsonNode>() { 
 					public void invoke(JsonNode event) {
 
 						String messageKind = event.get("kind").asText();						
 
 						if(messageKind.equals("plannerReady")){
 							plannerId++;
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): new planner_id: "+plannerId);
 							DLogger.addNew(new DLogger(appName, "manager-ws-connect","manager", new Integer(plannerId).toString() , "null"));
 							  							
 							//register planner
 							plannerSockets.put(Integer.toString(plannerId) , new Sockets(out));
 							plannerSocketReverter.put(out, Integer.toString(plannerId));
 							
 									
 						}//plannerReady
 
 						if(messageKind.equals("plannerClose")){
 							
 						
 						}//displayClose
 						
 						//   --- display refresh ---
 						if(messageKind.equals("displayRefresh")){
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): refresh display "+event.get("did"));
 							DLogger.addNew(new DLogger(appName, "manager-ws-display-refresh", "manager", "null", event.get("did").toString()));
 							
 							DisplayController.refreshDisplay(event.get("did").asText());
 						}//displayRefresh
 						
 						//   --- display on/off ---
 						if(messageKind.equals("displayOnOff")){
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): on/off display "+event.get("did"));
 							DLogger.addNew(new DLogger(appName, "manager-ws-display-onoff", "manager", "null", event.get("did").toString()));
 							
 							DisplayController.onOffDisplay(event.get("did").asText());
 						}//displayRefresh
 						
 						//   --- login info ---
 						if(messageKind.equals("login")){
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): login: un= "+event.get("username").asText()+" p= "+event.get("password").asText());
 							
 							DLogger.addNew(new DLogger(appName, "manager-ws-login", "manager", event.get("username").toString(), "null"));
 							
 							
 							//take all db entries
 							List<Owner> accounts = new ArrayList<Owner>();
 							accounts = Owner.all();
 							
 							//check the login data in db
 							boolean loginOk = false;
 							Long code = 0L;
 							Iterator<?> i = (Iterator<?>) accounts.iterator();
 							while(i.hasNext()) {
 								Owner element = (Owner)i.next();
 								if(element.username.equals(event.get("username").asText())){
 									if(element.password.equals(event.get("password").asText())){
 										//login ok
 										loginOk = true;
 										code = element.code;
 										break; //exit the while loop if login is ok
 									}//if
 								}//if	
 							}//while 
 							
 							if(!loginOk){
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("	login: loginNotOk.");
 								DLogger.addNew(new DLogger(appName, "manager-ws-login-notok", "manager", event.get("username").toString(), "login-notok"));
 								
 								ObjectNode msgLoginNotOk = Json.newObject();
 								msgLoginNotOk.put("kind", "loginNotOk");
 								msgLoginNotOk.put("username", event.get("username").asText());
 								out.write(msgLoginNotOk);
 							}else{
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("	login: loginOk.");
 								DLogger.addNew(new DLogger(appName, "manager-ws-login-ok","manager", event.get("username").toString(), "login-ok"));
 								
 								ObjectNode msgLoginOk = Json.newObject();
 								msgLoginOk.put("kind", "loginOk");
 								msgLoginOk.put("username", event.get("username").asText());
 								msgLoginOk.put("code", code);
 								out.write(msgLoginOk);
 								
 								//when loginOk - send all info to the client
 								sendInfoToClient(out);
 								
 							}//if
 							
 						}//loginInfo
 						
 						//   --- login code ---
 						if(messageKind.equals("loginCode")){
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): login: code= "+event.get("code").asText());
 							DLogger.addNew(new DLogger(appName, "manager-ws-login-code", "manager", event.get("code").toString(), "login"));
 							
 							
 							//take all db entries
 							List<Owner> accounts = new ArrayList<Owner>();
 							accounts = Owner.all();
 							
 							//check the login data in db
 							boolean loginOk = false;
 							String un = "";
 							Iterator<?> i = (Iterator<?>) accounts.iterator();
 							while(i.hasNext()) {
 								Owner element = (Owner)i.next();
 								
 								if(Long.toString(element.code).equals(event.get("code").asText())){
 										//login ok
 										loginOk = true;
 										un = element.username;
 										break; //exit the while loop if login is ok
 									}//if
 							}//while 
 							
 							if(!loginOk){
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("	login:code: loginCodeNotOk.");
 								DLogger.addNew(new DLogger(appName, "manager-ws-login-code-notok","manager", event.get("code").toString(), "login-notok"));
 								
 								
 								ObjectNode msgLoginNotOk = Json.newObject();
 								msgLoginNotOk.put("kind", "loginCodeNotOk");
 								out.write(msgLoginNotOk);
 							}else{
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("	login:code: loginCodeOk.");
 								DLogger.addNew(new DLogger(appName, "manager-ws-login-code-ok", "manager", event.get("code").toString(), "login-ok"));
 								
 								ObjectNode msgLoginOk = Json.newObject();
 								msgLoginOk.put("kind", "loginCodeOk");
 								msgLoginOk.put("username", un);
 								out.write(msgLoginOk);
 								
 								//when loginOk - send all apps to the client
 								sendInfoToClient(out);
 							}//if
 	
 						}//loginCode
 						
 						//   --- logout ---
 						if(messageKind.equals("logout")){
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): logout message");
 							DLogger.addNew(new DLogger(appName, "manager-ws-logout","manager", "null", "loginout"));
 							
 						}//loginCode
 						
 						//   --- register info ---
 						if(messageKind.equals("register")){
 							if(verbose == 1 || verbose == 2 || verbose == 3)
 								Logger.info("PLANNER.WS(): register: un= "+event.get("username").asText()+" e= "+event.get("email").asText()+" p= "+event.get("password").asText());
 							DLogger.addNew(new DLogger(appName, "manager-ws-register", "manager", event.get("username").toString(), "register"));
 							
 							//new owner object based on received register data
 							Owner register = new Owner(event.get("username").asText(),event.get("email").asText(),event.get("password").asText());
 				
 							//take all db entries
 							List<Owner> accountsReg = new ArrayList<Owner>();
 							accountsReg = Owner.all();
 							
 							//check if the user/owner info already exists in db
 							boolean userExists = false;
 							Iterator<?> i = (Iterator<?>) accountsReg.iterator();
 							while(i.hasNext()) {
 								Owner element = (Owner)i.next();
 								if(element.username.equals(register.username)){
 									//user already exists
 									userExists = true;
 								}//if	
 							}//while 
 							
 							if(userExists){ //send a message to the client that the username is already taken
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("		register: new user is already in db, cannot register two times.");
 								DLogger.addNew(new DLogger(appName, "manager-ws-register-notok", "manager", event.get("username").toString(), "un-exists"));
 								
 								ObjectNode msgRegNotOk = Json.newObject();
 								msgRegNotOk.put("kind", "regNotOk");
 								msgRegNotOk.put("username", register.username);
 								out.write(msgRegNotOk);
 							}else{ //add the new user to the db
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("		register: new user is not in db, add new user to db.");
 								DLogger.addNew(new DLogger(appName, "manager-ws-register-ok","manager", event.get("username").toString(), register.code.toString()));
 								
 								Owner.addNew(register);
 								ObjectNode msgRegOk = Json.newObject();
 								msgRegOk.put("kind", "regOk");
 								msgRegOk.put("username", register.username);
 								msgRegOk.put("code", register.code);
 								out.write(msgRegOk);
 								
 								//when loginOk - send all apps to the client
 								sendInfoToClient(out);
 							}//if
 										
 						}//register
 						
 						//   --- register new app ---
 						if(messageKind.equals("addNewApp")){
 							
 							App newApp = new App(event.get("name").asText(), event.get("description").asText(), event.get("web").asText(), 
 												 event.get("ws").asText(), event.get("icon").asText(), event.get("sshot").asText(),
 												 event.get("developer").asText(), "Free", event.get("ssize").asText());
 							
 							DLogger.addNew(new DLogger(appName, "manager-ws-add-new-app", "manager", event.get("name").toString(), event.get("web").asText()));
 							
 							
 							if(App.getAppByName(newApp.name) == null){
 								App.addNew(newApp);
 								ObjectNode msgOk = Json.newObject();
 								msgOk.put("kind", "addNewAppOk");
 								msgOk.put("username", newApp.developer);
 								msgOk.put("appname", newApp.name);
 								out.write(msgOk);
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("PLANNER.WS():addNewApp name:"+newApp.name
 																	   +" web:"+newApp.httpAddress
 																	   +" ws:"+newApp.wsAddress
 																	   +" icon:"+newApp.iconAddress
 																	   +" sshot:"+newApp.screenShotAddress
 																	   +" size:"+newApp.size
 																	   +" des:"+newApp.description
 																	   +" dev:"+newApp.developer);
 								DLogger.addNew(new DLogger(appName, "manager-ws-add-new-app-ok", "manager", event.get("name").toString(), event.get("web").asText()));
 								
 								//send message to all clients that a new app has been added
 								sendAppToClients(newApp);
 								
 							}else{
 								ObjectNode msgNotOk = Json.newObject();
 								msgNotOk.put("kind", "addNewAppNotOk");
 								msgNotOk.put("username", newApp.developer);
 								msgNotOk.put("appname", newApp.name);
 								out.write(msgNotOk);
 								if(verbose == 1 || verbose == 2 || verbose == 3)
 									Logger.info("PLANNER.WS():addNewApp app:"+newApp.name+" already exists!");
 								DLogger.addNew(new DLogger(appName, "manager-ws-add-new-app-notok", "manager", event.get("name").toString(), event.get("web").asText()));
 									
 							}//if
 						}//addNewApp
 						
 
 					}//invoke
 				});//in.onMessage
 
 				// When the socket is closed. 
 				in.onClose(new Callback0() {
 					public void invoke() { 
 						String displayID = plannerSocketReverter.get(out);
 						plannerSocketReverter.remove(out);
 						plannerSockets.remove(displayID);
 						if(verbose == 1 || verbose == 2 || verbose == 3)
 							Logger.info("PLANNER.WS(): planner "+displayID+" is disconnected; number of connected planner: "+plannerSockets.size());
 					}//invoke
 				});//in.onClose
 
 			}//onReady
 		};//WebSocket<String>()
 	}//webSocket() { 
 
 	public static class Sockets {
 		public WebSocket.Out<JsonNode> wOut;
 
 		public Sockets(Out<JsonNode> out) {
 			this.wOut = out;
 		}
 	}//class
   
 	//WS --------------------------------------------------------------------
   
 }// public class DisplayController extends Controller
 
 
