 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.internetrt.sdk.InternetRT;
 import org.internetrt.sdk.util.DescribedListenerConfig;
 import org.internetrt.sdk.util.ListenerConfig;
 import org.internetrt.sdk.util.RoutingGenerator;
 import org.internetrt.sdk.util.Signal;
 
 import config.properties;
 
 import cn.edu.act.internetos.appmarket.service.TermToJson;
 
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Scope.Session;
 import models.App;
 import models.RoutingRecommender;
 class RoutingChoice{
 	private String signalName;
 	private String signalDescription;
 	private String listener;
 	private String listenerDescription;
 	private String routing;
 	
 	public RoutingChoice(String signalName,String signalDescription,String listener,String listenerDescription,String routing){
 		this.signalName =signalName ;
 		this.signalDescription=signalDescription;
 		this.listener=listener;
 		this.listenerDescription=listenerDescription;
 		this.routing=routing;
 	}
 	
 	public void setSignalName(String signalName) {
 		this.signalName = signalName;
 	}
 	public String getSignalName() {
 		return signalName;
 	}
 	public void setSignalDescription(String signalDescription) {
 		this.signalDescription = signalDescription;
 	}
 	public String getSignalDescription() {
 		return signalDescription;
 	}
 	public void setListener(String listener) {
 		this.listener = listener;
 	}
 	public String getListener() {
 		return listener;
 	}
 	public void setListenerDescription(String listenerDescription) {
 		this.listenerDescription = listenerDescription;
 	}
 	public String getListenerDescription() {
 		return listenerDescription;
 	}
 	public void setRouting(String routing) {
 		this.routing = routing;
 	}
 	public String getRouting() {
 		return routing;
 	}
 	
 }
 public class RoutingRecomController extends Controller{
 	
 	
 		public static String getAccessToken() {
 			System.out.println("get token!!!!!!!!!!!!!!!!!");
 			return session.get("token");
 		}
 		
 		   @Before(only = { "index","ConfirmRecomRouting" })
 		public static void checkUser() {
 			System.out.println("checkUser");
 			String token = getAccessToken();
 			if (token == null) {
 				System.out.println(properties.irt.getAuthCodeUrl(config.properties.appID,config.properties.redirect));
 				Controller.redirect(properties.irt.getAuthCodeUrl(config.properties.appID,config.properties.redirect));
 				System.out.println("checkUser!!!!");
 			}
 		}
 	   
 		public static void index(String fromAppIDString){
 			String accessToken = getAccessToken();
 			
 			RoutingRecommender routingRecommender = new RoutingRecommender();
 			List<scala.Tuple2<Signal,DescribedListenerConfig>> result = routingRecommender.getPossibleRoutings(fromAppIDString, accessToken);
 			List<RoutingChoice> choices = generateChoieces(result);
 			render("Routing/recomRouting.html",choices);
 		}
 		
 		private static List<RoutingChoice> generateChoieces(List<scala.Tuple2<Signal,DescribedListenerConfig>> possibleRoutings){
 			List<RoutingChoice> res = new ArrayList<RoutingChoice>();
 			for(scala.Tuple2<Signal,DescribedListenerConfig> data:possibleRoutings){
				String routing = RoutingGenerator.generateRouting(data._1.name(),data._2);
 				String signalName = data._1.name();
 				String signaldes = data._1.description();
 				String listenerApp = data._2.appName();
 				String listenerDes = data._2.description();
 				res.add(new RoutingChoice(signalName,signaldes,listenerApp,listenerDes,routing));
 			}
 		}
 
 		public static void ConfirmRecomRouting(){
 			String[] routings = request.params.getAll("choices");
 			InternetRT rt = config.properties.irt;
 			
 			String accessToken = getAccessToken();
 			
 			for(String routing:routings){
 				System.out.println(routing);
 				
 				rt.ConfirmRouting(accessToken,routing);
 			}
 			/*PrintWriter out = response.getWriter();
 			out.flush();
 			out.close();*/
 		}
 }
