 package global;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import models.Right;
 
 import controllers.routes;
 
 import pages.AdminHuntEditPage;
 import pages.AdminPanelPage;
 import pages.AdminUserEditPage;
 import pages.DashboardPage;
 import pages.EnigmaCreatePage;
 import pages.EnigmaEditPage;
 import pages.GamePage;
 import pages.HomePage;
 import pages.HuntCreatePage;
 import pages.HuntEditPage;
 import pages.HuntShowPage;
 import pages.StepCreatePage;
 import pages.StepEditPage;
 import play.Application;
 import play.GlobalSettings;
 import play.api.mvc.Call;
 import play.api.mvc.Handler;
 import play.mvc.Action;
 import play.mvc.Http.Request;
 import play.mvc.Http.RequestHeader;
 
 public class Global extends GlobalSettings {
 	
 	public static final int sysCheckRequestInterval = 10;// Every 10 requests
 	public static final double criticalMemoryPercent = 0.90;// 90% +
 	private static int sysCheckRequestCount = sysCheckRequestInterval;
 
 	@Override
 	public void onStart(Application app) {
 		super.onStart(app);
 
 		// Initialisation de Sésame
 		String sesameDir = app.configuration().getString("sesame.store.directory");
 
 		if (sesameDir != null) {
 			Sesame.initialize(sesameDir);
 		} else {
 			throw new RuntimeException("sesame.store.directory de application.conf doit être spécifié");
 		}
 
 		// System.out.println("URL: "+getURLFromRoute("routes.AdminPanelController.userlist"));
 
 		initializeFormatters();
 
 		
 		try {
 			//		***    Pages    ***
 			// Put all pages' configuration below
 
 			// Admin Panel Pages
 			new AdminPanelPage("adminuserlist", "Utilisateurs", routes.AdminPanelController.userlist(), Right.USER_LIST, "");
 			new AdminPanelPage("adminhuntlist", "Chasses", routes.AdminPanelController.huntlist(), Right.HUNT_LIST, "");
 			new AdminUserEditPage("Édition d'un utilisateur", Right.USER_LIST, "");
 			new AdminHuntEditPage("Édition d'une chasse", Right.HUNT_LIST, "dashboard/hunt");
 			
 			// Dashboard Pages
			new DashboardPage("dashboard", "Tableau de bord", routes.ManagerController.dashboard(), "dashboard/dashboard");
 			
 			new HuntShowPage("Voir la chasse", "dashboard/show_hunt");
 			new HuntCreatePage("Nouvelle chasse", "dashboard/hunt");
 			new HuntEditPage("Modifier la chasse", "dashboard/hunt");
 			
 			new StepCreatePage("Nouvelle étape", "dashboard/step");
 			new StepEditPage("Modifier l'étape", "dashboard/step");
 			
 			new EnigmaCreatePage("Nouvelle énigme", "dashboard/enigma");
 			new EnigmaEditPage("Modifier l'énigme", "dashboard/enigma");
 			
 			//new EnigmaCreatePage("dashboard", "Nouvelle énigme", Right.MEMBER_AREA, "dashboard/enigma");
 			
 			// Game Pages
 			new GamePage("play", "Jouer", routes.GameController.home(), Right.MEMBER_AREA, "");
 
 			// Home Pages
 			new HomePage("home", "Découvrir", routes.ApplicationController.index(), "");
 			new HomePage("login", "Se connecter", routes.UserController.login(), "");// And register page
 			new HomePage("logout", "Déconnexion", routes.UserController.logout(), "");
 			
 
 			//		***    Static Menus    ***
 			// Put all menus' configuration below
 			// Use an existing menu, declared in class Menu or use a dynamic one.
 
 			// Member's Global Menu
 			Menu.memberMenu.add("home");
 			Menu.memberMenu.add("dashboard");
 			Menu.memberMenu.add("adminuserlist");
 			Menu.memberMenu.add("logout");
 			
 			// Admin Panel Menu
 			Menu.adminPanelMenu.add("adminuserlist");
 			Menu.adminPanelMenu.add("adminhuntlist");
 			
 			// Dashboard Menu
 			Menu.dashboardMenu.add("dashboard");
 			Menu.dashboardMenu.add("huntcreate");
 			Menu.dashboardMenu.add("play");
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
  	
 	@Override
  	public Action<?> onRequest(Request request, Method actionMethod) {
 		if( sysCheckRequestCount-- <= 0 ) {
 			if( Runtime.getRuntime().totalMemory() > criticalMemoryPercent*Runtime.getRuntime().maxMemory() ) {
 				System.out.println("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"] System memory usage is CRITICAL !");
 			}
 			sysCheckRequestCount = sysCheckRequestInterval;
 		}
  		//System.out.println("Requesting");
  		Action<?> action = super.onRequest(request, actionMethod);
  		//System.out.println("Request done");
  		return action;
  	}
  	
  	@Override
  	public Handler onRouteRequest(RequestHeader arg0) {
  		//System.out.println("onRouteRequest");
  		Handler handler = super.onRouteRequest(arg0);
  		//System.out.println("onRouteRequest done");
  		return handler;
  	}
 
 	@Override
 	public void onStop(Application app) {
 		// Extinction de Sésame
 		Sesame.shutdown();
 
 		super.onStop(app);
 	}
 
 	private void initializeFormatters() {
 
 	}
 	
 	public static String displayAppLoad() {
 		String result = "AppLoad> Pages loading:	"+CurrentRequest.getLoadingPages().size()+"\n";
 		long totMemory = Runtime.getRuntime().totalMemory();
 	    long maxMemory = Runtime.getRuntime().maxMemory();
 	    result += "AppLoad> Memory Usage:	"+humanReadableByteCount(totMemory, false)+
 	    	" / "+(maxMemory == Long.MAX_VALUE ? "No limit" : humanReadableByteCount(maxMemory, false))+
 	    	((totMemory > criticalMemoryPercent*maxMemory) ? " (OVERLOADING)" :"")+"\n";
 		//System.out.println("AppLoad> Free memory (bytes):	"+Runtime.getRuntime().freeMemory());
 		//System.out.println("AppLoad> Using memory (bytes):	"+humanReadableByteCount(Runtime.getRuntime().totalMemory(), false));
 		//System.out.println("AppLoad> Max memory (bytes):	"+(maxMemory == Long.MAX_VALUE ? "No limit" : humanReadableByteCount(maxMemory, false)));
 	    System.out.print(result);
 	    return result;
 	}
 	
 	public static String humanReadableByteCount(long bytes, boolean si) {
 	    int unit = si ? 1000 : 1024;
 	    if (bytes < unit) return bytes + " B";
 	    int exp = (int) (Math.log(bytes) / Math.log(unit));
 	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
 	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
 	}
 
 	// Don't care about parameters
 	public static String getURLFromRoute(String route) {
 		try {
 			String[] subs = route.split("\\.");
 			for (Field f : routes.class.getFields()) {
 				if (f.getName().equals(subs[1])) {
 					Object ctrl = f.getType().newInstance();
 					for (Method m : f.getType().getMethods()) {
 						if (m.getName().equals(subs[2])) {
 							Call c = (Call) m.invoke(ctrl);
 							
 							return c.url();
 						}
 					}
 					break;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
