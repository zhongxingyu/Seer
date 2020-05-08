 package controllers;
 
 import play.modules.securePermissions.Check;
 import play.modules.securePermissions.CheckPermission;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class SecuredController extends Controller {
 	
 	public static class Bean {
 		public String firstName,lastName;
 	}
 	
 	public static void noCheck(){
 		renderText("OK");
 	}
 
 	public static void permissionDenied(){
		if(!Secure.checkPermission("foo", new String[]{"bar"}))
 			forbidden();
 		renderText("OK");
 	}
 
 	public static void permissionDeniedOnArg(@CheckPermission("foo") String foo){
 		renderText("OK");
 	}
 
 	public static void permissionGrantedOnArg(@CheckPermission("select") String foo){
 		renderText("OK");
 	}
 
 	public static void permissionOnArgValue(@CheckPermission("select-value") String foo){
 		renderText("OK");
 	}
 
 	public static void permissionOnArgBeanValue(@CheckPermission("select-value") Bean user){
 		renderText("OK");
 	}
 
 	@Check("root")
 	public static void permissionDeniedOnRole(){
 		renderText("OK");
 	}
 
 	@Check("user")
 	public static void permissionGrantedOnRole(){
 		renderText("OK");
 	}
 
 }
