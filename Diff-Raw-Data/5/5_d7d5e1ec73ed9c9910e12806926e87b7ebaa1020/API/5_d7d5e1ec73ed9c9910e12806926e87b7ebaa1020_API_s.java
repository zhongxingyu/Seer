 package ca.hvsi.app;
 import android.app.Application;
 import com.usepropeller.routable.Router;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import ca.hvsi.lib.ApiClient;
 
 public class API extends Application{
 	private static Context context_;
 	private ApiClient root_, api_, game_, database_, blog_, globals_;
 	private static ca.hvsi.lib.Account self_;
 	private static boolean logged_in_;
 	private static boolean can_register_;
 	public void onCreate() {
 		API.context_ = getApplicationContext();
         Router.sharedRouter().setContext(API.context_);
         Router.sharedRouter().map("home", HvsIApp.class);
         Router.sharedRouter().map("login", LoginActivity.class);
         Router.sharedRouter().map("register", RegisterActivity.class);
        Router.sharedRouter().map("post/:pid", PostActivity.class);
 	}
 	public static Context context() {
 		return context_;
 	}
 	public static ApiClient root() {
 		return ((API)context()).rootapi();
 	}
 	public static ApiClient api() {
 		return ((API)context()).apiapi();
 	}
 	public static ApiClient globals() {
 		return ((API)context()).globalsapi();
 	}
 	public static ApiClient blog() {
 		return ((API)context()).blogapi();
 	}
 	public static ApiClient game() {
 		return ((API)context()).gameapi();
 	}
 	public static ApiClient database() {
 		return ((API)context()).databaseapi();
 	}
 	public static ca.hvsi.lib.Account self() {
 		return self_;
 	}
 	public static void self(ca.hvsi.lib.Account val) {
 		self_ = val;
 	}
 	public static boolean logged_in() {
 		return logged_in_;
 	}
 	public static void logged_in(boolean val) {
 		logged_in_ = val;
 	}
 	public static boolean can_register() {
 		return can_register_;
 	}
 	public static void can_register(boolean val) {
 		can_register_ = val;
 	}
 	public static String lang() {
 		if (self() != null)
 			return self().language;
 		return (String)api().get("language");
 	}
 	public ApiClient rootapi() {
 		if (root_ == null)
 			root_ = new ApiClient(isDebugBuild()?"http://192.168.1.102:9055/api":"http://hvsidevel.aws.af.cm/api");
 		return root_;
 	}
 	public ApiClient apiapi() {
 		if (api_ == null)
 			api_ = (ApiClient) rootapi().get("api");
 		return api_;
 	}
 	public ApiClient globalsapi() {
 		if (globals_ == null)
 			globals_ = (ApiClient) rootapi().get("globals");
 		return globals_;
 	}
 	public ApiClient blogapi() {
 		if (blog_ == null)
 			blog_ = (ApiClient) apiapi().get("blog");
 		return blog_;
 	}
 	public ApiClient gameapi() {
 		if (game_ == null)
 			game_ = (ApiClient) apiapi().get("game");
 		return game_;
 	}
 	public ApiClient databaseapi() {
 		if (database_ == null)
 			database_ = (ApiClient) apiapi().get("database");
 		return database_;
 	}
 	public boolean isDebugBuild() 
     {
         boolean dbg = false;
         try {
             PackageManager pm = getPackageManager();
             PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
 
             dbg = ((pi.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
         } catch (Exception e) {
         }
         return dbg;
     }
 }
